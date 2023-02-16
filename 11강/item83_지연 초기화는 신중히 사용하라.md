# 지연 초기화는 신중히 사용하라

멀티스레드 환경에선 지연초기화가 예상과 다르게 동작할 수 있다.

바로 예제를 보자.

아래 프로그램은 10,000까지 숫자를 세는 간단한 프로그램이다.  

대충 숫자는 `View` class를 통해 사용자에게 보여준다고 하고,  
`Container` class를 사용해 싱글톤 view 객체를 보장한다고 하자.

```java
class Container {
    private View view;

    public View view() {
        if (view == null) {
            view = new View();
        }

        return view;
    }
}
```

`View`클래스에선 동시성 문제를 해결하고자 `AtomicInteger`를 사용했다.

```java
class View {
    // 여기서 동시성 문제가 발생하지 않게 AtomicInteger 를 사용
    private final AtomicInteger val = new AtomicInteger(0);

    public void addOne() {
        val.addAndGet(1);
    }

    public int val() {
        return val.get();
    }
}
```

다음 코드에선 10000개의 요청(스레드)이 동시에 view에 1씩 더한다.  
간단히 생각하면 모든 스레드가 종료된 후 view의 값이 10000이 나와야 한다.

```java
class Scratch {
    static Container container = new Container();

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();

        for (int i = 0; i < 10000; i++) {
            executor.submit(Scratch::addOneOnView);
        }

        /**
         * 스레드 종료를 기다림.
         * 1초 내애 모든 스레드가 끝나지 않으면 예외를 던짐.
         */
        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
            System.out.println("테스트 실패!");
            throw new RuntimeException();
        }

        System.out.println("예상 값: 10000, 결과 값: " + container.view().val());
    }

    private static void addOneOnView() {
        container.view().addOne();
    }
}
```

결과는 아래와 같이 예상한 값이 나오지 않았다.

```text
예상 값: 10000, 결과 값: 9997
```

이유는 `Container`의 `view()`메서드에 있다.  
메서드 자체가 원자적이지 않아서 여러개의 view객체가 생성될 수 있기 때문이다.

## 🤔 해결방법1 - 일반적인 초기화를 쓰자

단순히 지연 초기화가 아닌 일반적인 초기화를 쓰면 해결된다.

아래와 같이 수정했다.

```java
class Container {
    private final View view = new View();

    public View view() {
        return view;
    }
}
```

다시 실행해보면 출력 결과는 아래와 같다.

```text
예상 값: 10000, 결과 값: 10000
```

## 🔎 해결방법2 - synchronized 접근자 사용

초기화 순환성이 꼭 필요할 때가 있다.  
초기화 비용이 정말 크거나, 순환 의존 문제가 걸려있을 때 유용하다.

그럴때는 `synchronized`접근자를 사용하자.  
예컨데 위 예제에서 `Container::view()`에 붙혀주면 된다.

```java
class Container {
    private View view;

    public synchronized View view() {
        if (view == null) {
            view = new View();
        }

        return view;
    }
}
```

출력 결과는 아래와 같다.

```text
예상 값: 10000, 결과 값: 10000
```

## 💬 해결방법3 - 정적 필드용 지연 초기화 홀더 클래스 관용구

static이 붙은 정적 필드도 synchronized접근자를 사용해도 좋지만  
다른 좋은 방법이 있다.

지연 초기화 홀더 클래스 관용구를 사용하자.  


```java
class Container {

    public static View view() {
        return ViewHolder.view;
    }

    private static class ViewHolder {
        static final View view = new View();
    }
}
```

`view()`메서드가 처음 호출되는 순간 `ViewHolder.view`가 읽히면서 `ViewHolder`클래스가 초기화된다.  
이때, `ViewHolder.view`가 초기화 될것이고, 이 순간에만 동기화가 적용된다.

VM은 클래스가 초기화 될때만 필드접근을 동기화한다.  
이후 클래스 초기화가 끝난 후에는 동기화를 하지 않아서 성능이 느려질 걱정이 없다.

## 🤩 해결방법4 - 이중검사 관용구

정적 필드가 아니라도 필드가 초기화 될때만 동기화를 하는 방법이 있다.

필드를 두번 검사하는 방식이다.  

1. 동기화 없이 초기화 되었는지 확인하는 검사
2. 동기화 있이 초기화 되었는지 확인하는 검사 

```java
class Container {
    private volatile View view;

    public View view() {
        /**
         * 첫번째 검사 : 락 없음, 필드가 이미 초기화 되어있음
         */
        View _view = view; // volatile 필드를 한번만 읽게 함 -> 성능 향상
        if (_view != null) {
            return _view;
        }

        /**
         * 두번째 검사 : 락 있음, 필드 초기화 해야 할지도 모름
         */
        synchronized (this) {
            if (view == null) {
                view = new View();
            }
            return view;
        }
    }
}
```

`synchronized` 접근자를 사용한 2번째 방식에서 이미 초기화가 된 경우를 분리한 로직이다.  

첫번째 검사에서 필드가 초기화 되었다고 확신할 수 있으면 동기화를 하지 않고 값을 반환한다.  
이 부분에는 동기화가 없으므로 필드를 `volatile`으로 선언해줘야 한다.

또, `_view` 지역변수는 volatile 필드인 `view`를 한번만 읽게 하는 역할을 한다. (첫번째 검사 내에서)

### 😬 변종 - 단일검사 관용구

반복해서 초기화 해도 상관없는 경우는 아래와 같이 아예 동기화를 안하는 방법도 있다.  
이는 `단일 검사 관용구`라고 한다.
```java
class Container {
    private volatile View view;

    public View view() {
        View _view = view; // volatile 필드를 한번만 읽게 함 -> 성능 향상
        if (_view == null) {
            view = _view = new View();
        }
        return _view;
    }
}
```

### 😬 변종 - 짜릿한 단일 검사

필드의 타입이 long, double을 제외한 다른 기본타입인데, 필드의 값이 여러번 초기화 되어도 상관없다면  
`volatile`한정자도 없앨 수 있다.

보통은 이렇게 쓰지 않는다고 한다.

```java
class Container {
    private int val;

    public int val() {
        if (val == 0) {
            val = initVal();
        }
        return val;
    }
}
```

## 👏 마무리

크롬 확장 프로그램을 만들때, MVP 구조를 적용했던 적이 있는데  
여러 객체를 **프레임워크가 없는 상황**에서, **의존성 주입을 내가 직접 해야할 때** 위 예제와 같이 **Container** 객체가 지연 초기화를 하는 방식으로 사용했던 적이 있다.  
코드를 깔끔하게 하기 위함도 있지만, **순환참조 문제** 때문에 보통은 이렇게 했었다.

책에 `초기화 순환성(initialization circularity)`, `위험한 순환 문제`라는 용어가 나오는데, 아래와 같은 구조에 생기는 문제라고 생각한다.  
예컨데 아래와 같이 두 객체가 서로를 참조해야 하는데 이럴때 생기는 문제인 것 같다.

```java
class View {
    private final Presenter presenter;

    public View(final Presenter presenter) { // Presenter를 원함
        this.presenter = presenter;
    }
}

class Presenter {
    private final View view;

    public Presenter(final View view) { // View를 원함
        this.view = view;
    }
}

class Container {
    private Presenter presenter;
    private View view;

    public View view() {
        if (view == null) {
            view = new View(presenter()); // 앗.. 이러면 안끝나는데
        }

        return view;
    }

    public Presenter presenter() {
        if (presenter == null) {
            presenter = new Presenter(view()); // 앗.. 이러면 안끝나는데
        }

        return presenter;
    }
}
```

이럴 때 일반적인 방법으로는 해결할 수 없고 아래와 같이 setter DI로 바꾸면 써먹을 수 있다.  

```java
class Container {
    ...

    public View view() {
        if (view == null) {
            view = new View();
            view.setPresenter(presenter());
        }
        ...
    }

    public Presenter presenter() {
        if (presenter == null) {
            presenter = new Presenter();
            presenter.setView(view());
        }
        ...
    }
}
...
```
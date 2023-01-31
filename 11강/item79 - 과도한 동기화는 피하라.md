## item79 - 과도한 동기화는 피하라

```
응답 불가와 안전 실패를 피하려면 동기화 메서드나 동기화 블록 안에서는 
제어를 절대로 클라이언트에게 양도하면 안된다.
```

<br/>

### 동기화란?

여러개의 쓰레드가 한 개의 리소스를 사용하려고 할 때,

사용 하려는 쓰레드를 제외한 나머지들이 접근하지 못하게 막는 것입니다.

<br/>

### 동기화 방법

- 메소드 자체 Synchronized로 선언하는 방법
- 블록으로 객체를 받아 lock을 거는 방법 - syncrhonized(this)

<br/>

### 과도한 동기화의 단점

1. 성능 저하 

2. 교착 상태 발생 

3. 예측할 수 없는 동작 발생

<br/>

### 응답 불가? , 안전 실패?

응답 불가란 : exception, timeout, lock등의 상황을 말합니다.

안전 실패란 : 실패시에도 작업을 중단하지 않는다는 것입니다.

(참고. Fail fast: 가능한 빨리 실패를 
노출하고, 작업을 중지한다.)

<br/>

### 외계인 메서드 (alien method)

- 무슨 일을 할지 모르는, 통제 불가능한 함수를 뜻합니다.
- 예외를 일으키거나, deadlock이 발생하거나, data가 훼손될 수 있습니다.
- 외계인 메서드가 하는 일에 따라 동기화된 영역은 예외를 일으키거나, 교착 상태에 빠질수도 있다.
    
<br/>    

### 교착 상태?

한정된 자원을 여러 곳에서 사용할 때 발생할 수 있는 현상을 말합니다.

<br/>

### 응답 불가와 안전 실패를 피하려면?

- 동기화 메서드/동기화 블럭 안에서는 제어를 클라이언트에 양도하면 안 된다.

    - 에일리언 함수를 사용하지 말자.

- 동기화 영역에서는 가능한 일을 적게 하자는 것이 목표라고 생각합니다.
    - (동기화는 성능 저하가 일어난다.)

<br/>

### 동시성 또는 병렬성?

동시성 : 하나의 코어에서 여러개의 프로세스가 번갈아 가면서 실행됨

병렬성 : 멀티 코어에서 개별 스레드를 동시에 실행

<br/><br/>

### 동기화 블록 안에서 외부 메서드를 호출하는 잘못된 코드

(코드 79-1)

```java
public class ObservableSet<E> extends ForwardingSet<E> {
    public ObservableSet(Set<E> set) {
        super(set);
    }

    private final List<SetObserver<E>> observers = new ArrayList<>();

    public void addObserver(SetObserver<E> observer) {
        synchronized(observers) {
            observers.add(observer);
        }
    }

    public boolean removeObserver(SetObserver<E> observer) {
        synchronized(observers) {
            return observers.remove(observer);
        }
    }

    private void notifyElementAdded(E element) {
        synchronized (observers) {
            for (SetObserver<E> observer : observers) {
                observer.added(this, element);
            }
        }
    }

    @Override
    public boolean add(E element) {
        boolean added = super.add(element);
        if (added)
            notifyElementAdded(element);
        return added;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean result = false;
        for (E element : c)
            result |= add(element);
        return result;
    }
}
```

```java
@FunctionalInterface
public interface SetObserver<E> {
    // ObservableSet에 원소가 추가되면 호출된다.
    void added(ObservableSet<E> set, E element);
}
```

<br/><br/>

### 0 ~ 99까지 출력하는 예제

이, 코드는 아무 이상없이 0부터 99까지 출력합니다.

```java
public class Main {
    public static void main(String[] args) {
        ObservableSet<Integer> set = new ObservableSet<>(new HashSet<>());
        set.addObserver((s, e) -> System.out.println(e));

        for (int i = 0; i < 100; i++) {
            set.add(i);
        }
    }
}
```

<br/>

### ConcurrentModificationException 이 발생하는 예제

```java
public class Main {
    public static void main(String[] args) {
        ObservableSet<Integer> set = new ObservableSet<>(new HashSet<>());

        set.addObserver(new SetObserver<Integer>() {
            @Override
            public void added(ObservableSet<Integer> set, Integer element) {
                System.out.println(element);

                if (element == 23) {
									set.removeObserver(this);
								}
            }
        });

        for (int i = 0; i < 100; i++) {
            set.add(i);
        }
    }
}
```

<br/>

외부 메서드를 호출하는 스레드는 이미 락을 쥐고 있으므로, 그 락이 보호하는 

데이터에 대해 개념적으로 관련이 없는 다른 작업이 진행 중이어도 

락 획득을 성공하게 되며, 그 결과 원하지 않는 결과를 유발할 수도 있습니다.

```
재진입 가능 락은 객체 지향 멀티스레드 프로그램을 쉽게 구현할 수 있도록 해주지만, 
응답 불가(교착상태)가 될 상황을 안전 실패(데이터 훼손)로 변모시킬 수도 있습니다.
```

<br/><br/>

## 문제 해결 방법

외부 메서드 호출을 동기화 블록 바깥으로 옮기는 간당한 방법으로 해결할 수 있습니다. 

위의 notifyElementAdded 메서드에서라면 관찰자 리스트를 복사해 쓰면 락 없이도 

안전하게 순회할 수 있습니다.

```java
private void notifyElementAdded(E element) {
    List<SetObserver<E>> snapshot = null;

    synchronized(observers) {
        snapshot = new ArrayList<>(observers);
    }

    for (SetObserver<E> observer : snapshot) {
        observer.added(this, element);
		}
}
```

<br/>

### 결론

- 가변 클래스를 설계할 때는 스스로 동기화해야 할지 고민해야 한다.
    - 즉, 올바르게 잘 이해하고 있다면 사용하자.
- 과도한 동기화를 피하는 것이 중요하다.
    - 합당하게 이유가 있을 때만 동기화를 사용하자.

<br/><br/>

Reference : [https://icarus8050.tistory.com/116](https://icarus8050.tistory.com/116)


# 공유 중인 가변 데이터는 동기화해 사용하라
### synchronized 키워드 
- 해당 메서드나 블록을 한번에 한 스레드씩 수행하도록 보장
  - 이 객체에 접근하는 synchronized 키워드를 가진 메서드는 그 객체에 락을 건다.
  - 객체를 하나의 일관된 상태에서 다른 일관된 상태로 변화시킴

## 동기화에 대한 오해 
- 동기화를 제대로 사용하면 어떤 메서드도 이 객체의 상태가 일관되지 않은 순간을 볼 수 없을 것. 
- 많이들 하는 오해 
  - 배타적 실행, 즉 한 스레드가 변경하는 중이라서 상태가 일관되지 않은 순간의 객체를 다른 스레드가 보지 못하게 막는 용도로만 생각함.

### 하지만, 중요한 기능이 하나 더 있다
- 동기화 없이는 한 스레드가 만든 변화를 다른 스레드에서 확인하지 못할 수 있다. 
- 동기화는 **일관성이 깨진 상태를 볼 수 없게 하는 것**은 물론, 동기화된 메서드나 블록에 들어간 스레드가 **같은 락의 보호하에 수행된 모든 이전 수정에 대한 최종 결과**를 보게 해줌

이게 대체 무슨 말인지 아래의 상황을 빌려 이해 해보도록 하자.

`Counter`라는 계수기 클래스가 있다. 

필자는 이를 이용해서 1만번 카운트하는 간단한(의미 없는) 애플리케이션을 만들었다. 그런데, 병렬로 수행하면 왠지 더 빠르고 효율적으로 계산할 것 같아, 스레드 풀에 10개의 스레드를 만들어서 병렬적으로 수행하기로 했다.

```java
public class Main {
  static class Counter {
    private int count = 0;

    public void increase() {
      count++;
    }
  }

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    final Counter counter = new Counter();
    
    for (int i = 0; i < 10000; i++) {
      threadPool.submit(counter::increase);
    }

    Thread.sleep(5000);  // 병렬 수행이므로 결과가 중간에 출력될 것을 방지하여, 모든 스레드 풀이 끝나길 5초정도 기다린다.
    System.out.println(counter.count); 
    threadPool.shutdown();  // threadPool을 닫아준다.

    // 완전히 닫혔는지 확인 
    if (threadPool.awaitTermination(20, TimeUnit.SECONDS)) {
      System.out.println(LocalTime.now() + " All jobs are terminated");
    } else {
      System.out.println(LocalTime.now() + " some jobs are not terminated");
      threadPool.shutdownNow();
    }
  }
}
```

과연 위 결과는 어떻게 됐을까? 올바르게 10000이라는 숫자가 계산되었을까? 결과를 한번 살펴보자.

```java
9987
17:09:01.022293 All jobs are terminated
```

결과로 10000이라는 숫자가 나오지 않았다. 심지어, 매번 실행할 때마다 매번 다른 숫자가 나온다. 

위와 같은 일이 발생하는 이유는 스레드들이 바라보는 `Counter`의 `count`필드의 상태의 **일관성이 깨졌기 때문**이다.
- 예를 들어, 스레드 1, 2가 동시에 `count`필드를 바라봤는데, 하필 그 때 필드 상태가 동일하게 0이었던 것이다. 스레드 1, 2는 `increase()` 메서드를 통해 `count`필드의 값을 증가시켰지만, 둘 모두 0에서 1로 증가시켜버렸기 때문에 무의미한 계산이 되었다.
  - 즉, 두 스레드는 **일관성이 깨진 상태를 봐버린 것**이다. 
  - 또한, **이전 수정에 대한 최종 결과**를 보지 못한 것이다. 즉, 스레드 1, 2가 순차적으로 실행 됐다면 스레드2는 이전 수정에 대한 최종 결과값인 **1**이라는 값을 봐야 했을 것이다.


위와 같은 문제는, 아주 간단하게 `synchronized`키워드를 통해 해결할 수 있다.
```java
public synchronized void increase() {
    count++;
}

// 결과
10000
17:15:51.528259 All jobs are terminated
```

즉, `synchronized` 키워드는 **일관성이 깨진 상태를 볼 수 없게**할 뿐만 아니라, 블록에 들어간 스레드가 **같은 락의 보호하에 수행된 모든 이전 수정에 대한 최종 결과**를 볼 수 있게 보장 해주는 것이다.


## 원자적(Atomic)
언어 명세상 `long`과 `double`외의 변수를 읽고 쓰는 동작은 원자적이다. (JLS 17.7 참고)
- 그럼 `long`과 `double`은 왜 비원자적일까? 
  - `long`과 `double` 값에 대한 단일 쓰기는 두 개의 개별 쓰기로 처리되기 떄문. (32bit씩 두번)
  - 이 때문에, 스레드가 하나의 쓰기에서 64bit 값의 처음 32bit를 보고 다른 쓰기에서 두번째 32bit를 보는 상황이 발생할 수 있다고 함 
  - `volatile` 예약어를 붙여주면, `long`, `double` 값의 쓰기 및 읽기는 항상 원자적이 된다. 
- Reference에 대한 쓰기 및 읽기는 32bit 또는 64bit 값으로 구현되는지 여부에 관계 없이 항상 원자적임 

즉, 여러 스레드가 같은 변수를 동기화 없이 수정하는 중이라도, 항상 어떤 스레드가 정상적으로 저장한 값을 **온전히 읽어옴을 보장한다**는 뜻이다.
- 하지만, 한 스레드가 저장한 값이 다른 스레드에게 **보이는가**는 보장하지 않는다고 한다.
  - 정확히 의미하는 바는 잘 모르겠지만, 위 예제에서 스레드1, 2가 0이라는 값은 온전히 읽어오지만, 스레드1이 저장한 값이 스레드2에 보이지 않는다는 의미와 상충하는 것으로 추측된다.
  - 즉, 스레드1이 먼저 연산을 완료해 변수값을 1로 바꿔놓아도, 스레드2는 스레드1이 바꿔놓은 값이 아닌, 여전히 0이라는 값을 읽어와 연산을 수행할 뿐이라는 뜻 같다.
  - 이 부분은 좀 더 공부를 해야 자세히 깨우칠 수 있을 듯 하다. (JLS 17.4 메모리 모델을 참고하자. 읽긴 했으나 아직 어려워서 더 공부가 필요함.)

> 동기화는 배타적 실행 뿐만 아니라, 스레드 사이의 안정적인 통신에 반드시 필요하다. 


공유 중인 가변 데이터를 비록 원자적으로 쓸 수 있을지라도, 동기화에 실패하면 처참한 결과로 이어질 수 있다. 

```java
public class StopThread {
  private static boolean stopRequested;

  public static void main(String[] args) throws InterruptedException {
    final Thread thread = new Thread(() -> {
      int i = 0;
      while (!stopRequested) {
        i++;
      }
    });

    thread.start();

    TimeUnit.SECONDS.sleep(1);
    stopRequested = true;
  }
}

```

위 예제는 1초 후 `stopRequested` 값이 `true`가 되면 `thread`가 멈추도록 구성되었으나, 1초가 지나도 멈추지 않는다.
- 원인은 동기화에 있는데, 동기화 하지 않으면 메인 스레드가 수정한 값을 백그라운드 스레드가 언제쯤에나 보게 될지 보증할 수 없기 때문이다.

동기화가 빠지면, 가상 머신이 다음과 같은 최적화를 수행 해버릴수도 있다고 한다.

```java
// 원래 코드
while (!stopRequested)  // 루프 내에서 수행할 필요가 없는 연산으로 취급되어
    i++;

// 최적화한 코드
if (!stopRequested)  // 호이스팅 기법에 의해 밖으로 꺼내어짐 
    while (true)
        i++

```

위 기법은 OpenJDK 서버의 VM이 실제로 적용하는 끌어올리기(hoisting)라는 최적화 기법이다.
- [참고 자료 - 매우 좋은 자료라고 생각됩니다.](https://zbvs.tistory.com/25)
  - [메모리 배리어 참고 자료](https://yoojin99.github.io/cs/Memory-Barrier/)
- 위 참고 자료를 보면, `JIT-Complie`의 `Hoisting` 최적화 기법 때문이라고 한다. 
  - 인터프리터는 한줄씩 읽어 수행하므로 Hoisting 기법이 적용이 안된다.
  - 자주 실행되는 구문의 경우, `JIT-Compiler`에 의해 기계어로 통번역된 후 실행되기 때문에, Hoisting 기법이 적용된다.


그리하여, 다음과 같이 변경하면 기대한 대로 1초 후에 스레드가 종료된다.

```java
public class StopThread {
    private static boolean stopRequested;

    private static synchronized void requestStop() {  // 쓰기
        stopRequested = true;
    }

    private static synchronized boolean stopRequested() {  // 읽기
        return stopRequested;
    }

    public static void main(String[] args) throws InterruptedException {
        final Thread thread = new Thread(() -> {
            int i = 0;
            while (!stopRequested()) {
                i++;
            }
        });

        thread.start();

        TimeUnit.SECONDS.sleep(1);
        requestStop();
    }
}
```

위 예제에서, 쓰게 메서드와 읽기 메서드 모두를 동기화 했음에 주목하자. 
- 쓰기 메서드만 동기화 해서는 충분하지 않다. 쓰기와 읽기 모두가 동기화 되어야 동작을 보장한다.
  - 어떤 기기에서는 둘 중 하나만 동기화해도 동작하는 듯 보이나, 여기에 속아서는 안된다. (필자의 컴퓨터는 실제로 읽기만 동기화해도 동작함)
- 위 두 메서드는 단순해서 동기화 없이도 원자적으로 동작한다.
- 동기화는 **배타적 수행**과 **스레드 간 통신**이라는 두 가지 기능을 수행하는데, 위 예제는 **통신 목적으로만 사용된 것**이다.


## 속도가 더 빠른 대안 `volatile`
위 예제에서, `stopRequested` 필드를 `volatile`로 선언하면 `synchronized`를 생략해도 된다.

> volatile?
> - `volatile`은 배타적 수행과는 관련 없지만, **항상 가장 최근에 기록된 값을 읽게 됨을 보장**함
> - `volatile` 선언 변수가 있는 코드는 쵲거화되지 않음 
> - 변수를 `Main Memory`에 저장함 
> - 변수의 값을 읽을 때 마다, `CPU cache`가 아닌 `Main Memory`에서 읽어옴 


```java
public class StopThread {
    private static volatile boolean stopRequested;

    public static void main(String[] args) throws InterruptedException {
        final Thread thread = new Thread(() -> {
            int i = 0;
            while (!stopRequested) {
                i++;
            }
        });

        thread.start();

        TimeUnit.SECONDS.sleep(1);
        stopRequested = true;
    }
}
```

### `volatile` 사용 시 주의점 
`volatile`은 배타적 수행과는 관련이 없다는 것을 주의해야 한다.

아래 `Counter` 클래스의 경우, `volatile`만으로는 해결되지 않는다.

```java
// 잘못된 예시 
class Counter {
    private static volatile int count = 0;   

    public static void increase() {
        count++;  // 문제가 되는 부분
    }
}
```
문제가 되는 부분은 `++` 증가 연산자다.
- 해당 연산자는 코드 상으로는 하나로 보이지만, 실제로는 `count` 필드에 **두번 접근**한다.
  - 값을 읽고 -> 1 증가한 새로운 값을 저장
- 만약 두 번째 스레드가 읽고, 저장하는 사이클의 사이를 비집고 들어와 값을 읽어가게 될 경우, 첫 번째 스레드와 똑같은 값을 돌려받게 된다.
- 이렇게 프로그램이 잘못된 결과를 계산 해내는 오류를 **안전 실패(safety failure)**라고 한다.

이는 아래와 같이 개선할 수 있다.
- `synchronized` 키워드를 통해 개선하거나 
- `AtomicLong`을 사용해 개선하는 방법 
  - 락-프리 
  - 스레드 안전 
  - `volatile`은 동기화의 두 효과 중 통신 쪽만 지원하지만, `atomic` 패키지는 원자성(배타적 실행)까지 지원함 

```java
import java.util.concurrent.atomic.AtomicLong;

// 1. synchronized 키워드를 통한 개선
class Counter {
  private static int count = 0;  // volatile 제거 (왜 제거해야 할까? 기능 중복이라서?)

  public static synchronized void increase() {
    count++;  // 문제가 되는 부분
  }
}

// 2. AtomicLong을 사용한 개선 
class Counter {
  private static AtomicLong count = 0;  

  public static synchronized void increase() {
      count.getAndIncrement();
  }
}
```


## 더더욱 좋은 방법
### 가변 데이터를 공유하지 않는 것이 가장 좋다.
- 불변 데이터만 공유하거나, 아무것도 공유하지 말자.
- 가변 데이터는 단일 스레드에서만 쓰도록 하자. 
  - 멀티 스레드 환경에서, 가변 데이터를 스레드 한정 변수로 사용할 수 있는 `ThreadLocal`을 이용하는 방법도 좋을 것 같다. 


## 또 다른 주의사항 
- 사용하려는 프레임워크와 라이브러리를 깊이 이해하자. 
  - 외부 코드는 인지하지 못한 스레드를 수행하는 복병으로 작용하는 경우도 있다.


## 참고 사항 
- 한 스레드가 데이터를 다 수정한 후 다른 스레드에 공유할 때는 해당 객체에서 공유하는 부분만 동기화해도 됨 
  - 그 객체를 다시 읽어 수정할 일이 생기기 전까지 다른 스레드들은 동기화 없이 자유롭게 값을 읽어갈 수 있음 
    - 수정하는 부분만 동기화 하라는 말인 것 같음 
  - 이러한 객체는 **사실상 불변 객체(effectively immutable)** 라고 함
  - 다른 스레드에 이런 객체를 건네는 행위를 **안전 발행(safe publication)** 이라고 함 
- 객체를 안전하게 발행하는 방법은 많다.
  - 클래스 초기화 과정에서 객체를 정적 필드, `volatile`필드, `final` 필드, 혹은 보통의 락을 통해 접근하는 필드에 저장해도 됨 
  - 혹은 동시성 컬렉션(`concurrent collection`)에 저장하는 방법도 있음


## 정리 
- 여러 스레드가 가변 데이터를 공유할 경우, 그 데이터를 읽고 쓰는 동작은 반드시 동기화 해야함 
- 동기화하지 않으면 한 스레드가 수행한 변경을 다른 스레드가 보지 못할 수도 있음 
  - 공유되는 가변 데이터를 동기화하는 데 실패한다면 **응답 불가** 상태에 빠지거나, **안전 실패**로 이어질 수 있음  
  - 디버깅 엄청 어려움 -> 간헐적이거나 특정 타이밍에만 발생할 수도 있고, VM에 따라 현상이 달라지기도 하기 때문 
- 배타적 실행은 필요 없고, 스레드끼리의 통신만 필요할 경우 `volatile`만으로 동기화 가능하다. 다만 올바르게 사용하기는 어려움.


## 개인적인 의견 
이번 아이템은 거의 3시간을 공부한 만큼 그 깊이가 깊고 알찼다는 생각이 듭니다. 아직 동기화, 멀티스레드, 동시성 등의 개념이 확 와닿지는 않지만, 꾸준히 공부해서 친숙해져야 겠다는 생각이 들었습니다. 

특히, 해당 개념들에 대해 이해를 하기 위해서는 탄탄한 CS 지식이 필요한 것 같다는 생각이 들었습니다. (특히 메모리)  


## 참고 
[JLS 17 - 스레드와 락](https://docs.oracle.com/javase/specs/jls/se17/html/jls-17.html)
[Java - ExecutorService를 사용하는 방법](https://codechacha.com/ko/java-executors/)




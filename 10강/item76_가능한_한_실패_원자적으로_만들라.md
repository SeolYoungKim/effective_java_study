# 가능한 한 실패 원자적으로 만들라 
### 실패 원자적인 특성
- 호출된 메서드가 실패하더라도 해당 객체는 메서드 호출 전 상태를 유지해야 하는 특성 

### 실패 원자적이지 않은 경우를 구현해보자
```java
public class Main {
  private static class Counter {
    private int count = 0;

    public void addCount(int number) {
      count += number;

      if (count < 0) {  // 숫자가 음수가 들어왔거나, 정수형 overflow가 발생했을 경우 예외를 발생시킨다.
        throw new IllegalArgumentException();
      }
    }

    public int count() {
      return count;
    }
  }

  private static final Counter COUNTER = new Counter();

  public static void main(String[] args) {
    final Thread thread1 = new Thread(() -> {
      try {
        failAdd();
      } catch (InterruptedException | RuntimeException e) {
        System.out.println("예외가 발생했습니다.");
      }
    });

    final Thread thread2 = new Thread(Main::readNumber);
    thread1.start();
    thread2.start();
  }

  private static void failAdd() throws InterruptedException {
    COUNTER.addCount(1);
    COUNTER.addCount(1);
    COUNTER.addCount(Integer.MAX_VALUE);
  }

  private static void readNumber() {
    for (int i = 0; i < 1000; i++) {
      System.out.println(COUNTER.count());
    }
  }
}
```
- 무언가의 개수를 세는 `Counter`라는 계수기 클래스를 하나 구현했다고 가정하자.
  - `addCount(int)`메서드는 들어온 정수 숫자를 `count` 필드에 더하고, `count`가 음수인지를 체크하여 예외를 발생시킨다.
    - 숫자가 음수가 들어왔거나, 정수형 overflow가 발생했을 경우 예외가 발생함


- `Main`클래스에는 `static`필드에 `Counter`를 하나 구현 해두고, 공유 변수로 사용하고 있다.
  - 이 때, 두 스레드가 해당 공유 변수에 접근한다.
  - thread1 : 카운터에 1을 두 번 더하고, 마지막으로 `Integer.MAX_VALUE`를 더한다.
  - thread2 : 카운터의 `count()` 메서드를 호출하여 `count` 필드의 값을 읽어온다.


위와 같은 경우, 해당 `Main`클래스의 `main` 메서드는 아래와 같은 결과를 나타낸다. 
```java
/* 실행 결과
...
-2147483647
-2147483647
예외가 발생했습니다.
-2147483647
-2147483647
...
 */
```

이러한 경우를 "실패 원자적이지 않다"라고 표현하는데, 메서드가 실패했음에도 불구하고, 객체가 메서드 호출 전 상태를 유지하지 못하고 있기 때문이다.


## 실패 원자적으로 만드는 방법
### 불변 객체로 설계하라 
- 불변 객체는 태생적으로 실패 원자적이다.
- 메서드가 실패하면 새로운 객체가 만들어지지는 않을 수 있으나, 기존 객체가 불안정한 상태에 빠지는 일은 결코 없다.
  - 불변 객체의 상태는 생성 시점에 고정되어 이는 실패 전/후로 상태가 절대 변하지 않기 때문이다. 
  - 이처럼, 상태가 변하지 않는 객체들은 실패 원자성이 무조건 보장된다.


### 가변 객체라면, 메서드를 실패 원자적으로 만들어라
#### 1. 매개 변수(혹은 변수)의 유효성을 검사하라
책에서는 Stack의 pop() 메서드를 예시로 들고 있다. size 필드 값을 변경하기 전에, 먼저 유효성 검증을 해서 예외를 미리 차단하는 것이다.


제가 구성한 예제 또한 객체가 **가변적이기 때문**에 실패 원자적이지 못한 메서드가 되었는데, 이는 다음과 같이 구성하여 실패 원자적으로 만들 수 있다.
```java
public void addCount(int number) {
    if (count + number < 0) {  
        throw new IllegalArgumentException();
    }

    count += number;
}
```

위 메서드는 count + number가 음수인지를 검사한 후에 `count` 필드의 상태 값을 변경한다. 위와 같이 구성할 경우, 메서드 수행 결과는 다음과 같다.

```java
private static void failAdd() throws InterruptedException {
    COUNTER.addCount(1);
    COUNTER.addCount(1);  // 실패하는 메서드 호출 전 : count = 2
    COUNTER.addCount(Integer.MAX_VALUE);  // 실패하는 메서드 
}
/* 실행 결과
...
2
2
예외가 발생했습니다.
2
2
...
 */
```

실패 원자적으로 구성하면 실패하는 메서드를 수행했을 때, 메서드를 수행하기 전의 객체 상태를 **그대로 유지**한다. 
- 이전에는 실패 원자적이지 못했던 메서드 수행 결과가 실패 원자적이게 구성됐다.


이 외에도, 책에서는 실패 원자적인 메서드를 만들기 위한 방법들을 다양하게 소개하고 있다.


#### 2. 실패할 가능성이 있는 모든 코드를 객체의 상태를 바꾸는 코드보다 앞에 배치하라 
이는 계산을 수행해보기 전에 **인수의 유효성을 검사 해볼 수 없을 때**사용하는 기법이다. 
- 예 : `TreeMap`
  - `TreeMap`의 `put(K,V)` 메서드를 참고해보자. 
  - 비교할 수 없는 타입의 원소를 추가하려 들 경우, 트리를 **변경하기 전**에 해당 원소가 들어갈 위치를 찾는 과정에서 `ClassCastException`을 던지도록 설계되어 있다.


#### 3. 객체의 임시 복사본에서 작업을 수행한 다음, 작업이 성공적으로 완료될 경우 원래 객체와 교체하라 
이는 데이터를 임시 자료구조에 저장해 작업하는 게 더 빠를 경우 적용하기 좋은 방식이다. 
- 예 : 정렬 메서드 
  - 정렬을 수행하기 전에 입력 리스트의 원소들을 배열에 옮겨 담음 
    - 이는 배열을 사용할 경우, 정렬 알고리즘의 반복문에서 원소들에 **훨씬 더 빠르게 접근**할 수 있기 때문임 
  - 물론 이는 성능을 높이고자 취한 결정이나, 정렬에 실패하더라도 **입력 리스트는 변하지 않는 효과**를 덤으로 얻게 됨 


#### 4. 작업 도중 발생하는 실패를 가로채는 복구 코드를 작성하여 작업 전 상태로 되돌리는 방법 수행 
이는 주로 디스크 기반의 내구성(durability)을 보장해야 하는 자료구조에 사용 됨. 
- 자주 쓰이는 방법은 아니다. 


### 실패 원자성은 권장되는 덕목이나, 항상 달성할 수 있는 것은 아님 
- 두 스레드가 동기화 없이 같은 객체를 동시에 수정할 경우에는 객체의 일관성이 깨질 수 있음  
  - `ConcurrentModificationException`을 잡아냈다고 해서 그 객체가 여전히 쓸 수 있는 상태라고 가정할 수 없음 
- `Error`는 복구할 수 없기 때문에 `AssertionError`에 대해서는 실패 원자적으로 만들기 위한 시도조차 할 필요가 없음 

### 실패 원자적으로 만들 수 있다 하더라도 **항상 그리 해야 하는 것은 아님**
- 실패 원자성을 달성하기 위한 비용이나 복잡도가 아주 큰 연산도 있기 때문  
- 그래도 문제가 무엇인지 알고나면 실패 원자성을 공짜로 얻을 수 있는 경우가 더 많음


## 정리
- 메서드 명세에 기술한 예외라면 설혹 예외가 발생하더라도 객체의 상태는 메서드 호출 전과 똑같이 유지돼야 한다
- 이 규칙을 지키지 못할 경우에는 실패 시의 객체 상태를 API 설명에 명시해야 한다.

---

## 배운 것 
"원자성"은 트랜잭션에서도 언급되는 성질인 만큼, 원자성은 정말 중요한 개념인 것 같습니다. 트랜잭션이 롤백되면 DB에 변경 사항이 적용되지 않는 것 처럼, 
메서드를 실패 원자적으로 구성하려면 "실패"할 경우 변경 사항이 적용되지 않도록 주의하면 될 것 같습니다.

또한, 해당 아이템에서 언급된 "객체의 상태"라는 것은 항상 주의해야 할 대상인 것 같습니다. 상태를 주의해야 할 경우를 가볍게 복습해 보았습니다.
- "싱글톤 스코프"의 스프링 빈은 상태를 갖게 구성되어서는 안됨 
- 다양한 스레드가 "공유하는 객체"는 항상 주의해서 사용해야 함. (synchronized/ThreadLocal/불변 객체 이용)


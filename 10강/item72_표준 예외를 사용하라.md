# 표준 예외를 사용하라

표준예외를 재사용하자.

표준 예외를 사용하게 되면 다음과 같은 이점이 있다.
1. 다른 프로그래머에게 익숙하다. (합의된 규약을 따르기 때문)
2. 클래스를 줄일 수 있으니 메모리 상으로 이득이다.

## IllegalArgumentException

호출자가 인수에 부적절한 값을 줄때 발생하는 예외다.

null값은 특별히 `NullPointerException`을 사용하자.

또 따로 범위에 해당하는 예외의 경우 `IndexOutOfBoundsException`을 사용하자.

## IllegalStateException

대상 객체가 메서드를 수행할 수 없거나 적합하지 않은 상황에 던진다.

예를들면 초기화를 안했을 때 이 예외가 발생할 수 있다.

가끔 `IllegalArgumentException`과 `IllegalStateException` 중 고민될 때가 있는데, 다음과 같은 규칙을 따르자

- 인수 값에 상관없이 예외가 발생해야 함 -> `IllegalStateException`
- 아닌 경우 -> `IllegalArgumentException`

## ConcurrentModificationException

단일 스레드에서 사용하려고 설계한 객체를 여러 스레드가 동시에 사용할때 발생하는 예외다.

예시로 ArrayList에서 remove를 함부로 사용하면 이 예외가 발생한다.  
(쓸 일이 있다면 반복자의 remove를 쓰자)
```java
public static void main(String[] args) {
    List<String> list = new ArrayList<>(List.of("A", "B", "C"));

    for (String s : list) {
        if (s.equals("A")) {
            list.remove(s);
        }
    }
}
```

## UnsupportedOperationException

이 예외는 지원하지 않는 메서드를 호출했을때 발생한다.

주로 인터페이스를 구현했지만 메서드는 제공하지 않을 경우 쓰인다.

## 💥 주의!

`Exception`, `RuntimeException`, `Throwable`, `Error`는 재사용하지 말자!

이 클래스들은 추상 클래스처럼 사용하는 클래스인 데다가 다른 예외들의 상위 클래스이므로 안정적으로 테스트할 수 없다.
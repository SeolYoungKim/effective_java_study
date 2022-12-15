# 표준 함수형 인터페이스를 사용하라

자바가 람다를 지원하면서 API를 작성하는 모범 사례도 크게 바뀌었다.  
예컨데 메서드를 재정의해 원하는 동작을 구현하는 템플릿 메서드 패턴대신, 함수 객체를 매개변수로 받는 형태를 취하는 형식으로 바뀌었다.

지금부터 템플릿 메서드 패턴에서 람다 표현식으로 바꾸는 예제가 나온다.  
책에있는 `LinkedHashMap`은 복잡하고 수정하기도 어려우니 `OperatingSystem` 객체로 구현했다.

하나씩 문제를 해결해가며 어떻게 하면 좋은지 보자.

## 💬 템플릿 메서드 패턴을 쓰면..

```java
public static void main(String[] args) {
    Window window = new Window();
    MacOs macOs = new MacOs();

    window.start();
    macOs.start();
}

static class OperatingSystem {
    void start() {
        turnOn();
        printName();
        turnOff();
    }

    protected void turnOn() {
        System.out.println("켜지는 중...");
    }

    protected void printName() {

    }

    protected void turnOff() {
        System.out.println("꺼지는 중...");
    }
}

static class Window extends OperatingSystem {

    @Override
    protected void printName() {
        System.out.println("나는 윈도우");
    }
}

static class MacOs extends OperatingSystem {

    @Override
    protected void printName() {
        System.out.println("나는 맥");
    }
}
```

`Window`와 `MacOs`에서 공통적으로 쓰이는 부분을 `OperatingSystem`에 구현하고 상속시켰다.  

다른 운영체제가 생기면 클래스를 하나 더 추가해야 된다는 점인데,  
운영체제는 종류가 엄청 많으니 골치 아프다! 😨

## 💬 람다식을 이용하면..

```java
public static void main(String[] args) {
    OperatingSystem window = new OperatingSystem();
    OperatingSystem macOs = new OperatingSystem();

    window.start(() -> System.out.println("나는 윈도우"));
    macOs.start(() -> System.out.println("나는 맥"));
}

static class OperatingSystem {
    void start(PrintNameFunction printName) {
        turnOn();
        printName.printName();
        turnOff();
    }

    protected void turnOn() {
        System.out.println("켜지는 중...");
    }
    protected void turnOff() {
        System.out.println("꺼지는 중...");
    }
}

@FunctionalInterface interface PrintNameFunction {
    void printName();
}
```

람다식을 이용해서 필요한 부분만 매개변수로 주면 클래스를 새로 만들 필요가 없다. 😊  

여기서는 interface로 `PrintNameFunction`이란 함수형 인터페이스를 구현해서 사용했다.  
당장 사용하는데 불편함은 없지만.. 어디서 많이 본 람다식 형태가 아닐까..? 🤔

## 💬 표준 함수형 인터페이스를 사용하면..

```java
public static void main(String[] args) {
    OperatingSystem window = new OperatingSystem();
    OperatingSystem macOs = new OperatingSystem();

    window.start(() -> System.out.println("나는 윈도우"));
    macOs.start(() -> System.out.println("나는 맥"));
}

static class OperatingSystem {
    void start(Runnable printName) {
        turnOn();
        printName.run();
        turnOff();
    }

    protected void turnOn() {
        System.out.println("켜지는 중...");
    }
    protected void turnOff() {
        System.out.println("꺼지는 중...");
    }
}
```

`Runnable`이라는 훌륭한 표준 함수형 인터페이스가 있다. 형태가 같은데 굳이 새로 만들어서 사용할 필요가 없다! 

# 표준 함수형 인터페이스들

이제, 표준 함수형 인터페이스를 소개한다. (책에 있는 내용으로만 구성했다.)

| 인터페이스           | 함수 시그니처          |
|-------------------|---------------------|
| UnaryOprator<T>   | T apply(T t)        |
| BinaryOperator<T> | T apply(T t1, T t2) |
| Predicate<T>      | boolean test(T t)   |
| Function<T,R>     | R apply(T t)        |
| Supplier<T>       | T get()             |
| Consumer<T>       | void accept(T t)    |
| Runnable          | void run()          |

반환값, 인자값으로 구분되어 있다.

## 기본형 변형

책에서 설명한 몇개를 나열해 보자면, 기본형을 지원하기 위해 int, long, double용으로 3개씩 변형이 있다.

예컨데 이런식이다.

Predicate
- IntPredicate
- LongPredicate
- DoublePredicate

BinaryOperator
- IntBinaryOperator
- LongBinaryOperator
- DoubleBinaryOperator

Function은 입력과 결과가 다르니 6개가 더 있다.

SrcToResultFunction 형태를 취하며  
int -> double이면 `IntToDoubleFunction`이다.

## 인수가 두개인 변형

위 표에서 소개한 인터페이스들 중에 3가지에 변형이 있다.  
다른 인수 2개를 입력으로 받고 싶어서 생겼다.

짜잔! 아래 삼인방이 주인공이다. 🎉

`BiPredicate<T,U>`, `BiFunction<T,U,R>`, `BiConsumer<T,U>`

이 중, 두번째 `BiFunction<T,U,R>`는 기본형 반환을 지원하기 위해 `ToIntBiFunction<T,U>`, `ToLongBiFunction<T,U>`, `ToDoubleBiFunction<T,U>`이 있다.

`BiConsumer<T,U>`도 질수 없다! 🤩 객체와 기본형을 인수로 가지는 병형이 존재한다. `ObjIntConsumer<T,U>`, `ObjLongConsumer<T,U>`, `ObjDoubleConsumer<T,U>`이다.

이렇게 해서 인수가 두개인 변형은 총 9개가 된다.

*우웩! 🤮 이제 끝이겠지?*

아니다! 마지막으로 boolean형을 반환하는 `BooleanSupplier`가 있다.

> 이렇게 많은 인터페이스들을 다 외울 필요는 없다.  
> 필요하면 찾아쓰자, 외우기도 어렵다.  
> 그렇다고 `IntSupplier`를 `Supplier<Integer>`로 쓰지는 말자.

----

## Comparator<T>와 ToIntBiFunction<T,U>
책에서는 표준 함수형 인터페이스를 소개하고 난 후,  
`Comparator<T>`의 예시를 들며 어떨때 함수형 인터페이스를 구현해야 하는지 소개하고 있다. 

사실 `Comparator<T>`와 `ToIntBiFunction<T,U>`는 구조적으로 동일하다.  
그럼에도 불구하고 `Comparator<T>`가 있는 이유는 뭘까? 🤔

**실수 일까?**

🙅‍♂️, 고의로 이렇게 정의했다고 한다. 책에서 설명한 이유는 이것.

1. API에서 굉장히 자주 사용하고 있다.
2. 구현하는 쪽에서 반드시 지켜야 할 규약을 담고 있다.
3. 비교자들을 변환하고 조합해주는 유용한 디폴트 메서드가 있다.

> 본인은 다른 이유를 다 제쳐도 `ToIntBiFunction<T,U>`라는 굉장히 난해한 이름의 인터페이스 대신 `Comparator<T>`를 쓰는 것이 낫다고 본다.

**나도 함수형 인터페이스를 구현해야 하나?**

이중 하나 이상을 만족하면 전용 함수형 인터페이스를 고민해보자.

1. 자주 쓰이며, 이름 자체가 용도롤 잘 설명한다.
2. 반드시 따라야 하는 규약이 있다.
3. 유용한 디폴트 메서드가 있다.

## 에너테이션을 꼭 씁시다 👏

함수형 인터페이스를 구현하고자 한다면 `@FunctionalInterface`를 반드시 달아두자.  
`@Override`를 쓰는 이유와 비슷한데, 다음과 같은 이점이 있다.

1. 코드를 보면 함수형 인터페이스라는 것을 바로 알 수 있다.
2. 추상 메서드 하나만 가지게 한다. (오류 방지)

## 람다식을 받는 메서드를 다중정의 하지말자 🙅‍♀️

`ExecutorService`의 submit 메서드는 `Callable<T>`와 `Runnable`를 받는 것을 다중정의 했다.  
그래서 올바른 메서드를 알려주려면 람다를 형변환 해줘야 한다.

사실 이는 `item52_다중정의는 주의해서 사용하라`의 예이기도 하다.
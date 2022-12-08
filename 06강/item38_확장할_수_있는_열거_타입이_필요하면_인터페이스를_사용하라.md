### **이펙티브 자바 스터디 7주차**

#### **개인 주제 |** 아이템38. 확장할 수 있는 열거 타입이 필요하면 인터페이스를 사용하라


## 열거 타입의 확장
- 열거 타입은 확장(extends)할 수 없다.
- 다만, interface를 구현(implements)할 수는 있다. (확장을 흉내낸다고 표현함.)


## Interface를 구현하는 열거 타입의 사용처
- 간단한 계산기의 연산 기능을 구현할 때
- API가 제공하는 기본 연산 외에, 사용자 확장 연산을 추가할 수 있도록 열어줘야 할 때


책에서는 "계산기"를 예로 들었다. (다른 예시를 생각 해보려 했으나, 좋은 예시를 들지 못할 것 같아 책의 예제를 그대로 가져왔습니다.)


## 계산기 예제
### 계산기 인터페이스 + 열거 타입
```java
// Operation 인터페이스
public interface Operation {
    double apply(double x, double y);
}

// Operation을 구현한 BasicOperation (기본 연산 담당)
public enum BasicOperation implements Operation {
    PLUS("+") {
        @Override
        public double apply(double x, double y) {
            return x + y;
        }
    },
    MINUS("-") {
        @Override
        public double apply(double x, double y) {
            return x - y;
        }
    },
    TIMES("*") {
        @Override
        public double apply(double x, double y) {
            return x * y;
        }
    },
    DIVIDE("/") {
        @Override
        public double apply(double x, double y) {
            return x / y;
        }
    },
    ;

    private final String symbol;

    BasicOperation(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}

// Operation을 구현한 ExtendedOperation
public enum ExtendedOperation implements Operation {
    EXP("^") {
        @Override
        public double apply(double x, double y) {
            return Math.pow(x, y);
        }
    },
    REMAINDER("%") {
        @Override
        public double apply(double x, double y) {
            return x % y;
        }
    },
    ;

    private final String symbol;

    ExtendedOperation(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
```


### 관련 메서드
```java
private static <T extends Enum<T> & Operation> void test(Class<T> opEnumType, double x,
        double y) {
    for (Operation op : opEnumType.getEnumConstants()) {
        System.out.printf("%.1f %s %.1f = %.1f%n", x, op, y, op.apply(x, y));
    }
}

private static void test(Collection<? extends Operation> opSet, double x,
        double y) {
    for (Operation op : opSet) {
        System.out.printf("%.1f %s %.1f = %.1f%n", x, op, y, op.apply(x, y));
    }
}
```

조슈아 선생님의 예제를 들고왔다. 제네릭스와 타입 토큰을 한껏 활용하시는 모습을 볼 수 있는데, 복습도 할 겸 간단히 짚고 넘어가겠다.

- `<T extends Enum<T> & Operation>`
    - T는 Enum인 동시에, Operation을 구현한 타입이어야 한다는 뜻이다.  
- `Collection<? extends Operation>`
    - Operation을 구현한 타입들의 Collection이라는 뜻이다. 
    - 조슈아 선생님은 위 제네릭스가 복잡하다며, 이렇게도 구현할 수 있다는 것을 보여주신 것 같다.
- 파라미터의 `Class<T> opEnumType`
    - 타입 토큰을 받겠다는 의미다.


```java
test(ExtendedOperation.class, x, y);
test(BasicOperation.class, x, y);   

test(Arrays.asList(ExtendedOperation.values()), x, y);
test(Arrays.asList(BasicOperation.values()), x, y);
```

메서드를 사용하면 두 메서드 모두 아래와 같은 결과가 나온다. 
```text
3.0 ^ 3.0 = 27.0
3.0 % 3.0 = 0.0
3.0 + 3.0 = 6.0
3.0 - 3.0 = 0.0
3.0 * 3.0 = 9.0
3.0 / 3.0 = 1.0
```


## interface를 구현한 열거 타입의 장점
- 다형성을 이용할 수 있다.
    - 필요에 따라, 구현체를 갈아끼울 수 있다.
    - 위 예시의 경우, 메서드에 `Operation`인터페이스를 파라미터로 받음으로써, 필요한 연산이 무엇인지에 따라 메서드는 큰 변경 없이 파라미터에 용도에 맞는 구현체만 넘겨줌으로써, 유연하게 사용할 수 있을 것 같다.


## 문제점
책에 문제점들이라고 소개한 것들인데, 조금은 이해가 가지 않지만, 내 방식대로 한번 해석을 해봤다. (문제가 있을 경우, 댓글로 알려주시면 감사합니당..!)


### 열거 타입 끼리 구현을 상속할 수 없다는 것이 문제
저자는 interface를 구현한 열거 타입들끼리 구현을 상속할 수 없다는 것을 문제삼고 있다.  
이게 왜 문제가 될까? 아무래도 인터페이스를 구현한 열거 타입(A)를 새로운 열거 타입(B)이 상속을 할 수 없는 상황을 말하는 것이 아닐까 생각한다.  

즉, 아래와 같은 상황이라고 할 수 있을 것 같다.
```java
public interface EnumInterface {

    void test();
}

// 기존 구현체
public enum EnumA implements EnumInterface {
    ENUM_A{
        @Override
        public void test() {
            System.out.println("이 기능은 B에서 중복되어 사용된다.");
        }
    }
} 

// 새로운 구현체
public enum EnumB extends EnumA {  // 불가능하다!

    // EnumB는 EnumA의 기능을 기본적으로 사용하고,
    // B만의 기능을 구현하고 싶으나 그럴 수 없다.
    // 어쩔 수 없이 중복 구현해서 사용하거나, EnumA에 구현하는 방법을 택해야 할 것 같다.
    // (EnumA가 사용되는 곳에서는 그 기능이 필요 없음에도..!)
}
```


### 중복이 될 가능성 존재
위 `Operation` 예시의 경우, 기호를 저장하고 찾는 로직이 `BasicOperation`과 `ExtendedOperation` 모두에게 들어가야만 한다. 
- 예시의 기호를 저장하고 찾는 로직이 생성자와 toString같은데.. 저자가 이 부분을 두고 말하는 게 맞는지는 모르겠다. 그냥 계산 메서드를 얘기하는 것 같기도.... 

아무튼 로직이 뭔지 여부를 떠나, 두 열거 타입 간에 공유하는 기능이 많아지면 많아질 수록, 중복되는 로직이 아주 많아질 것이다.    
즉, interface에 추상 메서드가 늘어날 수록 두 열거 타입에도 구현해야 할 메서드가 아주 많이 늘어날 것이다. 그러므로, 공유하는 기능이 많아질 경우 별도의 도우미 클래스나, 정적 메서드로 분리하는 방식으로 중복을 제거할 것을 권고하고 있다.


## 요약
- 열거 타입 자체는 확장 불가
- 인터페이스와 그 인터페이스를 구현하는 기본 열거 타입을 함께 사용해 같은 효과 낼 수 있음.
  - 클라이언트가 해당 인터페이스를 구현해 자신만의 열거 타입(혹은 다른 타입)을 만들 수 있도록 함.
- API가 기본 열거 타입을 직접 명시하지 않고, 인터페이스 기반으로 작성되었다면
  - 기본 열거 타입의 인스턴스가 쓰이는 모든 곳을 새로 확장한 열거 타입의 인스턴스로 대체하여 사용 가능.


---

# 개인적으로 찾아본 추가적인 내용
## `Enum`의 `getClass()` vs `getDeclaringClass()`
책의 예제 메서드를 작성하다가, 좀 더 확인 해보고 싶은 것이 생겨서 아래와 같이 코드를 구성하여 확인해봤다.

```java
private static void print(Operation operation) {
    System.out.println("operation의 클래스는 " + operation.getClass() + "입니다.");
}

public static void main(String[] args) {
    BasicOperation minus = BasicOperation.MINUS;
    print(minus);
    System.out.println(minus.getDeclaringClass());

    ExtendedOperation exp = ExtendedOperation.EXP;
    print(exp);
    System.out.println(exp.getDeclaringClass());

}
```

위와 같은 코드를 구성하여, 다형성도 확인 해볼 겸, 내부에서 `getClass()`메서드를 통해 `Operation`타입으로 들어온 무언가의 클래스를 얻어 출력 해보았다.

```text
출력 결과
operation의 클래스는 class chapter_06.item38.BasicOperation$2입니다.
operation의 클래스는 class chapter_06.item38.ExtendedOperation$1입니다.
```

결과를 보니, 뭔가 이상했다. 뒤에 숫자가 붙어서 출력되는 것이었다.  

이에 대해 좀 알아보았다.

> 참고
> [참고 1](https://www.logicbig.com/how-to/code-snippets/jcode-java-enum-declaring-class.html)
> [참고 2](https://stackoverflow.com/questions/5758660/java-enum-getdeclaringclass-vs-getclass)


위 두 자료를 참고하여 알아본 결과는 다음과 같다.


- Enum은 `{}`를 이용하여 클래스 본문을 가질 수 있다.
    - 클래스 본문을 가질 경우, Enum의 클래스 본문을 나타내는 내부 클래스가 생성된다.
    - 이러한 내부 클래스는 Enum의 하위 클래스가 된다.

- `getClass()`메서드로 클래스를 얻을 경우, 클래스 본문을 나타내는 내부 익명 클래스가 반환된다.
    - 그래서, 정확한 클래스 타입을 얻고자 하는 경우 + Enum이 클래스 본문을 가진 경우에는 `getDeclaringClass()`메서드를 이용해야 Enum의 정확한 클래스 값을 얻을 수 있다.
    - 다만, 내부 구현부가 없는 Enum의 경우에는, `getClass()`와 `getDeclaringClass()`가 똑같은 클래스 값을 반환한다.


### 예시

```java
public enum Test {

    A,
    B{},
    C{
        @Override
        public String toString() {
            return "this is C";
        }
    };

    public static void main(String[] args) {
        Class<? extends Test> classA = A.getClass();
        Class<Test> declaringClassA = A.getDeclaringClass();

        System.out.println("classA = " + classA);
        System.out.println("declaringClassA = " + declaringClassA);
        System.out.println();

        Class<? extends Test> classB = B.getClass();
        Class<Test> declaringClassB = B.getDeclaringClass();

        System.out.println("classB = " + classB);
        System.out.println("declaringClassB = " + declaringClassB);
        System.out.println();

        Class<? extends Test> classC = C.getClass();
        Class<Test> declaringClassC = C.getDeclaringClass();

        System.out.println("classC = " + classC);
        System.out.println("declaringClassC = " + declaringClassC);
        System.out.println();

        // 공식 문서에, 두 열거형 상수 e1, e2가 있을 때, e1.getDeclaringClass() == e2.getDeclaringClass()인 경우에만 동일한 열거형이라고 한다.

        System.out.println("class 비교");
        System.out.println("A == B : " + (classA == classB));
        System.out.println("B == C : " + (classB == classC));
        System.out.println("A == C : " + (classA == classC));
        System.out.println();

        System.out.println("declaringClass 비교");
        System.out.println("A == B : " + (declaringClassA == declaringClassB));
        System.out.println("B == C : " + (declaringClassB == declaringClassC));
        System.out.println("A == C : " + (declaringClassA == declaringClassC));
    }
}
```

### 출력
```text
classA = class chapter_06.item38.Test
declaringClassA = class chapter_06.item38.Test

classB = class chapter_06.item38.Test$1
declaringClassB = class chapter_06.item38.Test

classC = class chapter_06.item38.Test$2
declaringClassC = class chapter_06.item38.Test

class 비교
A == B : false
B == C : false
A == C : false

declaringClass 비교
A == B : true
B == C : true
A == C : true
```



















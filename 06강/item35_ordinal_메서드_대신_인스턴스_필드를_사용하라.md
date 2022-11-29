### **이펙티브 자바 스터디 6주차**

#### **개인 주제 |** 아이템 35. ordinal 메서드 대신 인스턴스 필드를 사용하라


열거 타입 상수는 순서가 적용되는데, 선언 순서대로 "순서 값"을 갖게 됩니다.

해당 순서 값은 `Enum`에서 기본으로 제공하는 `ordinal()`메서드를 통해 받아올 수 있습니다. (이는 0부터 할당됩니다.)

예를 들어, 해당 순서 값은 다음과 같이 사용될 수 있습니다. 

아이폰이 먼저 출시된 순서대로 정렬이 되어있습니다. 그리고, 출시 순서를 가져오는 메서드인 `orderOfRelease()`를 다음과 같이 정의했습니다.


```java
public enum Iphone {

    IPHONE_4, IPHONE_5, IPHONE_6, IPHONE_7, IPHONE_8, IPHONE_X;

    public int orderOfRelease() {
        return ordinal() + 1;
    }

    public static void main(String[] args) {
        int orderOfRelease = Iphone.IPHONE_4.orderOfRelease();
        System.out.println(orderOfRelease);
    }

}
```


위 코드는 우리가 의도한대로 잘 동작합니다.

하지만 무엇이 잘못되었다는 걸까요?


### 유지보수하기 끔찍하다..!

위 코드는 유지보수하기 정말 끔찍한 코드라고 하는데, 이유는 다음과 같습니다. 

- 누군가 Enum의 선언 순서를 바꾸거나, 중간에 무언가를 추가하게 된다면 `ordinal()`값이 전부 바뀌기 때문입니다. 이렇게 되면, 위 코드는 오동작하게 됩니다.
- 이미 사용 중인 ordinal 값과 같은 상수는 추가할 방법이 없습니다.
- 연속된 값이기 때문에, 중간에 값을 비워둘 수 없습니다. (1,2,5 <- 이런 식으로 선언이 안된다는 뜻입니다.)


### 해결책
아주 간단히 해결할 수 있습니다.
아래와 같이 인스턴스 필드에 순서를 지정해주면 됩니다.

```java
public enum Iphone {

	// 아래와 같이 하면, 중간에 값을 비워둘 수도 있습니다.
    IPHONE_4(1), IPHONE_5(2), IPHONE_6(3), IPHONE_7(4), IPHONE_8(6), IPHONE_X(7);

    private final int orderOfRelease;

    Iphone(int order) {
    	this.orderOfRelease = orderOfRelease;
    }

    public int orderOfRelease() {
    	return orderOfRelease;
    }

}
```

### Enum의 API 문서에는...
[API 문서] (https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Enum.html)

"대부분 프로그래머는 이 메서드를 쓸 일이 없다. 해당 메서드는 EnumSet과 EnumMap과 같이 열거 타입 기반의 범용 자료구조에 쓸 목적으로 설계되었다."라고 작성돼 있습니다. 그러니, 이런 용도가 아니라면 사용하지 맙시다!

---
### 느낀 점

조슈아 선생님은 Enum에서 하나같이 공통적으로 강조하는 것이 있습니다.
> Enum 선언에 의존적으로 코드를 짜지 말것
즉, 상수 선언으로 인해 값이 바뀌는 메서드가 있거나, 무언가 추가를 해야하는 것이 있으면 안된다고 강조 합니다.(ex: switch-case)

그러니, 선언에 영향을 받는 메서드는 구성하지 않는 것이 좋을 것 같습니다.





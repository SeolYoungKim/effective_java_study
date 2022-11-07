### **이펙티브 자바 스터디 3주차** 

#### **개인 주제 |** 아이템 16. public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라

아래와 같은 클래스는 "인스턴스 필드를 모아놓는 역할" 외에는 아무것도 하지 않는 "퇴보한 클래스"입니다.

이와 같은 클래스는 데이터 필드에 직접 접근할 수 있습니다. 따라서, 캡슐화의 이점을 제공하지 못합니다.

-   API를 수정하지 않고는 내부 표현을 바꿀 수 없습니다.  <- ???
-   불변식을 보장할 수 없습니다.
-   외부에서 필드에 접근할 때 부수작업을 수행할 수 없습니다. (메서드로 제공하면 검증 등의 부수 작업이 됩니다.)

```
public class GarbageClass {
    public String garbage1;
    public String garbage2;
}
```

그래서, "철저한" 객체지향 프로그래머들은 필드들을 모두 private로 바꾸고, pubilc한 getter를 추가해서 사용한다고 합니다.

-   이렇게 하면 값을 설정하거나 값을 가져올 때, 다른 부가적인 작업을 할 수 있게 됩니다.
-   저는 getter를 자바 빈 프로퍼티 방식의 getXxx가 아닌, java 17에서 지원하는 record 방식으로 설정해봤습니다. 
-   getXxx 형식의 getter는 자바 빈 프로퍼티를 사용하는 프레임워크에서는 필수로 제공해야 할 일이 있으니, 그런 부분에 한해서는 자바 빈 프로퍼티 방식을 사용해야 합니다.

```
public class ImprovedClass {
    private final String improve1;
    private final String improve2;

    public ImprovedClass(String improve1, String improve2) {
        this.improve1 = improve1;
        this.improve2 = improve2;
    }

    public String improve1() {
        return improve1;
    }

    public String improve2() {
        return improve2;
    }
}
```

저는 조슈아 선생님의 "철저한 객체지향 프로그래머는 public 클래스의 필드를 모두 private으로 바꾸고, getter를 추가한다"라는 의견에 대해 조금 더 생각을 해보았습니다.

-   "철저한 객체지향 프로그래머"는 필드를 모두 private으로 바꾼다. 
    -   이것은 맞는 말 같습니다. 해당 클래스 외에는 필드에 접근하지 못하게 함으로써 캡슐화를 할 수 있고, 정보를 은닉할 수 있으니, 객체지향 프로그래머라면 이를 마다할 리 없다고 생각합니다.
-   접근자(getter)를 추가한다
    -   모든 객체지향 프로그래머가 마치 getter를 무조건 추가할 것 처럼 말해두었습니다. 또한, 이 방식이 "확실히 맞다"라고 주장합니다. 이는 조금 극단적인 표현이었던 것 같습니다. (물론, 그럴 의도가 아니었을 수도 있습니다!)
    -   Spring에서 view에서 받은 값을 @ModelAttribute로 dto에 바인딩 시킬 때는 getter/setter가 필요합니다.
        -   이러한 필수적인 경우를 제외하고, getter의 사용이 필수적이지 않은 경우에는 굳이 사용하지 않아도 될 것 같습니다.
        -   필요 없는데도 getter를 열어둘 경우, 의도치 않게 다른곳에서 많이 오용될 가능성이 있을 것 같습니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fk.kakaocdn.net%2Fdn%2FvU33t%2FbtrQBKft5yj%2FpXaKsNAju6IRWuBPbwjAJ0%2Fimg.png)

package-private 클래스나 private 중첩 클래스라면 데이터 필드를 노출해도 괜찮습니다. (그래도 저는 getter와 같은 메서드를 사용하는 게 나을 것 같습니다. 검증 등의 부가 작업을 할 수 있으므로..)

위와 같이 작성할 경우,

-   default class는 같은 패키지 내부가 아니라면 접근조차 할 수 없는것을 확인할 수 있습니다. public으로 열어놔도, 필요한 곳에서만 사용할 수 있다는 뜻입니다.
-   inner private static class / private class의 경우, 해당 클래스를 선언한 외부 클래스에서 조차 접근하지 못하는 것을 확인할 수 있습니다.
    -   private class의 경우, 반대로 외부 클래스의 필드를 아무렇지 않게 참조할 수 있습니다. 

아마도 위와 같은 특징때문에 저자는 package-private 클래스나 private 중첩 클래스라면 데이터 필드를 노출해도 아무 문제가 없다고 한 것 같습니다.

---

#### 정리 + 제 생각

-   public 클래스는 절대 가변 필드를 직접적으로 노출해서는 안된다.
    -   필드는 무조건 private으로 하는 게 좋을 것 같습니다. 추후 필요할 경우 private-package까지 푸는건 괜찮을지도...
    -   getter는 필요할 때만 제공하는 게 좋을 것 같습니다.
-   불변 필드라면 노출 시 덜 위험하지만, 완전히 안심할 수는 없다.
    -   그러니 private을 이용해서 필드를 노출하지 않는게 좋을 것 같네요.
-   package-private 클래스나, private 중첩 클래스에서는 종종 필드를 노출하는 편이 나을 때도 있다.
    -   노출 하는 편이 나을 때도 있지만, 값 검증 등 부수적인 작업이 필요할 경우를 대비해서 getter와 같은 메서드를 통해 사용하는 것이 더 좋은 선택일 수도 있다고 생각합니다.

# [ITEM 1] 생성자 대신 정적 팩터리 메서드를 고려하라.

클라이언트가 클래스의 인스턴스를 얻는 전통적인 수단은 pulic 생성자이다.
하지만 클래스의 인스턴스를 얻는 다른 방법이 있는데, 정적 팩터리 메서드 라고 하는 방법이 있다.
다음 코드는 boolean의 박싱클래스(Wrapper class)인 Boolean에서 발췌한 간단한 예다.

```java
public final class Boolean implements java.io.Serializable,
                                      Comparable<Boolean>
{
    public static final Boolean TRUE = new Boolean(true);
  
    public static final Boolean FALSE = new Boolean(false);

    ...

    public static Boolean valueOf(boolean b) {
        return (b ? TRUE : FALSE);
    }
    
    ...
    
}
```

~~정적 팩터리 메서드는 GOF 디자인 패턴에서의 팩터리 메서드와 다르다~~

평소 Jpa를 사용할때면 Domain Entity에서 정적 팩토리 메서드를 사용하여 객체를 만들고, 데이터베이스에서 받아온 객체를 Dto로 변환하는 과정을 정적 펙토리 메서드로 사용하고 있었는데, 
왜 좋은지, 왜 사용해야하는지는 생각해 본 적이 없는것 같다...

아래는 최근에 사용했던 정적 팩토리 메서드의 예시이다.

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    ...
    
    @Builder
    public Member (String email, String username, String password, Role role) {
        this.email = email;
        ...
    }

    /*
     * 이부분이 정적 팩토리 메서드
     */
    public static Member createMember(Member member) {
        return Member.builder()
                .email(member.getEmail())
                .username(member.getUsername())
                .password(member.getPassword())
                .role(member.getRole())
                .build();
    }

}
```

---

이 방식(정적 팩토리 메서드)은 장점과 단점이 모두 존재하는 방법이다.

### 장점
#### 1. 이름을 가질 수 있다.
- 생성자에 매개변수와 생성자 자체만으로는 반환될 객체의 특성을 제대로 설명할 수 없다. 반면 정적 팩터리 메서드는 이름만 잘 지어놓으면 반환 객체의 특성을 쉽게 묘사할 수 있다.
```java
public Member(String id, String pwd) {
    this.id = id + "아이디";
    this.pwd = pwd + "비밀번호";
}

public static Member createMemberWithKoreanTranslate(String id, String pwd) {
    return Member.builder()
        .id(id + "아이디")
        .pwd(pwd + "비밀번호")
        .build();
}
```
대충 이런 느낌이지 않을까 싶다.

하나의 시그니처로 하나의 생성자를 만들 수 있지만, 매개변수의 순서를 다르게 하거나 매개변수의 수를 다르게 하는 등의 방법으로 생성자 생성 제한을 피해 볼 수 있지만, 좋지 않은 방법이다.
사용하는 사람도 해당 생성자가 어떤 역할을 하는지 정확히 알기 힘들기 때문이다.

한 클래스에 시그니처가 같은 생성자가 여러 개 필요할 것 같으면 생성자 대신 정적 팩터리 메서드를 사용하고, 이름을 잘 짓도록 하자!

#### 2. 호출될 때마다 인스턴스를 새로 생성하지는 않아도 된다.

이 덕분에 불변 클래스는 인스턴스를 미리 만들어 놓거나 새로 생성한 인스턴스를 캐싱하여 재활용하는 식으로 불필요한 객체 생성을 피할 수 있다.
대표적으로 `Boolean.valueOf(boolean)` 메서드는 객체를 아예 생성하지 않는다.
생성비용이 매우 큰 객체가 자주 요청된다면 성능향상에 도움이 된다.

반복되는 요청에 같은 객체를 반환하는 식으로 정적 팩터리 방식의 클래스는 인스턴스의 생명주기를 통제할 수 있다.
인스턴스를 통제하면 클래스를 싱글턴으로, 인스턴스화 불가로 만들수 있고, 불변값 클래스에서 동치인 인스턴스가 단 하나뿐임을 보장할 수 있다.

#### 3. 반환 타입의 하위 타입 객체를 반환할 수 있는 능력이 있다.
- 반환 객체의 클래스를 자유롭게 선택할 수 있는 엄청난 유연성을 갖는다.
- API를 만들 때 구현클래스를 공개하지 않고도 그 객체를 반환할 수 있어 API를 작게 유지할 수 있다.

프로그래머는 명시한 인터페이스대로 동작하는 객체를 얻을 것임을 알기에 굳이 구현 클래스를 찾아보지 않아도 된다.

#### 4. 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.
- 반환 타입의 하위타입이기만 하면 어떤 클래스의 객체를 반환하든 상관없다.
```java
public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) {
    Enum<?>[] universe = getUniverse(elementType);
    if (universe == null)
        throw new ClassCastException(elementType + " not an enum");

    if (universe.length <= 64) // 원소의 숫자가 64개 이하면 RegularEnumSet 반환 (EnumSet의 하위 타입)
        return new RegularEnumSet<>(elementType, universe);
    else // 원소의 숫자가 65개 이상이면 JumboEnumSet 반환 (EnumSet의 하위 타입)
        return new JumboEnumSet<>(elementType, universe);
}
```
클라이언트는 두 클래스의 존재를 모르고, 다음 릴리즈때 삭제해도 아무 상관이 없다.

#### 5. 정적 팩터리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.
- 이러한 유연함은 서비스 제공자 프레임워크를 만드는 근간이 된다.
- 대표적인 서비스 제공자 프레임워크로는 JDBC가 있다.
- 서비스 제공자 프레임워크에서의 제공자는 서비스의 구현체이다.
- 그리고 이 구현체들은 클라이언트에 제공하는 역할을 프레임워크가 통제하여 클라이언트를 구현체로부터 분리해준다.
- 서비스 제공자 프레임워크의 3가지 핵심 컴포넌트
  - Service Interface (구현체의 동작 정의)
  - Provider Registration API (제공자가 구현체를 등록할 때 사용)
  - Service Access API (클라이언트가 서비스의 인스턴스를 얻을 때 사용)

조건을 명시하지 않으면 기본 구현체를 반환하거나 지원하는 구현체들을 하나씩 돌아가며 반환하는데, 이 서비스 접근 API가 `유연한 정적 팩터리`이다.

---

### 단점
#### 상속을 하려면 public 이나 protected 생성자가 필요하니 정적 팩터리 메서드만 제공하면 하위 클래스를 만들 수 없다.
컬렉션 프레임워크의 유틸리티 구현 클래스들을 상속할 수 없다는 이야기이다.
상속보다 컴포지션(Item 18)을 하도록 유도하고 불변타입(Item 17)으로 만드려면 이 제약을 지켜야 한다는 점에서 오히려 장점으로 받아들일 수 있다.
~~Item18과 Item17의 내용이라 잘 모르겠습니다.~~
다만 불변타입의 경우, 변수에 final을 붙이고 만들고, Lombok Setter를 선언하지 않는 방식으로 만든다고 얼핏 알고있습니다.

#### 정적 팩토리 메서드는 프로그래머가 찾기 어렵다.
생성자처럼 API 설명에 명확하게 드러나지 않으니 사용자는 정적 팩토리 메서드 방식 클래스를 인스턴스화 할 방법을 알아내야 한다.
그렇기 때문에 정적 팩터리 메서드에 잘 알려진 규약을 따라 짓는 식으로 문제를 완화시켜줘야 한다.

- from
- of
- valueOf
- instance , getInstance
- create , newInstance
- getType
- newType
- type

---

정적 팩터리 메서드와 public 생성자는 각자의 쓰임새가 있기 때문에, 각 장단점을 이해하고 사용하는게 좋다. 하지만 public 생성자보다 정적 팩토리 메서드를 사용하는게 유리한 경우가 더 많으므로 자주 사용합시다!

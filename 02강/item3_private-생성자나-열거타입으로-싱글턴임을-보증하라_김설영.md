### **이펙티브 자바 스터디 1주차** 

#### **개인 주제 |** 아이템 3. private 생성자나 열거 타입으로 싱글턴임을 보증하라

#### 싱글톤이란?

-   인스턴스를 오직 하나만 생성할 수 있는 클래스
-   무상태 객체 or 설계상 유일해야 하는 시스템 컴포넌트

#### 문제점

#### 문제점

클래스를 싱글톤으로 만들면, 이를 사용하는 클라이언트를 테스트하기가 어려워질 수 있다고 합니다. 타입을 인터페이스로 정의하고, **그 인터페이스를 구현해서 만든 싱글톤이 아닐 경우**, 가짜(mock) 객체 구현으로 대체할 수 없기 때문이라고 합니다.

대체 이게 대체 무슨 말일까요????????

-   싱글톤 클래스 자체는 가짜 객체(Mock)를 만들 수 없다.
-   인터페이스를 구현한 싱글톤 클래스는 가짜 객체(Mock)를 만들 수 있다.

가짜 객체를 만들 수 없는 경우, 클라이언트 코드에 대한 단위 테스트 (독립적인 테스트)를 할 수 없다는 제약 사항이 생기게 됩니다. 

또한, 싱글톤 객체를 각 테스트마다 매번 생성하는 것은 상당히 비효율적이고, operation cost가 많이 들 수도 있다고 합니다. 사실 Mock 객체도 생성해서 사용할텐데, 이러한 연산 비용의 차이가 발생한다는 내용이 잘 이해가 가지 않았습니다. 

-   싱글톤 객체를 생성할 때는 "필요한 모든 필드가 들어가있는 상태인 객체"를 생성하고, Mock객체를 생성할 때는 싱글톤 객체보다 필드가 적어서 상대적으로 비용이 적게 든다. 라고 추측하고 있습니다

**Mocking이 안되는 경우**

```java
// 싱글톤 판다 티모 클래스입니다.
public class PandaTeemo {

    public static PandaTeemo PANDA_TEEMO = new PandaTeemo();

    private String pandaMoja;
    private String daeNaMoo;
    private String samgakMushroom;

    private PandaTeemo() {
    }

    public void poisonShot() {
        System.out.println("판다 티모가 독침을 쏩니다.");
    }

    public void sheared() {
        System.out.println("판다 티모가 찢겼습니다.");
    }
}

// Top 객체는 판다 티모를 직접적으로 참조하고 있습니다.
public class Top {

    private final PandaTeemo pandaTeemo;

    public Top(PandaTeemo pandaTeemo) {
        this.pandaTeemo = pandaTeemo;
    }

    public boolean init;
    public boolean teemoIsDead;

    public void fight() {
        init = true;
        pandaTeemo.poisonShot();
        pandaTeemo.sheared();
        teemoIsDead = true;
    }
}

// 테스트를 할 때, Mock 객체를 만들 수 없습니다.
// 판다 티모 싱글톤을 그대로 만들기 때문에, 
// 껍데기만 만드는 Mock 객체를 사용할 때 보다 연산 비용이 비교적 많이 듭니다.
@Test
void fail() {
    Top top = new Top(PandaTeemo.PANDA_TEEMO);  // 아무런 기능이 없는 Mock 객체를 만들어서 사용할 수 없습니다. 싱글톤 객체를 생성하여 사용해야 합니다.
    top.fight();

    // 심지어 해당 테스트는 독립적인 테스트가 아닙니다.
    // Top 객체의 Test만을 원했지만, 싱글톤 객체인 판다 티모 객체가 관여하기 때문입니다.
    assertThat(top.init).isTrue();
    assertThat(top.teemoIsDead).isTrue();
}
```
![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FdTvrKS%2FbtrPt6iPIU0%2FbFtQPubwTGlGwXOld1g3Ok%2Fimg.png)

**Interface를 구현한 싱글톤 객체를 만듦으로써 Mocking이 가능해지는 경우**

```java
// 인터페이스 입니다.
public interface Teemo {
    void poisonShot();
    void sheared();
}

// 인터페이스 구현체이며, 싱글톤 오메가 티모 클래스입니다.
public class OmegaTeemo implements Teemo{

    public static OmegaTeemo OMEGA_TEEMO = new OmegaTeemo();

    private String omegaMoja;
    private String gun;
    private String bombMushroom;

    private OmegaTeemo() {
    }

    @Override
    public void poisonShot() {
        System.out.println("오메가 티모가 독침을 쏩니다.");
    }

    @Override
    public void sheared() {
        System.out.println("오메가 티모가 찢겼습니다.");
    }
}

// Top2 객체는 오메가 티모 구현체가 아닌, 티모 인터페이스를 참조하고 있습니다.
public class Top2 {

    private final Teemo teemo;

    public Top2(Teemo teemo) {
        this.teemo = teemo;
    }

    public boolean init;
    public boolean teemoIsDead;

    public void fight() {
        init = true;
        teemo.poisonShot();
        teemo.sheared();
        teemoIsDead = true;
    }
}

// Test 디렉토리에 Teemo 인터페이스를 구현한 MockTeemo 객체를 만듭니다.
public class MockTeemo implements Teemo {
    @Override
    public void poisonShot() {
        System.out.println("오메가 티모가 독침을 쏩니다.");
    }

    @Override
    public void sheared() {
        System.out.println("오메가 티모가 찢겼습니다.");
    }
}

// 오메가 티모가 가진 필드가 이용되지도 않고, 생성 되지도 않습니다.
@Test
void success() {
    Top2 top2 = new Top2(new MockTeemo());  // 싱글톤 오메가 티모가 생성되지 않습니다. 그저 MockTeemo입니다.
    top2.fight();

    assertThat(top2.init).isTrue();
    assertThat(top2.teemoIsDead).isTrue();
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbGJwD0%2FbtrPp3gQjmA%2FcQQ4Z3br6j7EbkVYp3Wuk0%2Fimg.png)

---



#### 싱글톤을 만드는 방법 1 : public static final 필드 + private 생성자

```java
public class Kim {

    public static final Kim SINGLETON_KIM = new Kim();

    private Kim() {
    }
}

@DisplayName("싱글톤이 유지된다.")
@Test
void singleton() {
    Kim singletonKim1 = Kim.SINGLETON_KIM;
    Kim singletonKim2 = Kim.SINGLETON_KIM;

    System.out.println("singletonKim1 = " + singletonKim1);
    System.out.println("singletonKim2 = " + singletonKim2);

    assertThat(singletonKim1).isEqualTo(singletonKim2);
    assertThat(singletonKim1 == singletonKim2).isTrue();
}
```

-   생성자는 private으로 감춰두고, 유일한 멤버에 접근할 수 있도록 public static 멤버를 마련합니다.
    -   위와 같이 작성하면, SINGLETON\_KIM을 초기화 할 때 딱 한번만 호출하게 됩니다.
    -   생성자가 private이며, 다른 생성자가 없기 때문에, 해당 인스턴스가 전체 시스템에서 하나뿐임이 보장됩니다.

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2F5pkOk%2FbtrPg0diRIm%2F5euZz7jzxJyOKTqYS3Y301%2Fimg.png)

-   하지만, 위 방법은 Reflection API에서 AccessibleObject.setAccessible을 사용해 private 생성자를 호출할 경우에는 생성을 막을 수 없습니다.

```java
@DisplayName("리플렉션으로 접근할 경우에는 접근하여 생성할 수 있다.")
@Test
void reflection() throws Exception {
    Kim singletonKim = Kim.SINGLETON_KIM;

    Arrays.stream(Kim.class.getDeclaredConstructors())
            .forEach(constructor -> {
                constructor.setAccessible(true);
                try {
                    Kim reflectedKim = (Kim) constructor.newInstance();
                    System.out.println("reflectedKim = " + reflectedKim);
                } catch (InstantiationException | IllegalAccessException | UnsupportedOperationException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });

    System.out.println("singletonKim = " + singletonKim);
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FbEASJu%2FbtrPgWPmIhq%2FjQUDUNkwPQgTZQSRrjKjj1%2Fimg.png)

위와 같이, 리플렉션 API를 이용할 경우 싱글톤을 깰 수 있습니다. 이를 방지하기 위해서는, 두 번째 객체가 생성되려 할 때 예외를 던지도록 아래와 같이 구성하면 됩니다. 

```java
public class Kim {

    public static final Kim SINGLETON_KIM = new Kim();

    private Kim() {
        if (SINGLETON_KIM != null) {
            throw new UnsupportedOperationException("이미 생성되어 있는 객체입니다.");
        }
    }
}

@DisplayName("리플렉션으로 접근할 경우에는 접근하여 생성할 수 있다.")
@Test
void reflection() throws Exception {
    Kim singletonKim = Kim.SINGLETON_KIM;

    Arrays.stream(Kim.class.getDeclaredConstructors())
            .forEach(constructor -> {
                constructor.setAccessible(true);
                try {
                    Kim reflectedKim = (Kim) constructor.newInstance();
                    System.out.println("reflectedKim = " + reflectedKim);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    System.out.println("객체가 생성되지 않습니다!!! 해당 객체는 싱글톤으로 유지되어야 합니다.");
                }
            });

    System.out.println("singletonKim = " + singletonKim);
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FdPMs6o%2FbtrPhneRQyQ%2F9HSkFNTM6cZYtcmKkRdiQ1%2Fimg.png)

방법 1의 장점

-   해당 클래스가 싱글턴임이 API에 명확하게 드러납니다.
-   public static 필드에 final을 붙여 재할당을 막았기 때문에, 다른 객체를 참조할 수 없습니다.
-   간결합니다.

---

#### 싱글톤을 만드는 방법 2 : 정적 팩터리 메서드 + private 생성자

```java
public class Lee {
    private static final Lee SINGLETON_LEE = new Lee();

    private Lee() {
    }

    public static Lee getInstance() {
        return SINGLETON_LEE;
    }
}

@DisplayName("싱글톤이 유지된다")
@Test
void singleton() {
    Lee singletonLee1 = Lee.getInstance();
    Lee singletonLee2 = Lee.getInstance();

    assertThat(singletonLee1 == singletonLee2).isTrue();
    assertThat(singletonLee1).isEqualTo(singletonLee2);
}
```

-   getInstance는 항상 같은 객체의 참조를 반환합니다. -> 싱글톤을 보장합니다.
-   단, Reflection API를 통한 접근은 가능하기 때문에, 방법1과 같이 Reflection API에서의 두번째 생성도 방지하기 위해서는 예외 처리를 해주어야 합니다.

방법 2의 장점 (아래의 장점이 필요하지 않을 경우, 방법 1이 더 좋습니다.)

-   API를 바꾸지 않고도 싱글톤이 아니게 변경할 수 있습니다.
    -   유일한 인스턴스를 반환하는 정적 팩토리 메서드가 호출하는 스레드 마다 다른 인스턴스를 넘겨주게 설정할 수 있습니다.
-   정적 팩토리를 제네릭 싱글톤 팩토리로 만들 수 있습니다.
    -   아래와 같이 만들 경우, Set에 다양한 타입의 값들을 넣을 수 있게 됩니다.

```java
// 정적 팩토리 예시
public class GenericSingletonFactory {

    public static final Set<Object> GENERIC_SET = new HashSet<>();

    private GenericSingletonFactory() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> getSet() {
        return (Set<T>) GENERIC_SET;
    }
}

@Test
void genericTest() {
    Set<String> set1 = GenericSingletonFactory.getSet();
    Set<Integer> set2 = GenericSingletonFactory.getSet();
    Set<Kim> set3 = GenericSingletonFactory.getSet();
    Set<Lee> set4 = GenericSingletonFactory.getSet();

    set1.add("이게 된다고?");
    set2.add(123456);
    set3.add(Kim.SINGLETON_KIM);
    set4.add(Lee.getInstance());

    Set<Object> genericSet = GenericSingletonFactory.GENERIC_SET;

    System.out.println(genericSet);
    assertThat(genericSet.size()).isEqualTo(4);
    
    // 그런데, 아래와 같이 쓰는 것도 경고메세지가 뜰 뿐, 문제는 없다.
    // 그럼 제네릭 싱글톤 팩토리 메서드를 쓰는 이유는..? 알아봐야할듯..
    HashSet hashSet = new HashSet();
        hashSet.add("이게 된다고?");
        hashSet.add(123456);
        hashSet.add(Kim.SINGLETON_KIM);
        hashSet.add(Lee.getInstance());

        System.out.println(hashSet);
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Frd4vF%2FbtrPhmUAZk9%2F62TlldomUevJajCyo5I8Jk%2Fimg.png)

-   정적 팩토리의 메서드 참조를 공급자(supplier)로 사용할 수 있습니다.
    -   Supplier가 필요한 곳에 사용할 수 있습니다.

```java
@DisplayName("공급자로 사용하기")
@Test
void supplier() {
    Supplier<Lee> supplier = Lee::getInstance;  // 공급자로 사용 가능
}
```

#### 방법 1, 방법 2의 직렬화

두 방식으로 만든 싱글톤 클래스를 직렬화 하려면 Serializable을 구현하는 것만으로는 싱글톤을 보증하기 부족한데, 이는 직렬화된 인스턴스를 역직렬화 할 때 마다 새로운 인스턴스가 만들어지기 때문입니다. 

-   직렬화된 인스턴스를 역직렬화할 땐 "Reflection API"를 이용하기 때문입니다.
-   그래서, 역직렬화 할 때 Reflection을 통해 인스턴스가 생성되어버립니다.

책에 기술된 해결 방법은 다음과 같습니다.

-   모든 인스턴스를 transient 선언 해줘야 합니다.
    -   이는 반드시 해주지 않아도 되는것 같습니다. 이유를 찾아보고 있어요.
-   readResolve 메서드를 제공해줘야 합니다.
    -   readResolve 메서드를 구현해둘 경우, 역직렬화에 사용되는 Reflection API가 해당 메서드를 인식하고 실행합니다.
    -   때문에, readResolve 메서드가 싱글톤 객체를 반환하도록 구성하면 싱글톤을 보장할 수 있습니다.

예제 코드입니다.

```java
// 싱글톤이 유지되지 않는 경우
public class SerializeSingletonFail implements Serializable {

    private static final SerializeSingletonFail SINGLETON_FAIL = new SerializeSingletonFail();

    private final String str = "fail";

    private SerializeSingletonFail() {
    }

    public static SerializeSingletonFail getInstance() {
        return SINGLETON_FAIL;
    }
}


// 싱글톤이 유지되는 경우
public class SerializeSingletonOk implements Serializable {

    private static final SerializeSingletonOk SINGLETON_OK = new SerializeSingletonOk();

    private final String str = "ok";  // 선언 안해줘도 문제없던데.. 왜지?

    private SerializeSingletonOk() {
    }

    public static SerializeSingletonOk getInstance() {
        return SINGLETON_OK;
    }

    // 직렬화된 값을 역직렬화 할 때, Object를 새로 만드는 대신, 해당 메서드를 Reflection API를 이용하여 사용한다.
    private Object readResolve() {
        return SINGLETON_OK;
    }
}
```

이를 확인한 테스트 코드입니다.

```java
public class SerializeTest {

    @DisplayName("싱글톤이 유지되지 않는다")
    @Test
    void fail() {
        SerializeSingletonFail fail = SerializeSingletonFail.getInstance();
        byte[] serializedData = serialize(fail);
        SerializeSingletonFail result = (SerializeSingletonFail) deserialize(serializedData);

        System.out.println(fail == result);
        System.out.println(fail.equals(result));

        assertThat(fail == result).isFalse();
        assertThat(fail).isNotEqualTo(result);
    }

    @DisplayName("싱글톤이 유지된다.")
    @Test
    void success() {
        SerializeSingletonOk success = SerializeSingletonOk.getInstance();
        byte[] serializedData = serialize(success);
        SerializeSingletonOk result = (SerializeSingletonOk) deserialize(serializedData);

        System.out.println(success == result);
        System.out.println(success.equals(result));

        assertThat(success == result).isTrue();
        assertThat(success).isEqualTo(result);
    }

    private byte[] serialize(Object instance) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(instance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return bos.toByteArray();
    }

    private Object deserialize(byte[] serializedData) {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializedData);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FCs8ZS%2FbtrPp30jDBI%2FmmQinw0PMVs6S2DMpXDns1%2Fimg.png)

---

#### 싱글톤을 만드는 방법 3 : 원소가 하나인 Enum을 선언하라

```java
public enum Park {
    INSTANCE;
}

@DisplayName("Enum은 싱글톤이다.")
@Test
void enumTest() {
    Park park1 = Park.INSTANCE;
    Park park2 = Park.INSTANCE;
    Park park3 = Park.INSTANCE;

    System.out.println("park1 = " + park1);
    System.out.println("park2 = " + park2);
    System.out.println("park3 = " + park3);

    Assertions.assertThat(park1).isEqualTo(park2).isEqualTo(park3);
    Assertions.assertThat(park1 == park2 && park2 == park3).isTrue();
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Feww9x0%2FbtrPh9OwV10%2F5B1N2qx4dvUGYvUvb0ojwk%2Fimg.png)

public 필드 방식과 비슷하지만 아래와 같은 장점이 있습니다.

-   더 간결합니다.
-   추가적인 노력 없이 직렬화를 할 수 있습니다.
-   복집한 직렬화 상황이나 리플렉션 공격이 온다 하더라도, 제 2의 인스턴스가 생기는 것을 완벽하게 막아줍니다.

거의 대부분의 상황에서, 원소가 하나뿐인 Enum 타입이 싱글톤을 만드는 가장 좋은 방법입니다. 단, 싱글톤이 Enum 외의 클래스를 상속해야 할 경우에는 해당 방식을 사용할 수 없습니다. (Enum 타입이 다른 인터페이스를 구현하도록 할 수는 있습니다.)

```java
// 아래와 같이 인터페이스를 구현하여 사용 가능.
public interface EnumInterface {

    void hahaha();

}

public enum Park implements EnumInterface{
    INSTANCE;

    @Override
    public void hahaha() {
        System.out.println("hahaha");
    }
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fo7yGf%2FbtrPkqWxEkS%2FkhWl9FIY0hk7M9k4zlXqg1%2Fimg.png)

참고

[https://madplay.github.io/post/what-is-readresolve-method-and-writereplace-method](https://madplay.github.io/post/what-is-readresolve-method-and-writereplace-method)

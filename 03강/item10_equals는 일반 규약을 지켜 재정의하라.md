# 아이템 10 : equals는 일반 규약을 지켜 재정의하라

## ❗️아래 중 하나라도 만족하면 equals을 재정의 하지 말자!

<br>

- 각 인스턴스가 본질적으로 고유하다.

    ```java
    Person p1 = new Person("daisy");
    Person p2 = new Person("daisy");
    ```
    `Person` 객체는 이름이 같아도 본질적으로 고유해서 객체 참조 비교를 사용하는게 옳다.


- 논리적 동치성을 검사할 필요가 없다.

    ```java
    PersonName p1 = new PersonName("daisy");
    PersonName p2 = new PersonName("daisy");
    ```
    위는 논리적 동치성을 검사해야 하는 예제로 
    `PersonName` 객체는 이름이 같으면 논리적으로 같다. 두개의 객체는 같다고 보는 것이 맞으니 재정의를 해야한다.

- 상위 클래스의 equals을 사용해도 논리적으로 잘 맞다.
- 클래스가 private이거나 package-private(default)이고 equals을 사용할 일이 없다.

---

## ❕equals를 재정의 할때 고려해야 하는 규칙

- 반사성 : 모든 x!=null에 대해 `x.equals(x) = true`.
- 대칭성 : 모든 x!=null, y!=null에 대해 `x.equals(y) = y.equals(x)`.
- 추이성 : 모든 x!=null, y!=null, z!=null에 대해 `x.equals(y) = y.equals(z) = A`라면 `z.equals(x) = A`이다.
- 일관성 : 모든 x!=null, y!=null에 대해 `x.equals(y)`를 반복 호출하면 항상 같은 결과를 반환한다.
- null이 아님 : 모든 x!=null에 대해 `x.equals(null) == false`는 항상 옳다.

---

## 📌equals와 상속

아래는 Intellij에서 자동으로 생성해주는 equals함수다.  
```java
class PersonName {
    private String name;

    public PersonName(String name) {
        this.name = name;
    }

    public String get() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersonName that = (PersonName) o;

        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
```

반사성, 대칭성, 추이성, 일관성, null이 아님 을 모두 만족했다.  
이러면 된걸까?

상속을 생각하지 않는 경우는 괜찮겠지만, 상속을 하게 된다면 문제가 생길 것이다.

<br>

아래의 예제를 보자.

```java
class SuperPersonName extends PersonName {

    public SuperPersonName(String name) {
        super(name);
    }
}
```

`SuperPersonName`객체 끼리 비교하면 상관없지만, `PersonName`과 `SuperPersonName`을 비교하면 안된다.

<br>

```java
public static void main(String[] args) {
    List<PersonName> personNameList = new ArrayList<>();

    personNameList.add(new SuperPersonName("liubei"));
    System.out.println("liubei라는 사람이 있나? : " + personNameList.contains(new PersonName("liubei")));
}
```

<br>

위의 예제를 실행하면 결과는 아래와 같다.

```bash
liubei라는 사람이 있나? : false
```

이렇게 된 이유는 `PersonName`과 `SuperPerson`의 `getClass()`값이 다르기 때문이다.

<br>

고치자면 `getClass`가 아닌 `instanceof`를 사용하면 된다.  
아래는 고친 `PersonName`클래스의 `equals`함수다.

```java
class PersonName {
    ...

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonName)) return false;

        PersonName that = (PersonName) o;

        if (name != null ? !name.equals(that.name) : that.name != null) 
            return false;

        return true;
    }

    ...
}
```

main함수를 돌리면 출력은 아래와 같다.
```bash
liubei라는 사람이 있나? : true
```

책에서는 리스코프 치환 원칙에 따르면 SuperPersonName이 곧 PersonName이니 SuperPersonName과 PersonName을 비교할 수 있어야 한다고 했다.  
getClass를 쓰면 다형성을 쓸 수가 없다는 의미다.

---

## instanceof를 쓰면 다른 문제가 야기된다.

이번엔 ```SuperPerson```에 새로운 필드를 넣어보자.  
아래와 같이 작성했다.

```java
class SuperPersonName extends PersonName {

    private String houseName;

    public SuperPersonName(String houseName, String name) {
        super(name);
        this.houseName = houseName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuperPersonName)) return false;
        if (!super.equals(o)) return false;

        SuperPersonName that = (SuperPersonName) o;

        if (!houseName.equals(that.houseName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + houseName.hashCode();
        return result;
    }
}
```

main 함수도 아래와 같이 변경해야 주었다.
```java
public static void main(String[] args) {
    List<PersonName> personNameList = new ArrayList<>();

    personNameList.add(new SuperPersonName("liu", "liubei"));
    System.out.println("liubei라는 사람이 있나? : " + personNameList.contains(new PersonName("liubei")));
}
```

결과는 아래와 같다.

```
liubei라는 사람이 있나? : true
```

하위 클래스에 필드가 생기면 생기는 문제로,  
이와 같은 예제가 억지스럽긴 하지만, 일어날 수 있는 일이다.  

liu의 가문의 liubei와 그냥 liubei는 같은 것이라고 할 수 없는데, 같은 것으로 인식했다.

사실 가장 문제는 `personName.equals(SuperPersonName) != SuperPersonName.equals(personName)`의 문제가 생긴다.  
equals의 원칙 중 하나인 대칭성이 깨진다는 것이다.

책에서는 그래도 instanceof를 사용하라고 하고, 대칭성이 깨지는 문제는 객체지향 언어의 근본적인 문제라고 했다. 

이런 문제를 피하기 위해 하위 클래스에 필드를 추가하지 않는 것이 좋으며  
우회법으로 **상속이 아닌 컴포지션**을 쓰자.

---

## 정리

책에서는 equals 메서드 구현 방법을 단계적으로 정해놓았다.

1. == 연산자를 사용해 입력이 자기 자신의 참조인지 확인한다.
2. instanceof 연산자로 입력이 올바른 타입인지 확인한다.
3. 입력을 올바른 타입으로 형변환한다.
4. 입력 객체와 자기 자신의 대응되는 '핵심' 필드들이 모두 일치하는지 확인한다.

---

## 추가 주의사항

- equals를 재정의할 땐 hashCode도 재정의하자.
- `equals(PersonName o)`은 재정의가 아니다. `equals(Object o)`가 맞다.

## 참고

https://stackoverflow.com/questions/596462/any-reason-to-prefer-getclass-over-instanceof-when-generating-equals
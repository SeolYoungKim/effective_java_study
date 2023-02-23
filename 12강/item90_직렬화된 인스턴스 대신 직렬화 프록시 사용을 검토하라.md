# 직렬화된 인스턴스 대신 직렬화 프록시 사용을 검토하라

## 직렬화 프록시 패턴 (serialization proxy pattern)

Serializable을 구현한 경우, 생성자 이외의 방법으로 인스턴스를 생성할 수 있게 된다.  
당연히 버그와 보안 문제가 일어날 수 있다.

책에선 직렬화 프록시 패턴은 **일관성 검사나 방어적 복사가 필요 없는 이상적인 패턴**이라고 소개하고 있다.  
직렬화 프록시 패턴은 아래와 같이 이너 클래스로 직렬화 프록시 클래스를 선언하고, 본 클래스를 직렬화 할때 프록시 객체로 *바꿔치기* 하는 방식이다.

```java
final class Period implements Serializable {
    private final Date start;
    private final Date end;

    public Period(Date start, Date end) {
        this.start = new Date(start.getTime());
        this.end = new Date(end.getTime());
        if (this.start.compareTo(this.end) > 0)
            throw new IllegalArgumentException(start + " after " + end);
    }

    public Date start() {
        return start;
    }

    public Date end() {
        return end;
    }

    @Override
    public String toString() {
        return start + " - " + end;
    }

    private static class SerializationProxy implements Serializable {
        private final Date start;
        private final Date end;

        SerializationProxy(Period p) {
            this.start = p.start;
            this.end = p.end;
        }

        private Object readResolve() {
            return new Period(start, end);
        }
    }

    // 바꿔치기 하는 부분!
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    // 역직렬화 방지
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("프록시가 필요합니다.");
    }
}
```

직렬화 하기 전, 아래의 `writeReplace` 메서드가 호출되어 본 클래스의 인스턴스가 아닌 프록시 클래스의 인스턴스가 직렬화 된다.

```java
// 바꿔치기 하는 부분!
private Object writeReplace() {
    return new SerializationProxy(this);
}
```

본 클래스인 `Period`는 직렬화되지 않는다. 하지만 공격자가 어떻게든 바이트 스트림을 얻어내 조작할 수 있다.

따라서 아래의 `readObject`메서드에서 무조건적인 예외를 던져야 한다.

```java
// 역직렬화 방지
private void readObject(ObjectInputStream stream) throws InvalidObjectException {
    throw new InvalidObjectException("프록시가 필요합니다.");
}
```

마지막으로 프록시 클래스인 `SerializationProxy`를 역직렬화할때 본 클래스인 `Period`로 바꾸는 과정이 필요하다. 아래의 `readResolve`메서드에서 수행한다.

```java
private Object readResolve() {
    return new Period(start, end);
}
```

이 방식은 역직렬화시에도 생성자를 이용하기 때문에 아래와 같은 이점이 있다.

1. 생성자 이외의 다른 곳에서 불변식을 검사하지 않아도 된다
2. 생성자를 사용하기 때문에 필드에 final 키워드도 마음대로 붙일 수 있다.
3. 직렬화 가능한 불변 객체를 만들 수 있다.
4. 바이트 조작 공격에도 자유롭다.

직렬화 프록시 패턴을 이용해 *역직렬화 된 인스턴스*와 *직렬화 된 인스턴스*의 클래스가 달라져도 직렬화를 지원할 수도 있다. (실전에서도 유용하다고 함.)

`EnumSet`은 public 생성자 없이 정적 팩터리만 제공한다.  
아래 `noneOf()`메서드를 보면 원소가 64이하인 경우 `RelgularEnumSet`으로, 그보다 더 크면 `JumboEnumSet`으로 생성한다.

```java
public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) {
    //EnumSet.of, EnumSet.allOf 모두 이 메서드를 사용한다.
    ...

    if (universe.length <= 64)
        return new RegularEnumSet<>(elementType, universe);
    else
        return new JumboEnumSet<>(elementType, universe);
}
```

64개 이하 열거 타입을 가진 `EnumSet`(`RegularEnumSet`)를 직렬화한 후,  
열거 타입을 몇개 추가해 64초과로 만든 뒤, 역직렬화 하면 `JumboEnumSet`이 생성된다.

`EnumSet`이 직렬화 프록시를 이용해 클래스에 구애받지 않는 방식으로 직렬화, 역직렬화 하기 때문이다.

```java
private static class SerializationProxy<E extends Enum<E>>
        implements java.io.Serializable
{

    private static final Enum<?>[] ZERO_LENGTH_ENUM_ARRAY = new Enum<?>[0];
    private final Class<E> elementType;
    private final Enum<?>[] elements;

    SerializationProxy(EnumSet<E> set) {
        elementType = set.elementType;
        elements = set.toArray(ZERO_LENGTH_ENUM_ARRAY);
    }

    @SuppressWarnings("unchecked")
    @java.io.Serial
    private Object readResolve() {
        // instead of cast to E, we should perhaps use elementType.cast()
        // to avoid injection of forged stream, but it will slow the
        // implementation
        EnumSet<E> result = EnumSet.noneOf(elementType);
        for (Enum<?> e : elements)
            result.add((E)e);
        return result;
    }

    @java.io.Serial
    private static final long serialVersionUID = 362491234563181265L;
}
```

## 한계

직렬화 프록시 패턴은 두가지의 한계가 있다.  

1. 클라이언트가 멋대로 확장할 수 있는 클래스에는 적용할 수 없다.
2. 객체 그래프에 순환이 있는 클래스에는 적용할 수 없다. (프록시 클래스는 실제 클래스와 연관이 없어 참조시 `ClassCastException`이 발생한다.)
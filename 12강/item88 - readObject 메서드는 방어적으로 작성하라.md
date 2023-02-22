## item88 - readObject 메서드는 방어적으로 작성하라

<br/>

### 이 클래스를 직렬화하기로 결정했다고 생각해보자.

클래스 선언에 impements, Serializable을 선언해 간단히 직렬화를 끝낼 수 있을 것 것이라고 한다. 

<br/>

하지만, 이것만으로는 클래스의 불변식을 보장하지 못한다.

```java
public final class Period {
    private final Date start;
    private final Date end;

    // 수정한 생성자 - 매개변수의 방어적 복사본을 만든다.
    public Period(Date start, Date end) {
        this.start = new Date(start.getTime());
        this.end = new Date(end.getTime());

        if (start.compareTo(end) > 0)
            throw new IllegalArgumentException(
                start + "가" + end + "보다 늦다.");
    }

    // 수정한 접근자 - 가변 필드의 방어적 복사본을 만든다.
    public Date start() {
        return new Date(start.getTime());
    }
    public Date end() {
        return new Date(end.getTime());
    }
}
```

<br/><br/>

## 이 클래스가 불변식을 보장하지 못하는 이유

이 클래스가 불변식을 보장하지 못하는 이유는 **readObject 메서드** 때문이다.

```
readObject는 Serializable을 구현한 모든 타입을 생성할 수 있고, 
그 타입들 안의 모든 코드도 수행할 수 있다. 
```

<br/>

보통의 생성자처럼 **인수가 유효한지 검사**해야 하고, 필요하다면 **방어적으로 복사**해야 한다. 

이 작업을 제대로 수행하지 못하면 공격자는 **아주 쉽게 불변식을 깨트릴 수 있다.**

<br/><br/>

## 프로그램을 실행하면

`Fri Jan 01 12:00:00 PST 1999 - Sun Jan 01 12:00:00 PST 1984`를 출력한다.

```java
public class BogusPeriod {

    private static final byte[] serializedForm = {
        (byte)0xac, (byte)0xed, 0x00, 0x05, 0x73, 0x72, 0x00, 0x06
         // 바이트 코드들
    }

    public static void main(String[] args) {
        Period p = (Period) deserialize(serializedForm);
        System.out.println(p);
    }

    // 주어진 직렬화 형태로부터 객체를 만들어 반환한다.
    static Object deserialize(byte[] sf) {
        try {
            return new ObjectInputStream(new ByteArrayInputStream(sf)).readObject();
        } catch(IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
```

Period를 직렬화한 후 바이트 배열 리터럴을 조작해서 불변식을 깨트린 것이다.

<br/><br/>

## 해결 방법



이 문제의 근원은 Period의 readObject 메서드가 방어적 복사를 충분히 하지 않은 데 있다. 

**객체를 역직렬화할 때는 클라이언트가 소유하면 안 되는 객체 참조를 갖는 필드** 

**모두를 반드시 방어적으로 복사해야 한다.**

<br/>

readObject에서는 불변 클래스 안의 모든 private 가변 요소를 방어적으로 복사해야 한다.

```java
private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    s.defaultReadObject();

    // 가변 요소들을 방어적으로 복사한다.
    start = new Date(start.getTime());
    end = new Date(end.getTime());

    // 불변식을 만족하는지 검사한다.
    if (start.compareto(end) > 0) {
        throw new InvalidObjectException(start + " after " + end);
    }
}
```

<br/><br/>

## 기본 readObject와 커스텀 readObject의 선택 기준

transient 필드를 제외한 모든 필드의 값을 매개변수로 받아 유효성 검사 없이 필드에 대입하는 

public 생성자를 추가해도 괜찮은지를 기준으로 생각해보면 된다.

<br/>

괜찮다면 **기본 readObject** 를 사용해도 되지만, 아니라면 직렬화 프록시 패턴을 사용하거나 

커스텀 readObject 메서드를 만들어 모든 유효성 검사와 방어적 복사를 수행해야 한다.

<br/><br/>

## 핵심 정리

- readObject 메서드는 언제나 public 생성자를 작성하는 자세로 임하자.
- readObject 메서드는 어떤 바이트 스트림이 넘어와도 유효한 인스턴스를 만들어내야 한다.
- 바이트 스트림이 진짜 직렬화된 인스턴스라고 가정해선 안된다.
- private이어야 하는 객체 참조 필드는 각 필드가 가리키는 객체를 방어적으로 복사하자.
- 방어적 복사 후에는 반드시 불변식 검사가 뒤따라야 한다.

<br/><br/>

참조 : https://jjingho.tistory.com/139
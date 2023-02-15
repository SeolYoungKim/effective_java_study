# 커스텀 직렬화 형태를 고려해보라
개발 일정에 쫓기는 상황에서는 API 설계에 노력을 집중하는 편이 나을 것임
- 다음 릴리스에서 제대로 다시 구현하기로 하고, 이번 릴리스에서는 동작만 하도록 만들어 놓으라는 뜻 


하지만, `Serializable`을 구현하고 기본 직렬화 형태를 사용한다면, 다음 릴리스 때 버리려 한 현재의 구현에 영원히 발이 묶이게 될 수 있음 
- 대표적으로, Java의 `BigInteger`와 같은 일부 클래스가 이 문제에 시달림 


## 먼저 고민해보고 괜찮다고 판단될 때만 기본 직렬화 형태를 사용하라 
기본 직렬화 형태는 유연성, 성능, 정확성 측면에서 신중히 고민한 후 합당할 때만 사용해야 함 
- 직접 설계한다 하더라도, 기본 직렬화 형태와 거의 같은 결과가 나올 경우에만 기본 형태를 사용해야 함 

## 이상적인 직렬화 형태
- 물리적인 모습과 독립된 논리적인 모습만을 표현해야 함 
- 객체의 물리적 표현과 논리적 내용이 같다면 기본 직렬화 형태라도 무방함
  - 성명은 논리적으로 이름, 성, 중간 이름이라는 3개의 문자열로 구성된다.
  - 아래 예시 코드의 인스턴스 필드들은 이 논리적 구성 요소들을 정확히 반영했다.

```java
import java.io.Serializable;

public class Name implements Serializable {
    /**
     * 성. null이 아니어야 함
     * @serial 
     */
    private final String lastName;

    /**
     * 이름. null이 아니어야 함
     * @serial
     */
    private final String firstName;

    /**
     * 중간이름. 중간 이름이 없다면 null 
     * @serial
     */
    private final String middleName;
}
```

### 기본 직렬화 형태가 적합하다고 결정했더라도, 불변식 보장과 보안을 위해 readObject 메서드를 제공해야 할 때가 많다. 
- 위 Name 클래스의 경우, readObject 메서드가 lastName과 firstName 필드가 null이 아님을 보장해야 함 


## 직렬화 형태에 적합하지 않은 예

```java
import java.io.Serializable;

public final class StringList implements Serializable {
    private int size = 0;
    private Entry head = null;

    private static class Entry implements Serializable {
        String data;
        Entry next;
        Entry prev;
    }
    
    // ... 나머지 코드 
}
```
- 이 클래스는 
  - **논리적으로** 일련의 문자열을 표현함 
  - **물리적으로**는 문자열들을 이중 연결 리스트로 연결했음 
- 해당 클래스에 기본 직렬화 형태를 사용하면, 각 노드의 양방향 연결 정보를 포함해 모든 엔트리를 철두철미하게 기록함 


위와 같이 객체의 물리적 표현과 논리적 표현의 차이가 클 때 기본 직렬화 형태를 사용하면 크게 네 가지 면에서 문제가 발생한다.
1. 공개 API가 현재의 내부 표현 방식에 영구히 묶인다. 
   - 위 예시의 private static class인 Entry가 공개 API가 되어버림 
   - 다음 릴리스에서 내부 표현 방식을 바꾼다 하더라도, StringList 클래스는 여전히 연결 리스트로 표현된 입력도 처리할 수 있어야 함 
   - 즉, 연결 리스트를 더이상 하용하지 않더라도 관련 코드를 절대 제거할 수 없음
2. 너무 많은 공간을 차지할 수 있다.
   - 위 예시의 직렬화 형태는 연결 리스트의 모든 엔트리와 연결 정보까지 기록했음.
   - 하지만, 엔트리와 연결 정보는 내부 구현이기 때문에 직렬화 형태에 포함할 가치가 없음
   - 이에 따라 직렬화 형태가 너무 커져서 디스크에 저장하거나 네트워크로 전송하는 속도가 느려짐 
3. 시간이 너무 많이 걸릴 수 있다.
   - 직렬화 로직은 객체 그래프의 위상에 관한 정보가 없어, 그래프를 직접 순회해볼 수밖에 없음.
4. 스택 오버플로가 발생할 수 있다.
   - 기본 직렬화 과정은 객체 그래프를 재귀 순회함 
   - 해당 작업은 중간 정도 크기의 객체 그래프에서도 자칫 스택 오버플로를 일으킬 수 있음 
   - 심지어 스택 오버플로가 발생되는 최소 크기가 시도할 때마다 달라질 수 있음 (플랫폼 차이일 수도..)


## 합리적인 직렬화 형태로 변경하기 
- 물리적인 상세 표현은 배제한 채, 논리적인 구성만 담자
- 개선 버전의 직렬화 
  - 이전 버전에 비해 절반 정도의 공간을 차지 
  - 두 배 정도 빠른 수행 속도 
  - 스택 오버플로 발생하지 않음 (크기 제한 사라짐)

```java
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class StringList implements Serializable {
    private transient int size = 0;
    private transient Entry head = null;

    private static class Entry {
        String data;
        Entry next;
        Entry prev;
    }

    public final void add(String s) { /*내부 구현*/ }

    /**
     * 이 {@code StringList} 인스턴스를 직렬화한다.
     *
     * @serialData 이 리스트의 크기(포함된 문자열의 개수)를 기록한 후({@code int}), 이어서 모든 원소를(각각은 {@code String})
     * 순서대로 기록한다.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(size);

        for (Entry e = head; e != null; e = e.next) {
            s.writeObject(e.data);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int numElements = s.readInt();

        for (int i = 0; i < numElements; i++) {
            add((String) s.readObject());
        }
    }
    
    // ... 나머지 코드 생략 
}
```

## transient
- 해당 객체의 논리적 상태와 무관한 필드라고 확신할 때만 transient 한정자를 생략해야 함 
- transient 필드들은 역직렬화할 때 기본값으로 초기화된다.

## 동기화 메커니즘 
- 객체 전체의 상태를 읽는 메서드에 적용해야 하는 동기화 메커니즘을 직렬화에도 적용해야 함 
- `writeObject` 메서드 안에서 동기화하고 싶을 경우, 클래스의 다른 부분에서 사용하는 락 순서를 똑같이 따라야 함. 
  - 그렇지 않을 경우, 자원 순서 교착상태에 빠질 수 있음 

## serialVersionUID를 명시할 것 
```java
private static final long serialVersionUID = 1L;
```
- 이는 구버전으로 직렬화된 인스턴스들과의 호환성을 끊으려는 경우를 제외하고는 수정해서는 안됨 


## 정리 
- 클래스를 직렬화하기로 했다면 어떤 직렬화 형태를 사용할지 심사숙고해야 함 
- 자바의 기본 직렬화 형태는 객체를 직렬화한 결과가 해당 객체의 논리적 표현에 부합할 때만 사용해야 함 
  - 그렇지 않은 경우 객체를 적절히 설명하는 커스텀 직렬화 형태를 고안해야 함 
- 한 번 공개된 직렬화 필드는 마음대로 제거할 수 없음 
  - 호환성을 유지하기 위해 영원히 지원해야 함 
- 잘못된 직렬화 형태를 선택할 경우, 해당 클래스의 복잡성과 성능에 영구히 부정적인 영향을 남김 
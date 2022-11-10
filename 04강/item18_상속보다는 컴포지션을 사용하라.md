# 아이템 18: 상속보다는 컴포지션을 사용하라

## 상속의 문제점 : 캡슐화를 깨뜨린다.

상위 클래스의 변경에 따라 하위 클래스의 동작에 이상이 생길 수 있다.  
상위 클래스의 API가 바뀌는 것 외에 내부 구현이 바뀌어도 영향을 끼칠 수 있다.

하위 클래스가 상위 클래스가 어떻게 동작하는지 알아야 한다는 데서 캡슐화가 깨졌다는 것이다.

아래의 예제를 보자

```java
class InstrumentedHashSet<E> extends HashSet<E> {
    private int addCount = 0;

    public InstrumentedHashSet() {
    }

    public InstrumentedHashSet(int initCap, float loadFactor) {
        super(initCap, loadFactor);
    }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}
```

잘 만들어진 것 같지만 실제로는 이상하게 동작한다.  
아래를 보자

```java
public static void main(String[] args) {
    InstrumentedHashSet<String> set = new InstrumentedHashSet<>();
    set.addAll(List.of("A", "B", "C"));

    System.out.println("set.getAddCount: " + set.getAddCount());
}
```

```
set.getAddCount: 6
```

결과는 3이 아닌 6을 보여주고 있다.

사실 HashSet의 addAll은 add를 호출하는 방식으로 되어있다.  
이때 불리는 add는 재정의된 add 함수다.

addAll에서 3을 더하고 add를 3번씩 호출해서 3을 더한다.  
중복되어서 결과는 6이 되었다.

## 상속대신 컴포지션 래퍼클래스

기존 클래스를 확장하는 대신 새로운 클래스를 만들고 private 필드로 기존 클래스의 객체를 참조하는 방법이 있다.  
이러한 설계를 컴포지션(composition)이라고 한다.

컴포지션 클래스의 메서드들을 전달메서드(Forwarding method)라고 한다.  
이렇게 하면 기존 클래스의 내부 구현을 몰라도 문제가 없다.

```java
class InstrumentedHashSet<E> {
    private int addCount = 0;
    private final Set<E> set;

    public InstrumentedHashSet(Set<E> set) {
        this.set = set;
    }

    public boolean add(E e) {
        addCount++;
        return set.add(e);
    }

    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return set.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}
```

다만 이렇게 하면 다형성을 이용할 수 없으니 `Guava` 라이브러리의 `ForwardingSet`을 상속해서 쓰거나  
`Set`을 구현해서 사용하자.

### 컴포지션의 문제

콜백 프레임워크에서는 컴포지션을 쓰기 어렵다.
내부 객체가 자기 자신의 참조를 다른 객체로 넘기는 것이 문제가 될 수 있다.  
내부 객체는 자신이 래핑된지 모르기 때문에 래핑이 풀려버린다는 문제가 있다.

아래에 예시와 설명이 있다.  
https://stackoverflow.com/questions/28254116/wrapper-classes-are-not-suited-for-callback-frameworks

## 상속은 언제 써야 할까?

`A is-a B`인 관계일 때만 써야 한다.  
쉽게 말하자면 B는 A여야 한다.

또, 상위 클래스가 확장을 고려해 설계되어 있어야 하고  
API에 결함이 없어야 한다.

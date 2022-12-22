# [Item47]반환 타입으로는 스트림보다 컬렉션이 낫다

## 결론

- 나중에

---

## 자바 7 이전

자바 7까지는 일련의 원소를 반환하는 메서드에서 번환 타입으로 Collection, Set, List와 같은 컬렉션 인터페이스(기본) 혹은 Iterable이나 배열을 사용했다.

- 예외1 - for-each 문에서만 쓰이거나 반환된 원소 시퀀스가 일부 Collection 메서드를 구현할 수 없을때는 Iterable 인터페이스를 사용했다.
- 예외2 - 반환 원소들이 기본 타입이거나 성능에 민감한 상황이라면 배열을 썼다.

## 자바 8 이후

원소 시퀀스를 반환할 때는 당연히 스트림을 사용해야 한다. 단, 스트림은 반복을 지원하지 않는다.

Stream 인터페이스는 Iterable 인터페이스가 정의한 추상 메서드를 전부 포함하고, Iterable 인터페이스가 정의한 방식대로 동작한다.

그럼 왜 for-each가 안되는걸까?

바로 Stream이 Iterable을 extend 하지 않아서이다.

그럼 Stream을 반복하게 하려면 어떻게 해야 하는걸까?

### Stream<E> 를 Iterable<E>로 중개해주는 어댑터

```java
for (ProcessHandle ph : (Iterable<ProcessHandle>) // 형변환
    Process Handle.allProcesses()::iterator){
    // 프로세스를 처리한다.
}
```

Stream을 Iterable로 중계해주는 어댑터를 하용한다면 어떠한 스트림도 for-each 문으로 반복이 가능하다.

자바의 타입 추론이 문맥을 잘 파악하기 때문에 어댑터 메서드 안에서 따로 형변환을 하지 않아도 된다.

### Iterable<E> 를 Stream<E>로 중개해주는 어댑터

```java
public static <E> Stream<E> streamOf(Iterable<E> iterable) {
       return StreamSupport.stream(iterable.spliterator(), false);
}
```

 Collection 인터페이스는 Iterable의 하위 타입이고 stream 메서드도 제공하니 반복과 스트림을 동시에 지원한다.

따라서 원소 시퀀스를 반환하는 공개 API의 반환 타입에는 Collection이나 그 하위 타입을 쓰는게 좋다.

컬렉션을 반환한다는 이유로 덩치 큰 시퀀스를 메모리에 올리는 것은 좋지 않다. 

따라서 반환할 시퀀스가 크지만 표현을 간결하게 할 수 있다면 전용 컬렉션을 구현할수도 있다.


다음의 예제를 보면, 멱집합의 경우 원소의 개수가 n개일때 멱집합의 원소 개수는 2^n개가 되므로, 표준 컬렉션 구현체에 저장하면 위험하다.    

대신 AbstractList를 이용하면 훌륭한 전용 컬렉션을 구현할 수 있다.

입력 집합의 멱집합을 전용 컬렉션에 담아 반환한다.

```java
public class PowerSet {
    public static final <E> Collection<Set<E>> of(Set<E> s) {
        List<E> src = new ArrayList<>(s);
        if (src.size() > 30)
            throw new IllegalArgumentException(
                "집합에 원소가 너무 많습니다(최대 30개).: " + s);
        return new AbstractList<Set<E>>() {
            @Override public int size() {
                // 멱집합의 크기는 2를 원래 집합의 원소 수만큼 거듭제곱 것과 같다.
                return 1 << src.size();
            }

            @Override public boolean contains(Object o) {
                return o instanceof Set && src.containsAll((Set)o);
            }

            @Override public Set<E> get(int index) {
                Set<E> result = new HashSet<>();
                for (int i = 0; index != 0; i++, index >>= 1)
                    if ((index & 1) == 1)
                        result.add(src.get(i));
                return result;
            }
        };
    }
}
```

### 요약
- Stream을 Iterable로, Iterable을 Stream으로 변환하는 과정에서는 어댑터 메서드가 필요하다.
- 어댑터 메서드는 클라이언트 코드를 어수선하게 만들고 더 느리다
- 만약 메서드가 Stream 범위 내에서만 쓰인다면 → Stream을 반환해도 된다.
- 반환된 객체들이 반복문에서만 쓰인다면 → Iterable을 반환해도 된다.
- 하지만 가능한 Collection을 사용하는 것이 좋다.
- Stream, Iterator를 모두 지원할 수 있기 때문이다.
- 원소의 갯수가 많아진다면 전용 컬렉션을 고려해보자

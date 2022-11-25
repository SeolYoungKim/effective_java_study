# 아이템 27: 비검사 경고를 제거하라

제네릭을 사용하면 비검사 경고를 처리해야 할 때가 있다.

보통의 경우, 컴파일러의 경고를 보고 쉽게 해결할 수 있다.

```java
Set<Integer> intSet = new HashSet();
```

위와 같은 코드는 컴파일러가 경고를 내보낸다.  
(로 타입 HastSet을 사용했다는 경고)

```
Raw use of parameterized class 'HashSet'

unchecked assignment: 'java.util.HashSet' to 'java.util.Set<java.lang.Integer>' 
```

자바 7부터 타입 매개변수를 명시하지 않아도 <>를 사용해서 해결 할 수 있다.

```java
Set<Integer> intSet = new HashSet<>();
```

## @SuppressWarnings("unchecked")

경고를 제거하기 어려운 경우도 있다.  
그래도 최대한 경고를 제거하자!

경고가 있어도 충분히 타입 안전성이 보장되고 의도한 대로 동작한다면 `@SuppressWarnings("unchecked")` 에너테이션을 사용하자,
이 에너테이션은 선언에만 달 수 있으며, 사용하면 컴파일러의 경고를 숨길 수 있다.  

`@SuppressWarnings`는 가장 작은 범위에 달아줘야 좋다.  
경고가 날만한 곳에다가 달아주자.

다음은 ArrayList의 toArray 메서드다.

```java
public <T> T[] toArray(T[] a) {
    if (a.length < size)
        // Make a new array of a's runtime type, but my contents:
        return (T[]) Arrays.copyOf(elementData, size, a.getClass());
    System.arraycopy(elementData, 0, a, 0, size);
    if (a.length > size)
        a[size] = null;
    return a;
}
```

ArrayList를 컴파일하면 이 메서드에서 아래와 같은 경고가 나타난다.

```
Unchecked cast: 'java.lang.Object[]' to 'T[]' 
```

오류가 나온 곳은 4번째 줄 형변환이다.

```java
return (T[]) Arrays.copyOf(elementData, size, a.getClass());
```

해결하기 어려우니 `@SuppressWarning` 에너테이션을 달아주자.  
이때, return은 선언이 아니니 에너테이션을 달 수 없다.  
따라서 아래와 같이 지역변수를 선언해야 한다.

```java
@SuppressWarnings("unchecked")
T[] result = (T[]) Arrays.copyOf(elementData, size, a.getClass());
return result;
```
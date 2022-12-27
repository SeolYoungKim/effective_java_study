# 옵셔널 반환은 신중히 하라

자바 8 이전에는 반환값이 없을때 취할 수 있는 선택지가 2가지가 있었다.

1. 예외 던지기
2. null을 반환하기

두가지 다 문제점을 가지고 있다.  

예외를 던지는 선택지는 예외를 생성할때 비용이 많이 든다.  

null을 반환하는 선택지는 비용의 문제에선 자유로울 수 있지만,  
이런 메서드를 호출하는 곳에서 null확인을 해야 한다는 부담이 있다.  
null체크를 하지 않으면 다른 곳에서 `NullPointerException`이 터질 수도 있다.

## Optional<T>

자바 버전 8 부터 `Optional<T>`이 생겼다.  
`Optional<T>`은 null이 아닌 T 참조를 담거나, 아무것도 담지 않을 수 있다.

예외를 던지지 않으면서 `NullPointerException`을 일으키지 않으려면 `Optional<T>`을 반환하면 된다.

값이 수정될 걱정도 하지 않아도 된다. 옵서녈은 불변이다.

옵셔널을 사용한 예제를 보자.

```java
static <E extends Comparable<E>> Optional<E> max(Collection<E> c) {
    if (c.isEmpty()) {
        return Optional.empty();
    }

    E result = null;
    for (E e : c) {
        if (result == null || e.compareTo(result) > 0) {
            result = Objects.requireNonNull(e);
        }
    }

    return Optional.of(result);
}
```

이 메서드는 컬렉션에서 최댓값을 구한다. (컬렉션이 비면 빈 옵셔널을 반환한다)

스트림의 종단연산 중 상당 수가 옵셔널을 반환한다.  
이번엔 스트림을 응용해보자.

```java
static <E extends Comparable<E>> Optional<E> max3(Collection<E> c) {
    return c.stream().max(Comparator.naturalOrder());
}
```

`Stream::max`를 쓰려면 `Comparator`를 전달해줘야 하지만, 코드도 줄어들고 좋다!

## 🤩 옵셔널의 각종 기능들

옵셔널은 내장된 메서드로 유연한 코드 작성을 도와준다.

다음은 옵셔널에 있는 값을 꺼내 쓸때 사용하는 메서드이다.

1. orElse
2. orElseThrow
3. get

```java
// 없으면 "NONE"
String maxVal1 = max(list).orElse("NONE");
// 없으면 NoSuchElementException 예외 발생
// (다른 예외도 물론 가능하다.)
String maxVal2 = max(list).orElseThrow(NoSuchElementException::new);
// 그냥 꺼내기
// 없으면 NoSuchElementException이 발생한다.
String maxVal3 = max(list).get();
```

`orElseThrow`는 Supplier를 인자로 받는 형식으로 값이 있다면 예외 객체를 생성하지 않는다.

`orElse`는 값 객체를 받기 때문에 부담스럽다면 `orElseGet`을 사용하면 Supplier를 인자로 넘길 수 있다.

값을 꺼내는 메서드 외에도 더 다양한 메서드가 준비되어 있다.

1. filter
2. map
3. flatMap
4. isPresent

이 중, map을 응용한 예제를 보자.

```java
List<Integer> nums = List.of(1,2,3);
System.out.println(max(nums)
        .map(String::valueOf)
        .orElse("None"));
```
여기서 `map`은 스트림의 `map`처럼 동작하지는 않는다.  
앞서 말했듯 옵셔널은 불변이기 때문에 새로운 옵셔널을 만들어 반환해 줄 뿐이다.

위 함수들은 스트림에서 옵셔널의 값을 꺼낼때도 좋다.  
다음은 `Stream<Optional<T>>`을 `Stream<T>`로 바꾸는 코드다.

```java
optionals.stream()
    .filter(Optional::isPresent)
    .map(Optional::get);
```

자바 9에서는 옵셔널 자체를 스트림으로 변환할 수 있다.  
`Optional::stream`메서드를 사용하면 된다.

(값이 있다면 하나를 담은 스트림으로, 없다면 빈스트림이 반환된다)

## 💥 주의점

자바의 모든 것이 그렇듯, 옵셔널을 무조건적으로 사용하면 좋지 않다.  

1. 배열, 컬렉션, 스트림, 옵셔널과 같은 컨테이너는 옵셔널로 감싸지 말자.

    *아마 비어있다를 표현할 수 있는 객체를 굳이 감쌀 필요는 없다는 것 같다.*

2. 원시타입 전용도 있으니 박싱된 객체를 쓰지 않아도 된다.
    
    스트림과 같이 OptionalInt, OptionalDouble, OptionalLong을 제공한다.

3. 맵에서 값으로 사용하지 말자.

    맵에서는 null로 해당 엔트리가 비어있다를 표현하는데, 괜한 옵셔널로 혼란을 만들지 말자.

옵서녈은 결과가 없을 수 있으며, 클라이언트가 이 결과를 특별하게 처리해야 할 때 사용하자.
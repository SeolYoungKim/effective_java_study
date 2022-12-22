# 스트림 병렬화는 주의해서 적용하라
자바 8부터 `parallel` 메서드가 추가되서 파이프라인을 병렬로 실행할 수 있는 스트림을 지원했다.  
때문에 동시성 프로그램을 쉽게 작성할 수 있지만 주의해야 할 점이 있다.

예컨데, Stream.iterate이나 limit과 parallel은 잘 어울리지 못한다.

예제가 책에 나와있지만 왜 그렇게 되는지 알기 어려우니 아주 쉬운 예제를 준비했다.

```java
public static void main(String[] args) {
    Stream.iterate(0, i -> i + 1)
            .peek(i -> System.out.println("iterate: " + i))
            .limit(20)
            .peek(i -> System.out.println("limit 20: " + i))
            .limit(10)
            .parallel()
            .forEach(System.out::println);
}
```

## 😊 parallel이 없는 경우
먼저 parallel을 뺀 결과를 먼저 보자.

```
iterate: 0
limit 20: 0
0
iterate: 1
limit 20: 1
1
iterate: 2
limit 20: 2
2
iterate: 3
limit 20: 3
3
iterate: 4
limit 20: 4
4
iterate: 5
limit 20: 5
5
iterate: 6
limit 20: 6
6
iterate: 7
limit 20: 7
7
iterate: 8
limit 20: 8
8
iterate: 9
limit 20: 9
9
```

당연히 0부터 9까지만 출력하고 끝난다.  
충분히 예상할 수 있다.

## 🤔 parallel이 있는 경우

출력이 너무 많아 다 담지 않았다.
```
iterate: 6163
iterate: 28680
iterate: 10253
iterate: 10254
iterate: 10255
iterate: 10256
iterate: 10257
iterate: 10258
iterate: 28681
iterate: 28682
iterate: 28683
iterate: 21515
iterate: 21516
iterate: 21517
iterate: 3083
iterate: 3084
iterate: 3085
iterate: 21518
iterate: 21519
iterate: 21520
iterate: 28684
iterate: 28685
iterate: 28686
iterate: 28687
iterate: 10259
iterate: 28688
iterate: 28689
iterate: 28690
iterate: 28691
iterate: 21521
iterate: 21522
iterate: 21523
iterate: 3086
iterate: 3087
iterate: 3088
iterate: 3089
iterate: 3090
iterate: 3091
limit 20: 17
limit 20: 16
limit 20: 9
limit 20: 19
limit 20: 10
limit 20: 12
limit 20: 18
limit 20: 8
limit 20: 0
limit 20: 11
limit 20: 3
limit 20: 7
limit 20: 13
limit 20: 6
limit 20: 14
limit 20: 5
limit 20: 4
limit 20: 1
limit 20: 2
limit 20: 15
8
9
2
4
6
1
5
0
3
7
```

왜 이렇게 많이 출력하는 걸까?  
이유를 찾으러 코드를 훑어봤지만 이해하지 못했다. 😭

어쨌든,  
`Stream.iterate`는 불필요하게 많이 연산하는 모습을 볼 수 있고,  
`.limit(20)`은 요청한 대로 20개만 제한하고 후의 `.limit(10)`를 알지 못하는 것 같다.  
스트림 단위로 병렬연산을 실행하는 것 같다.

## 책에 있는 예제

파이프라인은 아래와 같이 이루어져 있다.

> 소수를 만드는 Stream  
> -> 2^소수 - 1  
> -> 소수를 거르는 필터  
> -> 10개 제한  
> -> 출력  

```java
public static void main(String[] args) {
    primes().map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
            .filter(mersenne -> mersenne.isProbablePrime(50))
            .limit(10)
            .parallel()
            .forEach(System.out::println);
}

static Stream<BigInteger> primes() {
    return Stream.iterate(TWO, BigInteger::nextProbablePrime);
}
```

이 예제가 위에서 다뤘던 예제에 대입해 생각해본다면,  
첫번째 스트림(소수를 만드는 스트림: `primes()`)에서 소수를 대량으로 만들고 있을 것이다. 

소수는 만드는 비용이 크고, 다음 소수를 만들때마다 2배씩 커지기 때문에 겁나 오래걸릴 것이다. 😓

## 🧐 그럼 어떻게 병렬화를 쓸까?

### 💬 병렬화에 어울리는 자료구조

스트림의 소스가 `ArrayList`, `HashMap`, `HashSet`, `ConcurrentHashMap`, `배열`, `int 범위`, `long 범위`일 때 병렬화의 효과가 제일 좋다고 한다.

나누기 좋고, 그렇기 때문에 일을 다수의 스레드에 분배하기에 좋다는 특징이 있다.

일을 나누는 작업은 `Spliterator`가 담당하고, `Stream`이나 `Iterable`의 spliterator의 매서드에서 얻을 수 있다고 한다.

위 자료구조의 중요한 공통점은 참조 지역성이 뛰어나다는 점이다.

참조 지역성은 메모리에 적재된 객체들의 위치가 가깝다는 의미로 캐시 Hit rate를 높이기 위해서 중요하다.

#### 📝 캐시 Hit rate?

![image](https://user-images.githubusercontent.com/7973448/208909674-4f17c121-ad34-439c-852a-74c52b6188e0.png)

찾고자 하는 블록이 캐시에 있는지에 대한 비율이다.

자세한 내용은 아래를 참고..

> 배열에서 캐시 참조 지역성이 왜 중요한가?
> https://stackoverflow.com/questions/12065774/why-does-cache-locality-matter-for-array-performance

> 캐시에 대한 정리
> https://parksb.github.io/article/29.html

### 💬 병렬화에 어울리는 종단 연산

스트림 파이프라인의 종단 연산도 병렬 효율에 영향을 준다.

축소는 이에 가장 적합하다.

축소(reduction) 연산은 파이프라인에서 만들어진 값을 하나로 합치는 작업으로, `reduce`, `min`, `max`, `count`, `sum`등이 있다.

`anyMatch`, `allMatch`, `noneMatch`같은 연산도 병렬화에 적합하다.

반면에 `collect`메서드는 컬렉션들을 합치는 부담이 크기 때문에 병렬화에 적합하지 않다.

### 💬 극한의 효율 

직접 구현한 Stream, Iterable, Collection을 병렬화에 적합하게 구현하고 싶다면 spliterator 메서드를 재정의 하면 된다.

어떻게 하는지는 상당히 난이도 있는 일이고 책에서 다루지 않는다. (알아야 할까..?)

## 🫤 기타

### 💬 안전 실패(safety failure)

스트림을 잘못 병렬화 하면 성능이 나빠지고 예상치 못한 동작이 발생할 수 있다.

결과가 잘못되었거나 오동작 하는 것을 안전 실패(safety failure)라고 한다.

안전 실패는 mapper, filter 등, 직접 만든 함수에서 제대로 동작하지 않으면 일어나는데, Stream 명세에 함수에 대한 규약이 적혀있다.

예컨데 `reduce` 메서드의 자바독을 보면 다음과 같이 적혀있다.

> accumulator – an associative, non-interfering, stateless function for incorporating an additional element into a result  
> 
> combiner – an associative, non-interfering, stateless function for combining two values, which must be compatible with the accumulator function

accumulator와 combiner는 다음과 같은 조건이 걸려있다.
1. 결합법칙을 만족해야 한다  
  `(a op b) op c == a op (b op c)`
2. 간섭 받지 않아야 한다
  `(파이프 라인이 수행되는 동안 데이터 소스가 변경되면 안된다.)`
3. 상태를 갖지 않아야 한다

### 💬 병렬화를 하면서 출력을 순서대로 하고 싶을때

순서대로 출력하고 싶다면 종단 연산에 `forEach`대신 `forEachOrdered`을 사용하면 된다.  

### 💬 병렬화는 언제 쓸까?

적은 코드에선 병렬화를 쓰는 이점이 없을 수 있다. 오히려 나쁠 수 있다.

그렇다면 병렬화를 언제 써야 할까?  

책에서는 `스트림 안의 원소 수 X 원소당 수행되는 코드 중 수`가 수십만 이상이여야 성능 상의 효과가 있다고 한다.

## 정리

병렬화는 항상 좋은 것이 아니다.  
코드가 길거나 원소가 많을때 사용하고 사용하더라도 성능 지표를 확인해야 한다.
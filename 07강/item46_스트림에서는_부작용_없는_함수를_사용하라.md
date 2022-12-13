### **이펙티브 자바 스터디 8주차**

#### **개인 주제 |** 아이템46. 스트림에서는 부작용 없는 함수를 사용하라 

## 스트림 패러다임의 핵심 
- 핵심은 계산을 일련의 변환(transformation)으로 재구성하는 부분이다 
- 각 변환 단게는 가능한 한 이전 단계의 결과를 받아 처리하는 "순수 함수"여야 함 

## 순수 함수?
- 오직 입력만이 결과에 영향을 주는 함수 
- 가변 상태 참조 X
- 함수 스스로도 다른 상태를 변경하지 않음 
> 위와 같이 하기 위해서는, 스트림 연산에 건네는 함수 객체는 모두 부작용이 없어야 한다.

## 스트림의 forEach
```java
/**
 * 스트림을 가장한 반복적 코드다 (스트림 답지 못한 예시)
 * 이는 스트림 API의 이점을 살리지 못하여, 같은 기능의 반복적 코드보다 길고, 읽기 어렵고, 유지보수에도 좋지 않다.
 */
public class BadCase {
    public static void main(String[] args) {
        Map<String, Long> freq = new HashMap<>();
        try (Stream<String> words = new Scanner(System.in).tokens()) {
            words.forEach(word -> { //forEach에서 외부 상태를 수정하는 람다를 실행한다 -> 문제가 생긴다.
                freq.merge(word.toLowerCase(), 1L, Long::sum);  // 람다가 상태를 수정하는 것은 나쁜 코드다. (연산 결과를 보여주는 일 이상의 것을 한다.)
            });
        }
    }
}

/**
 * forEach를 사용하면 안되는 경우  
 * 출처 : https://tecoble.techcourse.co.kr/post/2020-05-14-foreach-vs-forloop/
 */
//for-loop로 짠 경우
for (int i = 0; i < 100; i++) {
    if (i > 50) {
        break;  // 확실히 종료된다.
    }
    System.out.println(i);
}

// 아래의 경우는 모든 요소를 순회해야만 연산이 끝난다. 매우 손해다.
IntStream.range(1, 100).forEach(i -> {
    if (i > 50) {
        return; 
    }
    System.out.println(i);
});
```
- 종단 연산 중 기능이 가장 적고, 가장 "덜" 스트림 답다고 표현되어 있다. 
  - 대놓고 반복적이라서 병렬화 할 수 없다고 한다.
- 따라서, `forEach`연산은 스트림 계산 결과를 보고할 때만 사용하고, 계산하는 데는 쓰지 말자.
  - 가끔은 스트림 계산 결과를 기존 컬렉션에 추가하는 등의 다른 용도로 사용할 수는 있다. 

> 왜 스트림답지 않은걸까?
> Stream은 lazy evaluation이 가능하다. 중간 연산으로 필요한 데이터를 걸러내고, 마지막에 종단 연산에서 필요한 연산만 수행할 수 있다는 장점이 있다.  
> 
>
> 하지만, 예시에서의 forEach는 모든 요소를 순회한다. 이를 스트림답지 못하다고 표현한것 같다.


```java
/**
 * 스트림을 스트림 답게 사용한 예시 
 */
public class GoodCase {
    public static void main(String[] args) {
        Map<String, Long> freq;
        try (Stream<String> words = new Scanner(System.in).tokens()) {
            freq = words.collect(
                    groupingBy(String::toLowerCase, counting())
            );
        }
    }
}
```

## Collector
- `java.util.stream.Collectors`


책에 Collectors에 관한 기본 설명이 매우 부족한데, 설명할 것이 너무 많기 때문인 것 같다. 간단히 짚고 넘어가보도록 하자.
- 스트림은 `collect()`메서드를 이용해 값들을 "누적"한다.
  - 값을 수집하는 것은 여러 방법이 있는데, 방법 대부분을 `Collectors`가 제공해준다. 예를 들면 아래와 같다.
    - `toList` : 스트림 요소들의 값을 `List`에 누적한다.
    - `toSet` : 스트림 요소들의 값을 `Set`에 누적한다.
    - `toMap` : 스트림 요소들의 값을 `Map`에 누적한다.
    - `toCollection(collectionFactory)` : 스트림의 값을 프로그래머가 지정한 `Collection`에 누적한다.

```java
// toList
List<String> topTen = freq.keySet().stream()
                .sorted(comparing(freq::get).reversed())
                .limit(10)
                .collect(toList());  // 값을 List에 누적한다.
```

Collectors의 메서드는 자바 10 기준 총 43개라고 한다.   
그 중에서도, `Map`을 만드는 것과 관련된 Collector가 가장 많다고 한다. 가장 기본적인 `toMap`부터 알아보도록 하자 

## Map과 관련된 Collector
### toMap : 스트림 요소의 값을 `Map`에 누적하는 Collector 
```java
public enum Command {
    START("S"),
    PAUSE("P"),
    RESUME("R"),
    QUIT("Q"),;

    //각 hotkey를 key로, Command를 value로 하는 map 만들기
    private static final Map<String, Command> COMMAND_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(  // 값을 Map에 누적한다. 
                    command -> command.hotkey,  // keyMapper : Function이 들어간다. 여기서는 key로 command의 hotkey를 지정하였다.
                    command -> command  // valueMapper Function이 들어간다.
            ));

    public static Command of(String hotkey) {
        return COMMAND_MAP.get(hotkey);
    }

    private final String hotkey;

    Command(String hotkey) {
        this.hotkey = hotkey;
    }
}
```
가장 간단한 `toMap(keyMapper, valueMapper)`사용 예제이다.
- keyMapper : 스트림 원소를 키에 매핑하는 `Function<T, R>`이 온다.
- valueMapper : 스트림 원소를 값에 매핑하는 `Function<T, R>`이 온다.
- `toMap()`의 keyMapper와 valueMapper를 이용해서, collect() 메서드가 스트림의 요소들을 `Map`에 누적시킨다.

toMap은 스트림의 각 원소가 고유한 키에 매핑되어 있을 때 적합하다.
  - 스트림 원소 다수가 같은 키를 사용할 경우, `IllegalStateException`을 던지며 종료된다.

자매품으로 `toUnmodifiableMap`이 있다. 이는 변경 불가능한 Map을 반환한다.


### 파라미터가 3개인 toMap 예제
나만의 예제를 작성 해보려고 한시간을 고민하였지만, 아직 스트림에 대한 지식이 부족한 탓인지 마땅한 예제가 떠오르지 않아 책에 나온 예제를 그대로 사용하였다.  
예제가 너무 길었기 때문에 대부분 생략하였다. 클래스 구성 자체보다는 toMap에 파라미터 3개를 썼을 때 발생하는 일들이 더 중요하므로, 해당 내용을 주로 다루어 보겠다.
```java
public class Ex46_5 {
    public static void main(String[] args) {
        Map<Artist, Album> error = ALBUMS.stream()
                .collect(toMap(Album::artist, album -> album));  // 이 경우에는, key가 중복되어 예외가 발생한다.
      
        Map<Artist, Album> topHits = ALBUMS.stream()
                .collect(toMap(Album::artist, album -> album, maxBy(Comparator.comparing(Album::streamingCounts))));

        System.out.println(topHits);
    }

    private static final List<Album> ALBUMS = List.of(
            // Album 리스트 구현 
    );

    static class Album {
        private final String name;
        private final Artist artist;
        private final long streamingCount;

        //생성자, getter, equals와 hashCode
    }


    static class Artist {
        private final String name;
        //생성자, getter, equals와 hashCode
    }
}
```
**예제 설명**
- Album 리스트를 stream으로 변환한다. 
- Album에 할당된 Artist를 key로 하고 Album 자체(자기 자신)를 value로 하도록 toMap을 구성한다.

위와 같이 구성할 경우 문제가 하나 있는데, key가 중복될 수 있다는 것이다. 만약 key가 중복될 경우, 아래와 같은 예외가 발생하게 된다. 
- 중복되지 않을 경우에는 에외가 발생하지 않는다. 하지만 이는 런타임 예외에 해당하므로, key가 한개라도 중복될 가능성이 있다면 주의해야 할 것 같다.

```text
Exception in thread "main" java.lang.IllegalStateException: Duplicate key BLACK_PINK (attempted merging values 뚜두뚜두 and 붐바야)
	at java.base/java.util.stream.Collectors.duplicateKeyException(Collectors.java:133)
	... 대충 긴 예외 메세지 ... 
```

위와 같이, 키가 중복될 수 있는 경우 `toMap`에 파라미터를 3개 넘겨줌으로써 해결 해야한다. 첫번째와 두번째 파라미터는 동일하다.
- 첫 번째 파라미터 : `keyMapper`
- 두 번째 파라미터 : `valueMapper`
- **세 번째 파라미터 : `BinaryOperator`**

세 번째 파라미터인 `BinaryOperator`는 `merge`함수로 사용되는데, 할당된 `BinaryOperator`는 아래와 같이 동작한다. 
```java
default V merge(K key, V value,
        BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    Objects.requireNonNull(remappingFunction);
    Objects.requireNonNull(value);
    V oldValue = get(key);  // key로 value를 조회 
    V newValue = (oldValue == null) ? value :  // oldValue가 없으면 value를 newValue로 사용 
               remappingFunction.apply(oldValue, value);  // 없으면 remappingFunction 사용 
    if (newValue == null) {
        remove(key);
    } else {
        put(key, newValue);
    }
    return newValue;
}
```
간단히 메커니즘을 살펴보겠다.
- 받아온 `key`로 Map에서 `oldValue`를 조회한다.
- 받아온 `value`로 `newValue`를 구한다.
  - `oldValue`가 없으면, 할당된 key가 없으므로, 충돌 위험이 없기 때문에 `value`를 `newValue`로 사용한다.
  - 있으면, 충돌 위험이 있으므로 `remappingFunction`을 사용하여 `newValue`로 사용할 값을 구한다.
    - `maxBy(comparing(Album::streamingCounts))`를 사용한 경우 -> `oldValue`와 `value`를 비교하여 큰 값을 `newValue`로 사용
    - `(oldVal, newVal) -> newVal`를 사용한 경우 -> `value`를 그대로 `newValue`로 사용
- 만약 `newValue`가 `null`이면 `key`를 Map에서 지워버린다.
- `null`이 아니면, `key`에 `newValue`를 매핑한다.



위 예제에서는 `BinaryOperator.maxBy(comparing(Album::streamingCounts))`가 사용되었다.  
- key(Artist)가 중복될 경우, 앨범의 스트리밍 횟수를 비교하여 최댓값인 앨범을 value로 사용하겠다는 의미다.
- 같은 키를 공유하는 값들은 이를 이용해서 기존 값에 합쳐질 수 있다. 즉, 위 예제의 첫 번째 스트림에서 발생한 `IllegalStateException`을 방지할 수 있다.


세 번째 파라미터를 넣은 뒤의 결과는 다음과 같다. 
- 각 Artist 별로 가장 조회수가 많이 나온 곡들이 value로 할당되었다.
```text
{BTS=작은 것들을 위한 시, PSY=강남스타일, BLACK_PINK=뚜두뚜두, TWICE=LIKEY}
```


### toMap은 충돌이 나면 마지막 값을 취하는(last-write-wins) 수집기를 만들 수도 있다.
말이 뭔가 어려워 보이지만, 그냥 `BinaryOperator`에 아래와 같은 람다식을 넘겨주면 된다는 뜻이다. (메커니즘은 위에서 설명했다.)

아래와 같이 구현할 경우, key에는 가장 최근의 값이 매핑된다. 
```java
Map<Artist, Album> lastWriteWins = ALBUMS.stream()
        .collect(toMap(Album::artist, album -> album, (oldVal, newVal) -> newVal));

// 결과 
// {BTS=다이너마이트, PSY=GENTLEMAN, BLACK_PINK=Kill This Love, TWICE=FANCY}
```


### toMap의 네 번째 파라미터 "MapFactory"
이는 특정한 맵 구현체를 지정할 때 사용한다. 아까 위에서 본 `Command` enum을 예시에 재사용 해보도록 하겠다.  
> 이는 MapFactory를 어떻게 사용하는지 알려드리기 위해 억지로 구현한 무의미한 예제 코드이니, 따라하지는 마세요..!!
> - 조슈아 선생님이 쓰지 말라는 ordinal()도 써버린 아주 안좋은 예시입니다 ㅎㅎ;; 
```java
// 주의: 그냥 어떻게 쓰는지 보여드리려고 작성한 무의미한 코드입니다!!!
private static final EnumMap<Command, Integer> COMMAND_ORDINAL = Arrays.stream(values())
            .collect(toMap(
                    command -> command,
                    command -> command.ordinal(),
                    (oldVal, newVal) -> newVal, 
                    () -> new EnumMap<>(Command.class)
            ));
```
- 마지막 파라미터에 `Supplier`를 넘겨줌으로써 특정 맵 구현체를 지정 해줄 수 있다. 
  - 하지만, 세번째 파라미터를 건너 뛸 수 있는 방법은 없기에, groupingBy와 같은 더 좋은 메서드를 이용하도록 하자.


### toConcurrentHashMap
API 문서에 따르면, toMap은 반환된 Map 의 유형, 가변성, 직렬화 가능성 또는 스레드 안전성에 대한 보장은 없다고 하며, 안전성이 필요할 경우 `toConcurrentHashMap`을 사용할 것을 권고하고 있다.
- 사용 방법은 toMap과 동일하다. 병렬성을 보장할 뿐. 병렬성이 필요하다면 해당 메서드를 사용하도록 하자 


### groupingBy
해당 메서드는 입력으로 분류 함수(`classifier`)를 받고, 출력으로는 원소들을 카테고리별로 모아 놓은 맵을 담은 Collector를 반환한다.
- classifier는 입력받은 원소가 속하는 카테고리를 반환한다. (classifier는 `Function`이다)
- 반환된 카테고리는 해당 원소의 맵 키로 쓰인다.


`groupingBy`에는 `classifier` 한개만 넘길 수 있다. 이에 대한 예제는 아래와 같다.
```java
Map<Artist, List<Album>> artistListMap = ALBUMS.stream()
        .collect(groupingBy(Album::artist));  //classifier만 넘겼다.
```

위 예제는 Album 리스트를 Artist 기준으로 분류한 맵이다. 단순히 `classifier`만 넘길 경우, `classifier`기준으로 `List<Album>`이 value에 할당된다.
```text
{BTS=[작은 것들을 위한 시, 다이너마이트], PSY=[강남스타일, GENTLEMAN], BLACK_PINK=[뚜두뚜두, 붐바야, Kill This Love], TWICE=[LIKEY, FANCY]}
```


`groupingBy`는 `List`외에도 다른 값을 갖는 맵을 생성하게 할 수 있다. 이 때는 `classifier`와 함께 다운스트림(downstream) 수집기도 명시해야 한다.
```java
// Set으로 변환
Map<Artist, Set<Album>> setMap = ALBUMS.stream()
                .collect(groupingBy(Album::artist, toSet()));

// Album을 Artist를 기준으로 분류 후, Album의 streamingCounts를 모두 더한 결과를 Map으로 변환 
Map<Artist, Long> accumulatedStreamingCounts = ALBUMS.stream()
                .collect(groupingBy(Album::artist, summingLong(Album::streamingCounts)));
```

위 두 번째 예제의 결과는 다음과 같다.
```text
{BTS=3070000000, PSY=5800000000, BLACK_PINK=4800000000, TWICE=1030000000}
```


`groupingBy`도 MapFactory를 지정할 수 있다. 
- 이는 점층적 인수 목록 패턴에 어긋난다.
- `MapFactory`매개변수가 `downStream` 매개변수보다 앞에 놓이기 때문이다.

사용 예제
> 아래의 예제는 안되는 예제입니다!!! 그냥 해당 위치에 MapFactory가 들어갈 수 있음을 나타낸 예제일 뿐입니다.
```java
// 
TreeMap<Artist, Set<Album>> treeMap = ALBUMS.stream()
        .collect(groupingBy(Album::artist, TreeMap::new, toSet()));
```

또한, 병렬성을 제공하는 `groupingByConcurrent`메서드도 있다.


## partitioningBy
groupingBy의 사촌격인 메서드다. 이는 `Boolean`을 key로 하는 Map을 반환한다. 말 그대로 true, false로 "파티셔닝"해주는 역할을 한다. 하지만, 자주 쓰이지는 않는 듯 하다.

다음의 예제를 보자.
```java
// groupingBy vs partitioningBy
Map<Boolean, Long> count = IntStream.rangeClosed(0, 100)
        .boxed()
        .collect(groupingBy(i -> i % 2 == 0, counting()));

Map<Boolean, Long> partitioning = IntStream.rangeClosed(0, 100)
        .boxed()
        .collect(partitioningBy(i -> i % 2 == 0, counting()));
```
```text
// 실행 결과 
{false=50, true=51}
{false=50, true=51}
```


참고로, 위 예제에서 사용한 `counting()`메서드는 `Collector`를 반환한다. 이는 **다운 스트림 Collector**전용으로 만들어진 메서드다.
- `Stream`에는 `count()`메서드가 있기 때문에, `collect(counting())`처럼 사용할 일은 전혀 없을 것이기 때문에, 다운 스트림에만 사용하게 될 것이다. 


`counting()`과 같은 메서드들이 여러개 더 있는데, 다 설명하면 길어질 것 같으니 예제는 아래의 세개만 확인 해보도록 하자.
```java
Map<Artist, Double> average = ALBUMS.stream()
        .collect(groupingBy(Album::artist, averagingLong(Album::streamingCounts)));
System.out.println("average = " + average);

Map<Artist, LongSummaryStatistics> summary = ALBUMS.stream()
        .collect(groupingBy(Album::artist, summarizingLong(Album::streamingCounts)));
System.out.println("summary = " + summary);

Map<Artist, List<Album>> filtering = ALBUMS.stream()
        .collect(groupingBy(Album::artist,
                filtering(album -> album.streamingCounts() <= 1_500_000_000L, toList())));
System.out.println("filtering = " + filtering);
```
```text
average = {BTS=1.535E9, PSY=2.9E9, BLACK_PINK=1.6E9, TWICE=5.15E8}
summary = {BTS=LongSummaryStatistics{count=2, sum=3070000000, min=1520000000, average=1535000000.000000, max=1550000000}, PSY=LongSummaryStatistics{count=2, sum=5800000000, min=1400000000, average=2900000000.000000, max=4400000000}, BLACK_PINK=LongSummaryStatistics{count=3, sum=4800000000, min=1400000000, average=1600000000.000000, max=1800000000}, TWICE=LongSummaryStatistics{count=2, sum=1030000000, min=500000000, average=515000000.000000, max=530000000}}
filtering = {BTS=[], PSY=[GENTLEMAN], BLACK_PINK=[붐바야], TWICE=[LIKEY, FANCY]} 
```
- 이 외에도, 다중 정의된 `reducing`메서드들과, `mapping`, `flatMapping`, `collectingAndThen` 메서드가 있다.


## minBy & maxBy
- 이는 `Collectors`에 정의되어 있으나, "수집/누적"과는 관련이 없다.
- 스트림에서 가장 작은 값, 혹은 가장 큰 값을 찾아 반환할 뿐이다.

아래와 같이 쓰이긴 하지만, 더 간략하게 stream의 min을 사용해도 된다.
```java
// 아래 둘은 같다.
ALBUMS.stream().collect(Collectors.minBy(Comparator.comparingLong(Album::streamingCounts)));
ALBUMS.stream().min(Comparator.comparingLong(Album::streamingCounts))
```

## joining
- 해당 메서드는 문자열 등의 `CharSequence` 인스턴스의 스트림에만 적용할 수 있다. 
- 이 중 매개변수가 없는 joining은 단순히 원소들을 연결하는 수집기를 반환한다.
```java
// 단순한 joining
String albumName = ALBUMS.stream().map(Album::name).collect(Collectors.joining());

//결과
//albumName = 강남스타일뚜두뚜두붐바야Kill This Love작은 것들을 위한 시다이너마이트LIKEYFANCYGENTLEMAN
```

단순한 joining은 결과를 알아보기 어려울 수 있다. 이 때, delimeter값을 넘겨 줌으로써 구분자를 넣어 joining을 수행할 수 있다.
```java
//delimeter가 설정된 joining
String albumName = ALBUMS.stream().map(Album::name).collect(Collectors.joining(", "));

//결과
//albumName = 강남스타일, 뚜두뚜두, 붐바야, Kill This Love, 작은 것들을 위한 시, 다이너마이트, LIKEY, FANCY, GENTLEMAN
```

더 나아가, prefix와 suffix도 지정 해줄 수 있다.
```java
//delimeter, prefix, suffix가 설정된 joining
String albumName = ALBUMS.stream().map(Album::name).collect(Collectors.joining(", ", "[ ", " ]"));

//결과
//albumName = [ 강남스타일, 뚜두뚜두, 붐바야, Kill This Love, 작은 것들을 위한 시, 다이너마이트, LIKEY, FANCY, GENTLEMAN ]
```

---

## 정리
> 스트림 파이프라인 프로그래밍의 핵심
> - 부작용 없는 함수 객체에 있다.
> - 스트림 뿐만 아니라, 스트림 관련 객체에 건네지는 모든 함수 객체가 부작용이 없어야 한다.
> - forEach는 계산 결과를 보고할 때만 사용하라
> - 스트림을 올바르게 사용하기 위해 Collector를 잘 알아두어야 한다.

---

## 이번 단원에 대한 생각
사실, **부작용 없는 함수 객체**라는 말에 **불변 객체를 사용해야 한다, 변하는 값을 사용하면 안된다, 혹은 값을 변화시키면 안된다**에 관련된 깊은 내용들이 나올 줄 알았습니다.

생각보다 더 기본적인 부분들을 짚어주어서 Stream에 대한 기본기 학습을 위해 괜찮은 단원이었던 것 같습니다.

특히, Collectors에 관련된 내용이 아주 많았는데, 이를 이용하면 정말 다양한 일들을 할 수있는 것을 보고, 제가 사용하던 스트림은 정말 "극히 일부"였다는 생각이 들었습니다.

좀 더 깊은 이해를 하기 위해, 소스코드를 파헤쳐가며 공부 해보았지만 아주 개략적인 흐름만 알 수 있었을 뿐, 더 자세한 내용은 디버깅을 해도 알기가 어려웠습니다.

스트림 API는 앞으로도 더 많은 공부가 필요한 영역인 것 같네요..! 





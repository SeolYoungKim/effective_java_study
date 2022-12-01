# 아이템 32: 제네릭과 가변인수를 함께 쓸 때는 신중하라

## 가변인수는 배열을 자동으로 생성해준다

가변인수를 사용하면 인수를 담기 위한 배열을 하나 만들어서 사용한다.  
가변인수를 출력하는 예제를 보자.
```java
public static void main(String[] args) {
    printStrings("123", "234", "345");
}

static void printStrings(String... strings) {
    System.out.println(strings);
}
```
결과는 스트링 배열(`[Ljava.lang.String;@531be3c5`)이 나온다.

## 가변인수로 제네릭 배열 생성하기

```java
static void heapPollution(List<String>... stringLists) {
    List<Integer> integers = List.of(1, 2, 3);
    Object[] objects = stringLists;
    // List<String>[]에 List<Integer>를 넣은 꼴!
    objects[0] = integers;
    // ClassCastException!
    String s = stringLists[0].get(0);
}
````

위 함수는 호출하면 `String s = strings[0].get(0)`에서 런타임 오류가 발생한다.

제네릭 배열은 프로그래머가 직접 생성할 수 없지만,  
가변인수를 사용하면 간접적으로 생성할 수 있게 해준다.  
stringLists를 출력해보면 `[Ljava.util.List;@52af6cff`가 나오는데,  
사실 로 타입 List 배열이다.

#### 💬 코드 설명

배열의 특성으로 `List[]`은 `Object[]`의 하위타입이니 아래와 같이 Object[] 배열로 쓸수 있다.  
`Object[] objects = stringLists;`

`Object[]`이니 `List`인 integers 변수도 넣을 수 있다.  
`objects[0] = integers;`  

이때 런타임 오류는 당연히 없다.  
stringLists가 `List[]`이기 때문이다.

#### 💥 힙 오염

사실 여기서 문제가 발생해야 하는데, 타입 매개변수가 사라져서 알 수 없다.  
이렇게 *다른 매개변수화 타입의 변수 간, 매개변수화 타입의 변수와 매개변수화 타입이 아닌 변수가 서로를 참조하는 문제*를 **힙 오염: Heap Pollution**이라고 한다. 

#### ⚠️ 경고
다행히도 힙 오염 가능성이 있을 때, 컴파일러가 경고를 내보낸다.  
```
Possible heap pollution from parameterized vararg type
```

## 안전한 경우?

힙 오염이 발생하는 데에도 불구하고 궂이 허용하는 이유가 무엇일까?  
자바 라이브러리에서 유용하게 쓰이기 때문이다.  
물론 라이브러리에서 제공하는 메서드는 타입 안전하다.

```java
Arrays.asList(T... a)
Collections.addAll(Collection<? super T> c
T... elements), List.of(E... e)

...
```

### @SafeVarargs

제네릭과 가변인수를 함께쓰는 메서드를 사용해야 하고,  
타입 안전해서 경고를 없애고 싶다면 `@SafeVarargs`에너테이션을 사용하면 된다.  
(`@SafeVarargs`에너테이션은 재정의할 수 없는 메서드에만 사용해야 한다.)

## 제네릭과 가변인수를 함께 써야 할 때 주의할 점!

```java
static <T> T[] _toArray(T... args) {
    return args;
}

static <T> T[] toArray(T a, T b) {
    return _toArray(a, b);
}
```

간단하게 타입이 같은 a, b를 받아 배열로 만들어주는 메서드를 작성했다.

```java
public static void main(String[] args) {
    // java.lang.ClassCastException: 
    // class [Ljava.lang.Object; cannot be cast to 
    // class [Ljava.lang.String; ([Ljava.lang.Object;
    String[] strings = toArray("123", "234");
}
```

아무문제가 없을 것 같지만 오류가 발생한다.  

### 🤔 오류가 발생하는 이유

```java
static <T> T[] toArray(T a, T b) {
    T[] ts = _toArray(a, b);
    // toArray(123,234) : [Ljava.lang.Object;@27ddd392
    System.out.println("toArray("+a+","+b+") : " + ts);
    return ts;
}

static <T> T[] _toArray(T... args) {
    // _toArray : [Ljava.lang.Object;@27ddd392
    System.out.println("_toArray : " + args);
    return args;
}
```

출력해보면 알 수 있다.  
args는 Object 배열이다.  

_toArray의 반환값도 `Object[]`이고,  
그걸 그대로 반환한 toArray의 반환값도 `Object[]`이다.

main으로 가면 `String[]` 변수로 반환값을 저장한다.  
`String[] strings = toArray("123", "234");`  
자동으로 `Object[] -> String[]` 타입 변환이 이루어져야 하는데  
Object[]는 String[]의 하위타입이 아니니 오류가 나오는 것이다.

결국 가변인자는 Object[]로 생성되기 때문에...  
**제네릭 매개변수 배열을 다른 메서드에서 접근하지 못하게 해야 한다.**

## 제네릭을 써야 한다면 가변인자 대신 List를 사용하는 것도 방법!
"아이템28 배열보다는 리스트를 사용하라" 참고..
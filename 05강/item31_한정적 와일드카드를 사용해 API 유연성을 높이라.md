# `List<String>` 은 `List<Object>`의 하위타입이 아니다.

`List<String>` 는 `List<Object>` 가 하는 일을 제대로 수행하지 못하니 리스코프 치환 원칙에 어긋난다.
<br/>

`Stack.java`
```java
public class Stack<E> {

    public Stack(){};

    private void push(E e) {
        System.out.println("push method run, item = " + e);
    }

    public void pushAll(Iterable<E> src) {
        for(E e : src) push(e);
    }
}
```
`Main.java`
```java
public static void main(String[] args) {
    Stack<Number> numberStack = new Stack<>();
    List<Integer> list = ... // {1, 2, 3, 4}
    Iterable<Integer> integers = (Iterable<Integer>) list;
    numberStack.pushAll(integers); // 매개변수화 타입이 불공변
}
```
위의 코드와 같이 `Stack<Number>`로 클래스를 선언한 뒤, Number의 하위타입인 Integer의 List를 매개변수로 넣으면 잘 작동할것 같지만
아래와 같은 오류가 발생하는데...


java: incompatible types: java.lang.Iterable<java.lang.Integer> cannot be converted to java.lang.Iterable<java.lang.Number>

책에서는 매개변수화 타입이 불공변이기 때문이라고 말한다.


### 한정적 와일드카드 타입

- 와일드카드?
	- Unbound WildCard -> <?>
	- Upper Bounded Wildcard -> <? extends T> (상한 경계)
	- Lower Bounded Wildcard -> <? super T> (하한 경계)

위 `Stack.java`의 pushAll 메소드는 E (Number)의 Iterable이 아닌 E의 하위 타입 (Integer)의 Iterable 이어야 한다.

즉 위의 코드는 아래와 같이 변경되면 문제없이 돌아간다.

```java
public void pushAll(Iterable<? extends E> src) {
    for(E e : src) push(e);
}
```

이번엔 popAll 이라는 메소드를 만들어보자.

`Stack.java`
```java
public void popAll(Collection<E> dst) {
    while (!isEmpty())
		dst.add(pop());
}
```
`Main.java`
```java
Stack<Number> numberStack = new Stack<>();
Collection<Object> objects = ...;
numberStack.popAll(objects);
```

위 코드도 문제없어 보이지만, 컴파일 오류가 발생한다.

**`Collection<Object>` 는 `Collection<Number>`의 하위타입이 아니다.**

여기서는 E의 Collection (Number) 가 아닌 E의 상위타입 (Object) 의 Collection 이어야 한다.

수정해보면 
`Stack.java`
```java
public void popAll(Collection<? super E> dst) {
    while (!isEmpty())
		dst.add(pop());
}
```
이제 원하는 대로 동작하게 된다.

한정적 와일드카드는 Upper Bounded Wildcard와 Lower Bounded Wildcard가 있는데,
- Upper Bounded Wildcard (상한 경계)
	- <? extends E>
	- 자신과 자신의 하위타입
	- 아래 그림에서 <? extends Student>는 Student, StudentDeveloper
- Lower Bounded Wildcard (하한 경계)
	- <? super E>
	- 자신과 자신의 상위타입
	- 아래 그림에서 <? super Student>는 Student, Person

![](https://velog.velcdn.com/images/coen/post/d72bf390-c8f3-4132-a970-4e3e13ee4301/image.png)

## 유연성을 극대화 하려면 원소의 생산자나 소비자용 입력 매개변수에 와일드카드 타입을 사용하라.

#### 와일드카드 타입 공식
- 팩스(PECS) : producer-extends, consuper-super
매개변수화 타입 T가 생산자라면 <? extends T>를, 소비자라면 <? super T>를 사용하라.

pushAll의 매개변수는 Stack이 사용할 E 인스턴스를 생산하므로 Iterable<? extends E> 가 적절하다.

popAll의 매개변수는 Stack으로부터 E 인스턴스를 소비하므로 Collection<? super E> 가 적절하다.

## SPEC 공식은 와일드카드 타입을 사용하는 기본 원칙이다.

---

코드 30-2의 union 메소드를 SPEC 공식에 맞춰 변환하면 아래와 같다.
```java
public static <E> Set<E> union(Set<E> s1, Set<E> s2) // 30-2 원본

public static <E> Set<E> union(Set<? extends E> s1, Set<? extends E> s2) // 30-2 수정본
```

반환 타입에는 한정적 와일드카드 타입을 사용하면 안된다. 유연성을 높여주기는 커녕 클라이언트 코드에서도 와일드카드 타입을 써야하기 때문이다.
그러므로, 클래스 사용자가 와일드카드 타입을 신경써야 한다면 API 설계가 잘못되었을 가능성이 크다.

코드 30-7을 수정해보자.

```java
public static <E extends Comparable<E>> E max (List<E> list)

public static <E extends Comparable<? super E>> E max (List<? extends E> list)
```

매개변수를 먼저 보자면, 입력 매개변수에서 E 인스턴스를 생산하므로 `List<? extends E>`를 사용하는게 적절하다.

타입 매개변수 `Comparable<E>`는 E 인스턴스를 소비하기에 `Comparable<? super E>`가 적절하다.

수정버전의 `max`는 이 책에서 가장 복잡한 메서드 선언이다.
이렇게까지 해야할 이유가 있나? 라고 생각할만 한데, 이렇게 까지 할 가치가 있다.


```java
List<ScheduledFuture<?>> scheduledFutures = ... ;
```

위의 `List`는 수정버전 `max` 만 처리 가능하다.

수정 전 max가 이 리스트를 처리할 수 없는 이유는 `ScheduledFuture`가 `Comparable<ScheduledFuture>`를 구현하지 않았기 때문.

하지만 `ScheduledFuture` 는 `Delayed`의 하위 인터페이스이고, Delayed는 `Comparable<Delayed>`를 구현했는데, 와일드 카드를 통하여, `Comparable`을 직접 구현하지 않고 직접 구현한 다른 타입을 확장한 타입을 지원할 수 있는 것이다.
 
---

타입 매개변수와 와일드카드는 공통되는 부분이 있어서 메서드를 정의할 때 둘 중 어느 것을 사용해도 괜찮을 때가 많다.
- 주어진 리스트에서 명시한 두 인덱스의 아이템들을 교환하는 정적 메서드 두 방식 모두로 정의해 보자.

```java
public static <E> void swap(List<E> list, int i, int j);

public static void swap(List<?> list, int i, int j);
```
public API라면 간단한 두번째가 낫다.

기본 규칙.
- 메서드 선언에 타입 매개변수가 한 번만 나오면 와일드카드로 대체해라.

하지만 두번째 와일드카드 swap에는 문제가 있는데
```java
public static void swap(List<?>, int i, int j) {
	list.set(i, list.set(j, list.get(i)));
}
```
가 컴파일 되지 않는다는 점이다.

List<?>에는 null 이외에 어떠한 값도 넣을 수 없다. 컴파일러는 만나는 모든 와일드카드 ? 마다 독립적인 capture 를 만들기 때문이다.

위 코드를 형변환하거나, 리스트의 로 타입을 사용하지 않고 해결하는 방법이 있다.

```java
public static void swap(List<?>, int i, int j) {
	swapHelper(list, i, j);
}

private static <E> void swapHelper(List<E> list, int i, int j) {
	list.set(i, list.set(j, list.get(i)));  
}
```

swapHelper 는 List가 List<E>임을 알고있다. 리스트에서 값을 꺼내면 그 값의 타입은 E이고, E 타입은 리스트에 넣어도 안전하다. swap 메서드 내부에서 더 복잡한 제네릭 메서드를 이용했지만, 클라이언트는 swapHelper의 존재를 모른 채 그 혜택을 누릴 수 있따ㅣ.


<br/><br/><br/><br/>

---

> 제네릭을 잘 모르겠어서 자바의정석 보다가 신기(~~나만 신기한가~~)한것을 봐서 적어보는것


```java
class TestClass{}

class TestClass1 extends TestClass implements TestInterface {}

class TestClass2 extends TestClass {}

class TestClassInterface<E extends TestClass & TestInterface> {} // TestInterface를 구현한 TestClass의 자식 클래스만 담을수 있다.

public static void main(String[] args) {

	TestClassInterface<TestClass1> test = null; // 컴파일 성공
	TestClassInterface<TestClass2> test = null; // 컴파일 오류
	
}
```





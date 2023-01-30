# 메서드가 던지는 모든 예외를 문서화하라

메서드가 던지는 예외는 메서드를 올바르게 사용하는 데 아주 중요한 정보다.  
그러니 예외에 대해 문서화를 해두어야 한다.

## @throws

자바독의 @throws 태그로 각 예외가 발생하는 상황을 문서화할 수 있다.

예시로 `ArrayList`의 생성자를 하나 가지고 왔다.  
초기화 크기가 음수일 경우 `IllegalArgumentException`이 발생한다고 알려주고 있다.

```java
/**
  * Constructs an empty list with the specified initial capacity.
  *
  * @param  initialCapacity  the initial capacity of the list
  * @throws IllegalArgumentException if the specified initial capacity
  *         is negative
  */
public ArrayList(int initialCapacity) {
    ...
}
```

예외는 각각 따로 선언해서 문서화 해야 한다.  
`Exception`이나 `Throwable`같은 공통 상위 클래스 하나로 뭉뚱그려선 안된다.  
메서드 사용자에게 각 예외에 대한 별개의 힌트를 제공할 수 없기 때문이다.  
(main메서드는 예외로, JVM이 호출하므로 선언에 자유롭다)

예시로 `ArrayList`의 `addAll`메서드를 들 수 있다.

```java
 /**
  * Inserts all of the elements in the specified collection into this
  * list, starting at the specified position.  Shifts the element
  * currently at that position (if any) and any subsequent elements to
  * the right (increases their indices).  The new elements will appear
  * in the list in the order that they are returned by the
  * specified collection's iterator.
  *
  * @param index index at which to insert the first element from the
  *              specified collection
  * @param c collection containing elements to be added to this list
  * @return {@code true} if this list changed as a result of the call
  * @throws IndexOutOfBoundsException {@inheritDoc}
  * @throws NullPointerException if the specified collection is null
  */
public boolean addAll(int index, Collection<? extends E> c) {
  ...
}
```

인터페이스에서도 @throws 태그를 사용할 수 있다.  
자바독에 예외를 명시함으로써 일반 규약을 정한 것이다.

`List` 인터페이스에서 이러한 사용법을 볼 수 있다.  

```java
/**
* Returns {@code true} if this list contains the specified element.
* More formally, returns {@code true} if and only if this list contains
* at least one element {@code e} such that
* {@code Objects.equals(o, e)}.
*
* @param o element whose presence in this list is to be tested
* @return {@code true} if this list contains the specified element
* @throws ClassCastException if the type of the specified element
*         is incompatible with this list
* (<a href="Collection.html#optional-restrictions">optional</a>)
* @throws NullPointerException if the specified element is null and this
*         list does not permit null elements
* (<a href="Collection.html#optional-restrictions">optional</a>)
*/
boolean contains(Object o);
```

비검사 예외와 검사 예외 모두 문서화하자.  

그리고 비검사 예외를 메서드 선언의 throws목록에 넣으면 좋지 않다.  

사용자는 @throws만 있는 경우와 throws절과 @throws에 있는 경우를 구분할 수 있고, 그로 인해 어느것이 비검사 예외인지 알기 쉽기 때문이다.

- 검사 예외의 경우
![image](https://user-images.githubusercontent.com/7973448/214629569-be301637-7388-4210-81b6-40604d6aaf51.png)

- 비검사 예외의 경우

![image](https://user-images.githubusercontent.com/7973448/214629865-83b68a15-30f3-4964-88d0-52d2e078c2f3.png)

## 다만..

비검사 예외는 모두 문서화하기 현실적으로 어려울 수 있다.  
클래스를 수정해서 새로운 비검사 예외를 던지게 만들어도 기존 소스와 호환되기 때문이다.

예시로 우리가 `ArrayList`를 사용하는 `ArrayListUser`를 만들었다고 하자.  
`ArrayList`의 `addAll`을 사용했고 예외도 모두 문서화 했다.

시간이 지나 `ArrayList::addAll`에서 새로운 비검사 예외를 던지더라도 우리 클래스는 수정하지 않아도 동작은 하지만, 문서화되지 않은 예외가 발생할 수 있을 것이다.

한 클래스의 많은 메서드가 같은 예외 상황을 문서화 해야 한다면 클래스의 자바독 문서에 적어두는 방법도 있다. (`NullPointerException`이 흔한 예시)
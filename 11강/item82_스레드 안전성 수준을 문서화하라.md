# 스레드 안전성 수준을 문서화 하라

다중 스레드 환경에서 메서드를 사용하는 방식도 클라이언트와 메서드의 계약이다.  
따라서 스레드 안정성 수준을 문서화 해야한다.

## 😬 API문서에서 **synchronized** 한정자가 붙은 메서드는 스레드 안전하다?

자바독이 생성한 API문서는 synchronized 한정자를 포함하지 않는다.

이는 synchronized 한정자가 메서드 구현이슈일 뿐 API에 속하지 않는다는 걸 의미하기 때문이다.

결국 이 한정자만으로는 스레드 안전성 수준을 알 수 없다.

아래 메서드의 API문서를 생성해보자. 

```java
public class Sync {
    private int num = 0;
    public synchronized int increaseNum() {
        return ++num;
    }
}
```

생성된 문서는 아래와 같다.

<img width="1440" alt="image" src="https://user-images.githubusercontent.com/7973448/216275710-b64218b1-d82e-422e-8c8f-2e0c104c4836.png">

synchronized 한정자를 볼 수 없다.  
사실 synchronized 유무로 스레드 안전성을 판단하는 것은 스레드 안전성이 (있음 / 없음)으로 나뉜다는 오해다.

## 📝 스레드 안전성 수준

스레드 안전성에도 수준이 나뉜다.  
다음 목륵은 안전성이 높은 순으로 나열한 것이다.

 - 불변(immutable): 이 클래스의 인스턴스는 변경이 불가능 하니 외부 동기화도 필요 없다.  
 ex) `Integer`

 - 무조건적 스레드 안전(unconditionally thread-safe): 이 클래스의 인스턴스는 수정가능하나 내부에서 동기화가 잘 되어있어 별도의 외부 동기화가 필요없다.  
 ex) `AtomicLong`, `ConcurrentHashMap`

 - 조건부 스레드 안전(conditionally thread-safe): 무조건적 스레드 안전과 같지만 일부 메서드는 동시에 사용하려면 외부 동기화가 필요하다.  
 ex) `Collections.synchronizedXXX로 생성한 인스턴스`

 - 스레드 안전하지 않음(not thread-safe): 이 클래스의 인스턴스는 수정되지 않고 동시에 사용하려면 클라이언트가 외부 동기화를 해야한다.  
 ex) `ArrayList`, `HashMap`같은 기본 컬렉션

 - 스레드 적대적(thread-hostile): 이 클래스는 동시에 사용하면 외부 동기화로 감싸더라도 안전하지 않다.  
 예컨데 이 수준의 클래스는 정적 데이터를 동기화 없이 사용한다.

이 분류는 스레드 안전성 애너테이션과도 대략 일치한다.
(`자바 병렬 프로그래밍`의 부록 A에 스레드 안전성 애너테이션 참고 😓)

 - `불변` - `@Immutable`
 - `무조건적 스레드 안전`과 `조건부 스레드 안전` - `@ThreadSafe`
 - `스레드 안전하지 않음` - `@NotThreadSafe`

## 🧐 조건부 스레드 안전한 클래스의 문서화

이 중에서 조건부 스레드 안전한 클래스는 주의해서 문서화 해야한다. 외부 동기화가 필요한 조건이 뭔지, 어떻게 락을 얻아야 하는지 알려줘야 한다.

예컨데 `Collections.synchronizedMap`는 아래와 같이 javadoc에 사용법을 명시하고 있다. (from java 1.2)

```java
/**
  * Returns a synchronized (thread-safe) map backed by the specified
  * map.  In order to guarantee serial access, it is critical that
  * <strong>all</strong> access to the backing map is accomplished
  * through the returned map.<p>
  *
  * It is imperative that the user manually synchronize on the returned
  * map when traversing any of its collection views via {@link Iterator},
  * {@link Spliterator} or {@link Stream}:
  * <pre>
  *  Map m = Collections.synchronizedMap(new HashMap());
  *      ...
  *  Set s = m.keySet();  // Needn't be in synchronized block
  *      ...
  *  synchronized (m) {  // Synchronizing on m, not s!
  *      Iterator i = s.iterator(); // Must be in synchronized block
  *      while (i.hasNext())
  *          foo(i.next());
  *  }
  * </pre>
  * Failure to follow this advice may result in non-deterministic behavior.
  *
  * <p>The returned map will be serializable if the specified map is
  * serializable.
  *
  * @param <K> the class of the map keys
  * @param <V> the class of the map values
  * @param  m the map to be "wrapped" in a synchronized map.
  * @return a synchronized view of the specified map.
  */
public static <K,V> Map<K,V> synchronizedMap(Map<K,V> m) {
    return new SynchronizedMap<>(m);
}
```

> 번역(번역기): 지정된 맵이 지원하는 동기화된(스레드 안전) 맵을 반환합니다.  
> 직렬 액세스를 보장하기 위해 지원 맵에 대한 모든 액세스가 반환된 맵을 통해 수행되는 것이 중요합니다.  
> **`Iterator`, `Spliterator` 또는 `Stream` 을 통해 컬렉션 뷰를 순회할 때 반환된 지도에서 사용자가 수동으로 동기화해야 합니다.**
> 
> ```java
> Map m = Collections.synchronizedMap(new HashMap());
> Set s = m.keySet();  // 동기화 블럭이 필요하지 않음!
> synchronized (m) {  // m을 동기화 함, s가 아님!
>     Iterator i = s.iterator(); // 동기화 블럭 안에 있어야 합니다!
>     while (i.hasNext())
>         foo(i.next());
> }
> ```
> **이 조언을 따르지 않으면 비결정적 동작이 발생할 수 있습니다.**  

위에서 컬렉션을 순회할때의 주의사항을 명시해주고 있다.  
클래스의 스레드 안전성은 클래스 주석에 작성하지만,  
어떤 특정한 메서드에 관한 안전성은 메서드 주석에 기재하자.  

또, `Collections.synchronizedMap`과 같이 반환 객체를 명확히 특정하지 않는 정적 팩터리는 자신이 반환하는 객체의 스레드 안전성을 문서화 해야 한다.

## 🤔 **비공개 락 객체** 쓰기

클래스가 외부에서 사용할 수 있는 락을 제공하면 클라이언트에서 일련의 메서드 호출을 원자적으로 수행할 수 있다

하지만 내부에서 처리하는 동시성 제어 메커니즘과 같이 사용할 수 없게 된다. (`ConcurrentHashMap`같은 것)

또, 클라이언트가 공개한 락을 오래 쥐고 놓지 않는 서비스 거부 공격(denial-of-service attack)을 할 수도 있다.

이러한 문제를 해결하기 위해선 synchronized 메서드 대신 **비공개 락 객체**를 사용해야 한다. (synchronized는 공개된 락이다.)

```java
private final Object lock = new Object();

public void foo() {
    synchronized(lock) {
        ...
    }
}
```

클라이언트는 `lock` 변수에 접근할 수 없으니 동기화에 관여할 수 없다.

당연히 **비공개 락 객체**는 무조건적 스레드 안전 클래스에서만 사용할 수 있다. 
(조건부 스레드 안전 클래스는 락을 클라이언트에게 제공해주기 때문)

## 추가

아이템 78에 나온 예제로 스레드 적대적인 경우를 확인할 수 있다.

```java
private static volatile int nextSerialNumber = 0;

public static int generateSerialNumber() {
    return nextSerialNumber++;
}
```

위 메서드의 `nextSerialNumber`는 `volatile`으로 선언했지만,  
이는 락의 역할을 수행하지 못한다.

그래서 `nextSerialNumber`에 락을 걸어야 하는데, private 필드이기 때문에 외부에서 락을 걸 수 있는 방법이 없다.

따라서 어떻게 해도 스레드 안전하지 않다.
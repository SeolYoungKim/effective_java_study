**스터디 깃허브 : [https://github.com/SeolYoungKim/effective\_java\_study](https://github.com/SeolYoungKim/effective_java_study)**

-   7명이서 일주일마다 1인 1아이템 정리 및 발표

**소스코드 깃허브 : [https://github.com/SeolYoungKim/effective-java-example](https://github.com/SeolYoungKim/effective-java-example)**

### **이펙티브 자바 스터디 3주차** 

#### **개인 주제 |** 아이템 21. 인터페이스는 구현하는 쪽을 생각해 설계하라

기존 인터페이스에 디폴트 메서드 구현을 추가하는 것은 위험한 일입니다.

-   디폴트 메서드는 구현 클래스에 대해 아무것도 모른 채, 합의 없이 무작정 "삽입"될 뿐입니다.
-   디폴트 메서드는 기존 구현체에 런타임에 어떠한 오류를 발생시킬 가능성이 있습니다. (런타임 에러가 될 수도 있고, Error가 될 수도 있습니다.)

때문에, 인터페이스를 설계할 때는 세심한 주의를 기울여야 합니다.

-   서로 다른 방식으로 최소 세 가지는 구현을 해보세요. (상속을 구현할 때도 기준이 세개였는데, 세개면 충분한가 봅니다.)

#### **책에서의 예시 |** Collection의 removeIf() 디폴트 메서드

Collection의 removeIf()는 Java8에서 추가된 default 메소드인데, 아래와 같이 Predicate를 넘겨 받을 수 있도록 구성되어 있습니다. (콜백의 한 예시가 될 수 있을 것 같네요.)

```
default boolean removeIf(Predicate<? super E> filter) {
    Objects.requireNonNull(filter);
    boolean removed = false;
    final Iterator<E> each = iterator();
    while (each.hasNext()) {
        if (filter.test(each.next())) {
            each.remove();
            removed = true;
        }
    }
    return removed;
}
```

저자의 말에 따르면, Java 라이브러리의 디폴트 메서드는 코드 품질이 높고 범용적이라 대부분의 상황에서 잘 작동한다고 합니다. 저 메서드가 자바 API 내에서는 문제가 없다고 말하는 것 같았습니다.

다만, 아파치에서 따로 구현한 동기화 Collections는 책 작성 시점에서는 removeIf()를 구현하지 않았다고 설명(?)하고 있습니다. 이는 문제가 될 수도 있다고 하는데요. 어떤게 문제가 될까요?

default 메서드로 구현된 removeIf()는 아파치의 동기화 Collections에서 아무런 제약 없이 사용될 수 있습니다. 바로 이 부분이 문제입니다. 아무래도 아파치의 동기화 Collections는 "동기화"를 지원하는 Collection이기 때문에, 모든 오퍼레이션을 동기화를 통해 멀티 쓰레드 환경에서, 한번에 한 쓰레드만 오퍼레이션을 시작할 수 있도록 구성했을 것입니다.

이러한 상황에서, 동기화를 지원하지 않는 removeIf()를 사용하게 되면, 멀티 쓰레드에 대해 안전하지 않은 Collection이 되어버리기 때문입니다. 결과적으로 이는 예상치 못한 동작으로 이어지게 되어, ConcurrentModificationException이 발생할 확률이 높아집니다.

따라서, 해당 클래스가 removeIf()를 오버라이딩 하게 해야하는데, 인터페이스의 default 메서드는 추가가 된걸 컴파일러를 통해 따로 알려줄 수 없어, 서드 파티 클래스들은 어떤 인터페이스에 어떤 default 메서드가 추가되었는지 일일히 확인하기가 매우 어렵습니다.

결과적으로, 이미 만들어져 사용 중인 interface에 default 메서드를 추가할 때는 아주 많은 생각을 해야한다고 합니다. 

---

설계 할 때부터 예측하면 좋겠지만.. 이미 만들어져 사용중인 interface에 default 메서드를 추가하는 것은 정말 필요한 경우에만 해야할 것 같습니다. 

혹은, 그 interface에 반드시 넣어야 하는 default 메서드가 아니라면, 추상 클래스를 통해 구현하는 방법(추상 골격 클래스)도 있다고 합니다. 그런 우회 방법을 고려하는게 더 좋을 수도 있을 것 같네요.

# 아이템 24: 맴버 클래스는 되도록 static으로 만들라

## 맴버 클래스란?

`맴버 클래스`는 `중첩 클래스`로 `정적 맴버 클래스`와 `비정적 맴버 클래스`로 나뉜다.

## 정적 맴버 클래스

정적 맴버 클래스는 다른 클래스 안에 선언되는데,  
바깥 클래스의 private 맴버에도 접근할 수 있다.

정적 맴버 클래스는 다른 정적 맴버와 같은 규칙을 적용한다.  
예를 들어 private으로 선언하면 바깥 클래스만 사용할 수 있는 식이다.

아래는 정적 맴버 클래스를 사용한 예제다.

```java
class Parent {
    private static String name = "Parent";

    static class Child {
        void printMyParentName() {
            // 바깥 클래스의 private 정적 맴버를 사용할 수 있다.
            System.out.println(name);
        }
    }

    private static class VeiledChild {
        void printMyParentName() {
            // 바깥 클래스의 private 정적 맴버를 사용할 수 있다.
            System.out.println(name);
        }
    }
}

class Scratch {

    public static void main(String[] args) {
        Parent.Child child = new Parent.Child();
        child.printMyParentName();

        /** 아래는 컴파일 에러가 뜬다! **/
//        Parent.VeiledChild child2 = new Parent.VeiledChild();
//        child2.printMyParentName(`);
    }

}
```

보는 것과 같이 접근 지시자를 다른 정적 맴버와 같이 사용할 수 있으며,  
외부에서 `클래스.클래스` 형태로 클래스를 참조할 수 있다.

## 비정적 맴버 클래스

비정적 맴버 클래스의 인스턴스는 바깥 클래스의 인스턴스와 연결된다.  
바깥 클래스의 **인스턴스**에서 생성할 수 있다.

비정적 맴버 클래스는 `바깥클래스.this` 키워드를 이용해서 바깥 인스턴스의 참조를 가져올 수도 있다.

아래는 위의 예시를 비정적으로 바꾼 예시다.

```java
class Parent {

    private String name = "Parent";
    final Child child;

    Parent() {
        child = new Child();
    }

    class Child {

        void printMyParentName() {
            // 이런식으로 바깥 클래스의 맴버변수를 바로 참조할 수 있다.
            System.out.println(name);
        }

        Parent getMyParent() {
            return Parent.this;
        }
    }
}

class Scratch {

    public static void main(String[] args) {
        Parent parent = new Parent();
        parent.child.printMyParentName();

        // 아래 두 결과는 같다!
        System.out.println(parent);
        System.out.println(parent.child.getMyParent());
    }
}
```

위 예제에선 맴버 클래스를 바깥 클래스의 인스턴스에서 만들고 있지만,  
바깥 클래스의 인스턴스만 있다면 생성할 수 있다.  
아래와 같이 하면 된다.
```java
Child child = parent.new Child();
```

## 정적 맴버 클래스를 써야 할때

개념상 바깥 인스턴스와 독립적일 수 있다면 정적 클래스로 선언하는 것이 좋다.  
비정적 맴버 클래스는 바깥 인스턴스가 없으면 생성할 수 없기 때문이다.

게다가 비정적 맴버 클래스는 항상 바깥 인스턴스를 참조하고 있기 때문에 메모리 낭비가 일어날 수 있다.  
(바깥 인스턴스를 더 이상 쓰지 않는다고 해도 맴버 클래스가 살아있으면 참조하고 있기 때문에 GC가 회수하지 않는다.)

## 비정적 맴버 클래스를 써야 할때

비정적 맴버 클래스는 어댑터 기법을 정의할때 쓰인다.  
어떤 클래스의 인스턴스를 다른 클래스의 인스턴스 처럼 보이게 한다는 것인데, 컬렉션 뷰를 구현할 때 활용한다.

`HashMap`의 `KeySet`이 대표적인 예다.

```java
public class HashMap<K,V> ... {
  ...

  final class KeySet extends AbstractSet<K> {
    ...
  }

  ...
  public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }
}
```

위를 보면 `HashMap::keySet()`에서 키 값들을 `KeySet`인스턴스를 통해 외부로 내보내고 있다.

```java
class Scratch {

    public static void main(String[] args) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1,1);
        map.put(2,2);

        System.out.println(map);
        //{1=1, 2=2}

        Set<Integer> keys = map.keySet();
        keys.remove(1);

        System.out.println(map);
        //{2=2}
    }
}
```

`HashMap::keySet()`에서 내보낸 `KeySet`인스턴스의 값을 변경하면 `바깥 인스턴스 (map)`가 수정된다.

## 참고 했던 글
https://jihyeong-ji99hy99.tistory.com/122
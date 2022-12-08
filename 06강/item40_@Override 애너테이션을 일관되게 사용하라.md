# [ITEM 40] @Override 애너테이션을 일관되게 사용하라


# 자바 기본 애너테이션 중 가장 중요한것은 @Override

자바가 기본으로 제공하는 애너테이션 중에서 보통 가장 중요한 어노테이션으로 @Override 을 꼽을 수 있다.

## `@Override`

	- 상위 타입의 메서드를 재정의 했음을 뜻한다.

이 어노테이션을 일관되게 사용하면 여러 가지 악명 높은 버그들을 예방해준다.

```java
public class Bigram {
	private final char first;
    private final char second;

    public Bigram(char first, char second) {
        this.first = first;
        this.second = second;
    }
    
    public int hashCode() {
        return 31 * first + second;
    }

    public boolean equals(Bigram b) {
        return b.first == first && b.second == second;
    }
}

public static void main(String[] args) {
    Set<Bigram> s = new HashSet<>();
    for (int i = 0; i < 10; i++) {
        s.add(new Bigram('a', 'a'));
    }

    System.out.println(s.size());
}
```
Object의 equals를 override를 하려면 매개변수가 Object여야 하지만 위의 코드는 매개변수로 Bigram을 넣는 실수를 했다.
이 경우 Object의 euqals를 Override 한게 아니라 Overload 한것으로 간주된다.
하지만 @Override 어노테이션을 달고 컴파일하면 잘못된 부분을 IDE나 컴파일 단계에서 보여준다.

# 결론. 상위 클래스의 메서드를 재정의하려는 모든 메서드에 @Override 애너테이션을 달자.


추가1. IDE에서 관련 설정을 활성화하면 @Override 애너테이션이 달려있지 않은 메서드가 재저의 되었다면 경고를 준다.

추가2. 구체클래스에서 상위 클래스의 추상 메서드를 재정의한 경우엔 @Override 애너테이션을 달지 않아도 된다. 하지만 단다고 해서 해로울 것도 없다. 그냥 재정의를 했다면 @Override를 붙여라.




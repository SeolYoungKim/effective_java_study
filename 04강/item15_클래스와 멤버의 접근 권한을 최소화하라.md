# 아이템 15 - 클래스와 멤버의 접근 권한을 최소화하라
### 어설프게 설계된 컴포넌트와 잘 설계된 컴포넌트의 가장 큰 차이
- 클래스 내부 데이터와 내부 구현 정보를 외부 컴포넌트로부터 얼마나 잘 숨겼는가.
- 오직 API를 통해서만 다른 컴포넌트와 소통하며, 서로의 내부 동작 방식에는 전혀 개의치 않는다.
- 정보 은닉, 캡슐화 라고 한다.
<br/>
## 캡슐화의 장점!
1. 시스템 개발 속도를 높인다.
	- 여러 컴포넌트를 병렬로 개발할 수 있기 때문.
<br/>
2. 시스템 관리 비용을 낮춘다.
	- 각 컴포넌트를 더 빨리 파악하여 디버깅 가능
    - 다른 컴포넌트로 교체하는 부담을 줄여줌
<br/>
3. 정보 은닉 자체가 성능을 높여주지는 않지만 성능 최적화에 도움을 준다.
	- 완성된 시스템을 프로파일링해 최적화할 컴포넌트를 정한 다음 다른 컴포넌트에 영향을 주지 않고 해당 컴포넌트만 최적화할 수 있기 때문이다.
<br/>
4. 소프트웨어 재사용성을 높인다.
	- 외부에 거의 의존하지 않고 독자적으로 동작할 ㅅ ㅜ있는 컴포넌트라면 그 컴포넌트와 함께 개발되지 않은 낮ㅊ선 환경에서도 유용하게 쓰일 가능성이 크기 때문.
<br/>
5. 큰 시스템을 제작하는 난이도를 낮춘다.
	- 시스템 전체가 완성되기 전에도 개별 컴포넌트의 동작을 검증할 수 있기 때문이다.
<br/>

## 캡슐화의 기본 원칙
**모든 클래스와 멤버의 접근성을 가능한 좁혀야 한다.**
- 소프트웨어가 올바로 동작하는 한 항상 가장 낮은 접근 수준을 부여해야 한다는 뜻

톱레벨 클래스와 인터페이스에게 부여할 수 있는 접근 수준은 package-private과 public 두가지이다.
패키지 외부에서 사용할 이유가 없다면 package private으로 선언하여 사용한다.

```java
public class Name {
    private String privateName = "현태";
    String packagePrivateName = "ヒョンテ";
    protected String protectedName = "炫炱";
    public String publicName = "Hyuntae";
}
```
* private 변수 - 선언된 클래스에서만 접근 가능
* package private(default) 변수 - 선언된 클래스가 포함된 패키지에서 접근 가능
* protected 변수 - package private의 범위를 포함하여 상속받은 하위클래스에서 접근 가능
* public 변수 - 다른 패키지에서 접근 가능

---

### 주의점

- 접근 수준을 상위 클래스보다 좁게 설정할 수 없다
	- 상위 클래스의 인스턴스는 하위 클래스의 인스턴스로 대체해 사용할 수 있어야 한다는 리스코프 치환 원칙에 위배되고, compile오류가 발생한다.

- public 클래스의 인스턴스필드는 되도록 public이 아니어야 한다.
	- 필드가 가변 객체를 참조하거나, final이 아닌 인스턴스 필드를 public으로 선언하면 그 필드에 담을 수 있는 값을 제한할 힘을 잃게 된다.(불변성 보장 불가능)
    - Lock 획득과 같은 작업이 불가능하기 때문에, 쓰레드 세이프하지 않다.
    
- 길이가 0이 아닌 배열은 모두 변경이 가능하다.
```java
public static final String[] VALUE = { ... };
```
위 코드는 아래와 같이 수정이 가능하다.
```java
AccessModifierTest test = new AccessModifierTest();
test.VALUE[0] = "나는 해커다";
String[] testValue1 = test.VALUE;
for (String s : testValue1) {
    System.out.println("s = " + s);
} // {"나는 해커다", ...}
```
위와 같이 보안에 허점이 존재하고, 불변성을 지킬수 없기에 아래와 같은 두가지 방법으로 해결할 수 있다.
```java
private static final String[] PRIVATE_VALUES = {...};
public final List<String> VALUES = Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));
```
```java
private static final String[] PRIVATE_VALUES = {...};
public static final String values() {
	return PRIVATE_VALUES.clone();
}
```
1번은 public 배열을 private 으로 만들고 public 불변 리스트를 추가하는 방법이고,
2번은 배열을 private으로 만들고 그 복사본을 반환하는 방어적 복사의 방법이다.


## 결론
> 꼭 필요한 것만 골라 최소한의 public API를 설계하자.
그 외에는 클래스, 인터페이스, 멤버가 공개되지 않도록 해야 한다.
public 클래스는 상수용 public static final 필드 외에는 어떠한 public 필드도 가져서는 안되며 참조하는 객체가 불변인지 확인하라.

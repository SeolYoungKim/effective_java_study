# 자바의 인터페이스
인터페이스는 자신을 구현한 클래스의 인스턴스를 참조할 수 있는 타입 역할만 하는데, 오직 이 용도로만 사용해야 한다.

상수 인터페이스라는 안티패턴이 있는데, 절대 사용하지말라고 한다.

```java
public interface HyuntaeConstants {
	
    staic final String HYUNTAE_KOREAN = "현태";
	
    staic final String HYUNTAE_HIRAGANA = "ひょんて";
    
    staic final String HYUNTAE_KATAKANA = "ヒョンテ";
    
    staic final String HYUNTAE_ENGLISH = "Hyuntae";
}
```

**상수 인터페이스 안티패턴**은 인터페이스를 잘못 사용한 예이다.
- 클래스 내부에서 사용하는 상수는 외부 인터페이스가 아닌 내부 구현에 해당하기에 상수 인터페이스를 구현하는 것은 내부 구현을 클래스의 API로 노출하는 행위이다.

- 자바 플랫폼 라이브러리에서도 잘못활용한 예니까 따라하지마세요!


### 상수 인터페이스 안티패턴을 사용하지 않고 상수를 공개하는 방법
- 박싱클래스처럼 열거타입으로 만들어 공개
```java
public final class Integer extends Number implements Comparable<Integer> {

	...
    
    @Native public static final int   MIN_VALUE = 0x80000000;
    
    @Native public static final int   MAX_VALUE = 0x7fffffff;
    
    ...
    
}
```
- 인스턴스화 할 수 없는 유틸리티 클래스에 담아 공개
```java
public class HyuntaeConstants {

	private HyuntaeConstants() { }
	
    staic final String HYUNTAE_KOREAN = "현태";
	
    staic final String HYUNTAE_HIRAGANA = "ひょんて";
    
    staic final String HYUNTAE_KATAKANA = "ヒョンテ";
    
    staic final String HYUNTAE_ENGLISH = "Hyuntae";
}
```
유틸리티클래스에 정의된 상수를 클라이언트에서 사용하려면 클래스 이름까지 명시해야하는데, Static Import를 사용하면 변수명으로만 사용할 수 있다.
```java
String name = HyuntaeConstants.HYUNTAE_KOREAN; // 보통은 이렇게 사용해야 하지만

import package.package.HyuntaeConstants.*; // static import를 하면
String name = HYUNTAE_KOREAN; // 이렇게 사용 가능하다

```
`HyuntaeConstants`의 상수를 많이  사용한다면 static import를 사용하여 클래스 이름을 생략하면 좋다.

## 일반적으로 통용되는 명명 규칙을 따르라

<br/>

## 철자 규칙

특별한 이유가 없다면 반드시 따라야 한다. 

그렇지 않으면 유지보수가 어려워 진다는 것이다.

```
패키지, 인터페이스, 클래스, 메소드, 필드, 타입변수
```

<br/><br/>

## 패키지

```java
package com.lotto.domain;

import java.util.ArrayList;
```

- 패키지와 모듈명은 각 요소를 점(.)으로 이으며 계층적으로 만든다.

    - 점(.)으로 구분되는 각 요소는 보통 8자 이하 짧은 단어로 한다.
- 모두 소문자 혹은 숫자로 지어야 한다.
- 외부에서도 사용될 패키지라면 (인터넷 도메인) 이름을 역순으로 사용한다.

<br/><br/>

## 클래스, 인터페이스

```java
MainTitle
UserStatus
DiscountPolicy
```

- `Pascal Case(파스칼 표기법)`로 작성한다. 각 단어의 첫 번째 글자는 대문자이다
- 하나 이상의 단어로 이루어진다.

    - 각 단어는 대문자로 시작한다.
- 약자의 경우라도 첫글자만 대문자로 하는 것을 권장한다.
    - 자바 클래스의 명명 규칙인 첫 글자를 대문자로 한다는 규칙에 <br/>`Pascal Case(파스칼 표기법)`  + `Camel Case(캐멀 표기법)` 이 합쳐진 형태 같다(?)

<br/><br/>

## 메서드와 필드

```java
hasInvitation
sellTicketTo
```

- `Camel Case(캐멀 표기법)` 으로 작성한다.

    - 첫 글자는 소문자이고 나머지 단어의 첫 글자는 대문자로 작성한다

- 상수필드는 모두 대문자로 쓰며 단어 사이는 밑줄(_)로 구분한다.

<br/><br/>

## 타입 매개변수

```java
T / E / K, V / X / R
```

보통 한 문자로 표현한다고 한다.

- T : 임의의 타입. Type의 약자

- E : 컬렉션의 원소. Element의 약자
- K, V : Map의 키와 값. Key와 Value의 약자
- X : 예외. Exception의 약자
- R : 메소드의 반환 타입. Return의 약자

<br/><br/>

## 문법 규칙

- 객체를 생성할 수 있는 클래스명은 보통 명사, 명사구를 사용한다.

```java
Thread
```

<br/>

- 객체를 생성할 수 없는 클래스명은 보통 복수형 명사로 짓거나 형용사로 짓는다.

```java
Collections
```

<br/>

- 메소드명은 동사, 동사구로 짓는다.

```java
append, drawImage
```

<br/>

- boolean을 반환한다면 is나 has로 시작하고 명사, 명사구, 형용사로 끝난다.

```java
isBlank, hasSiblings
```

<br/>

- 객체의 타입을 바꿔서 다른 타입의 또 다른 객체를 반환하는 메소드는 보통 to타입 형태로 짓는다.

```java
toString, toArray, toPath
```

<br/>

- 객체의 내용을 다른 뷰로 보여주는 메소드는 as타입 형태로 짓는다.

```java
asList, asType
```

<br/>

- 객체의 값을 기본타입 값으로 반환하는 메소드는 타입Value 형태로 짓는다.

```java
intValue
```
## item85 - 자바 직렬화의 대안을 찾으라

## 핵심

```
“직렬화는 위험하다.”
```

직렬화의 위험성을 회피하는 가장 좋은 방법은 아무것도 역직렬화하지 않는 것입니다.

<br>

## 직렬화란 뭔가요?

넓은 의미로 직렬화는 어떤 데이터를 다른 데이터의 형태로 변환하는 것을 말합니다.

<br>

## 그렇다면 역직렬화란?

바이트 스트림에서 객체의 상태로 변환하는 것을 말합니다.

<br><br>

## 그럼 바이트 스트림이란 무엇을 말하나요?

스트림은 데이터의 흐름입니다. 데이터의 통로라고도 말합니다.

예를 들어, 웹 개발을 하다보면 클라이언트에서 서버에게 데이터를 보내는 일이 있습니다.

<br>

이처럼 스트림은 클라이언트와 서버같이 어떤 출발지와 목적지로 입출력하기 위한 통로를 말합니다.

자바는 이런 입출력 스트림의 기본 단위를 바이트로 두고 있고 

입력으로는 `InputStream`, 출력으로는 `OutputStream`라는 추상클래스로 구현되어 있습니다.

<br><br>

## 직렬화가 왜 위험하다고 하는거야?

직렬화가 위험한 이유는 공격 범위가 너무 넓기 때문이라고 말하고 있습니다. 

게다가 지속적으로 더 넓어져 방어하기도 어렵다고 합니다. 

<br>

OutputInputStream의 `readObject` 메서드를 호출하면서 객체 그래프가 역직렬화되기 때문이다.

바이트 스트림을 역직렬화하는 과정에서 `readObject` 메서드는 그 타입들 안의 모든 코드를 수행할 수 있습니다. 

즉, 그 타입들의 코드 전체가 악의적인 공격 범위에 들어갈 수 있다는 뜻이 된다.

<br>

한편 역직렬화 과정에서 호출되어 잠재적인 위험한 동작을 수행하는 메서드를 **가젯(gadget)** 이라고 부릅니다. 

하나의 가젯이 또는 여러 개의 가젯이 마음대로 코드를 수행하게 할 수 있다. 

따라서 아주 신중하게 제작된 바이트 스트림만 역직렬화해야 한다.

<br><br>

## 예제코드 - 역직렬화 폭탄

```java
static byte[] bomb() {
        Set<Object> root = new HashSet<>();
        Set<Object> s1 = root;
        Set<Object> s2 = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            Set<Object> t1 = new HashSet<>();
            Set<Object> t2 = new HashSet<>();

            t1.add("foo");
            s1.add(t1); s1.add(t2);
            s2.add(t1); s2.add(t2);
            s1 = t1;
            s2 = t2;
        }
        return serialize(root);
}
```

<br>

### 이미지 참고 - https://klyhyeon.tistory.com/301 

![이미지](/12강/item85.PNG)

`deserialize(bomb())`가 실행될 때, 

HashSet을 역직렬화 하기위해 2^100의 hashCode() 메서드(폭탄)이 호출됩니다. 

<br><br>

## 그럼 어떻게 해야하나?

가장 좋은 방법은 아무것도 역직렬화하지 않는 것이라고 합니다. 

직렬화를 피할 수 없고 역직렬화한 데이터가 안전한지 완전히 확신할 수 없다면 

java 9에 나온 ObjectInputFilter를 사용하는 것도 방법입니다. 

<br><br>

## 핵심 정리

- 자바의 직렬화는 위험하니 피해야 한다.
- 신뢰할 수 없는 데이터라면 역직렬화를 하지 말자.
- 시스템을 처음 설계한다면 JSON이나 프로토콜 버퍼 같은 직렬화 시스템을 사용하자.



<br><br>

### 참조 : 

[https://madplay.github.io/post/prefer-alternatives-to-java-serialization](https://madplay.github.io/post/prefer-alternatives-to-java-serialization)


[https://klyhyeon.tistory.com/301](https://klyhyeon.tistory.com/301)


https://github.com/Meet-Coder-Study/book-effective-java/blob/main/12%EC%9E%A5/85_%EC%9E%90%EB%B0%94_%EC%A7%81%EB%A0%AC%ED%99%94%EC%9D%98_%EB%8C%80%EC%95%88%EC%9D%84_%EC%B0%BE%EC%9C%BC%EB%9D%BC_%EC%9D%B4%ED%98%B8%EB%B9%88.md
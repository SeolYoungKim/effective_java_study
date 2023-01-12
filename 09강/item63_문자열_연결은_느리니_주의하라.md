# 문자열 연결은 느리니 주의하라 
- 문자열 연결 연산자 `+`로 문자열 n개를 잇는 시간은 n^2에 비례한다.
  - 문자열은 불변이기 때문에, 두 문자열을 연결할 때 양 쪽의 내용을 모두 복사해야 하기 때문 

## String 대신 StringBuilder를 사용하자
- 문자열 연결 성능이 아주 크게 개선된다.
```java
StringBuilder sb = new StringBuilder();
sb.append("연결할 문자열");
sb.toString();
```

## 정리 
- 많은 문자열을 연결할 때는 문자열 연결 연산자 `+`를 사용하지 말자
- 대신 StringBuilder의 append 메서드를 사용하자. 
- 문자 배열을 사용 하거나, 문자열을 (연결하지 않고) 하나씩 처리하는 방법도 있다. 

--- 
## 자매품
### StringJoiner
```java
StringJoiner stringJoiner = new StringJoiner(", ");
stringJoiner.add("A").add("B");
String result1 = stringJoiner.toString();
System.out.println(result1);  // A, B
```

### String.join
- `String.join` : 내부적으로 StringJoiner를 사용
```java
String a = "A";
String b = "B";
String result = String.join(", ", a, b);
System.out.println(result);  // A, B
```


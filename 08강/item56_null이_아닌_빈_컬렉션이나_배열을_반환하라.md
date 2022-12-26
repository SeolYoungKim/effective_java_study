# null이 아닌, 빈 컬렉션이나 배열을 반환하라 
- `null`을 반환할 경우, 클라이언트는 `null`을 처리하는 코드를 추가로 작성해야 한다.
  - `null`처리를 외부에서 해줘야 하므로 코드가 지저분 해진다.
  - 운좋게 오류가 안나다가, 수 년 후에나 오류가 발생하기도 한다.


### 빈 컨테이너를 할당하는 것도 비용이 드니 null을 반환하자?
- 성능 분석 결과, 성능 저하의 주범이라고 확인되지 않는 한, 이 정도 성능 차이는 신경 쓸 수준이 안됨 
- 빈 컬렉션과 배열은 굳이 새로 할당하지 않고도 반환할 수 있음 


다음과 같이 빈 컬렉션을 반환하면 된다.

```java
public List<Cheese> getCheeses() {
    return new ArrayList<>(cheesesInStock);
}
```


하지만, 이는 사용 패턴에 따라 빈 컬렉션 할당이 성능을 눈에 띄게 떨어뜨릴 수도 있다. 
- 이럴 땐 매번 똑같은 "빈 불변 컬렉션"을 반환하면 된다.
  - 빈 불변 컬렉션 반환하기 
    - `Collections.emptyList`, `Collections.emptySet`, `Collections.emptyMap`...

다만, 이 또한 최적화에 해당하니 꼭 필요할 때만 사용하자 

```java
public List<Cheese> getCheeses() {
    return cheesesInStock.isEmpty() ? 
        Collections.emptyList() : new ArrayList<>(cheesesInStock);
}
```


배열을 사용할 때도 마찬가지다. 절대 `null`을 반환하지 말고 길이가 0인 배열을 반환하자.

```java
List.of(1, 2, 3).toArray(new Integer[0]);
```

위 방식이 성능을 떨어뜨릴 것 같다면 길이 0짜리 배열을 미리 선언해두고 매번 그 배열을 반환하게 하자.

```java
private static final Integer[] EMPTY_INTEGER_ARRAY = new Cheese[0];
List.of(1, 2, 3).toArray(EMPTY_INTEGER_ARRAY);
```

배열을 미리 할당하면 성능이 나빠지므로 주의하자

```java
List.of(1, 2, 3).toArray(new Integer[integerList.size()]);
```

---

## 정리 
- `null`이 아닌 빈 배열이나 컬렉션을 반환하라
- `null`을 반환하는 API는 사용이 어렵고 오류 처리 코드가 늘어난다. 그렇다고 성능이 좋은 것도 아니다.

---

## 나의 생각
`null`은 `NullPointerException`이라는 무서운 예외를 발생시킬 수 있습니다. 저도 SpringSecurity를 하나도 모를 때, 컨트롤러 파라미터에 인증 객체가 `null`로 넘어와서 한동안 고생했던 기억이 있습니다.
이렇게 `null`이 넘어올 경우, `null`에 대한 처리를 해줘야 했기 때문에 제 서비스로직이 더러워질 수 밖에 없었습니다. 이런 점을 미루어 볼 때, (프레임워크는 그럴 수 없더라도) 저라도 제 코드가 `null`을 반환하지 않게 구성해야 덜 고생할 것 같습니다.
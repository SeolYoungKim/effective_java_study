# 전통적인 for문보다는 for-each문을 사용하라

인덱스나 `Iterator`가 필요한 경우가 아니라면 for-each문을 사용하는 편이 낫다.

## 📝 전통적인 for문

다음은 전통적인 for문을 이용해서 컬렉션을 순회하는 코드다.

```java
List<Integer> list = List.of(1,2,3);
for (Iterator<Integer> i = list.iterator(); i.hasNext(); ) {
    Integer num = i.next();
}
```

이번에는 인덱스 변수 `i`를 이용해 배열을 순회하는 코드다.

```java
int[] arr = new int[]{1,2,3};
for (int i = 0; i < arr.length; i++) {
    int num = arr[i];
}
```

위와 같은 코드들에서 **원소**만 필요한 경우 인덱스나 `Iterator`는 필요하지 않다.

이럴때는 아래에 나올 `for-each`문을 사용하면 좋다.

## 💥 전통적인 for문 - 문제점 1

순회가 중첩될때 실수를 저지르기 쉽다.

아래코드는 남여 짝을 구할 때, 모든 경우의 수를 출력하는 코드이다.  
실행하면 오류가 발생하는데, 문제가 뭔지 살펴보자.

```java
List<String> females = List.of("유리", "수지");
List<String> males = List.of("짱구", "훈이", "철수", "맹구");

for (Iterator<String> i = females.iterator(); i.hasNext(); ) {
    for (Iterator<String> j = males.iterator(); j.hasNext(); ) {
        System.out.println(i.next() + " ♥ " + j.next());
    }
}
```

```java
// 출력 결과
유리 ♥ 짱구
수지 ♥ 훈이
Exception in thread "main" java.util.NoSuchElementException
  at java.base/java.util.ImmutableCollections$ListItr.next
  (ImmutableCollections.java:375)
  at Scratch.main(scratch_10.java:23)
```

~~훈이의 꿈⭐️은 이루어진다~~

여기서 문제는 바깥 컬렉션(females)의 `Iterator`에서 `next()`가 너무 많이 호출된다는 점이다.  
원래대로라면 2번 호출해야 하지만, 안쪽 for문에 `next()`를 호출하니 `2*4=8`번 호출하게 될 것이다.  

결국 너무 많이 호출한 탓에 `NoSuchElementException`이 발생한다.

그렇다면 바깥 컬렉션(females)의 원소 개수가 내부 컬렉션(males)의 원소 개수의 배수라면?  
다시 말해서 females의 원소 개수가 4의 배수라면?  
프로그래머는 오류를 찾기 힘들 것이다.

### 🤔 다른 예제 

책에서는 바깥 컬렉션과 내부 컬렉션이 같을 때를 다루고 있다.

이번에는 남남 커플을 다루겠다.

```java
List<String> males = List.of("짱구", "훈이", "철수", "맹구");
for (Iterator<String> i = males.iterator(); i.hasNext(); ) {
    for (Iterator<String> j = males.iterator(); j.hasNext(); ) {
        System.out.println(i.next() + " ♥ " + j.next());
    }
}
```

```java
// 출력 결과
짱구 ♥ 짱구
훈이 ♥ 훈이
철수 ♥ 철수
맹구 ♥ 맹구
```

예상대로라면 `4*4=16`줄의 출력결과가 나와야 하지만 `4`줄만 나온다.  

지금 이 상황은 위에서 언급했던 `바깥 컬랙션의 원소 개수가 내부 컬렉션의 원소 개수의 배수일 때`와 같은 상황이다.  
예외가 발생하지 않아 알아차리기 어려울 수 있다.

## 👏 해결법과 for-each 대안

해결법은 아래와 같이 변수를 하나 만들어 주면 된다.

```java
List<String> females = List.of("유리", "수지");
List<String> males = List.of("짱구", "훈이", "철수", "맹구");

for (Iterator<String> i = females.iterator(); i.hasNext(); ) {
    // 이 변수!
    String female = i.next();
    for (Iterator<String> j = males.iterator(); j.hasNext(); ) {
        System.out.println(female + " ♥ " + j.next());
    }
}
```

```java
// 출력 결과
유리 ♥ 짱구
유리 ♥ 훈이
유리 ♥ 철수
유리 ♥ 맹구
수지 ♥ 짱구
수지 ♥ 훈이
수지 ♥ 철수
수지 ♥ 맹구
```

코드를 깔끔하게 바꾸고 싶다면 for-each문을 사용하면 좋다.  
아래와 같이 바꿀 수 있다.

```java
List<String> females = List.of("유리", "수지");
List<String> males = List.of("짱구", "훈이", "철수", "맹구");

for (String female : females) {
    for (String male : males) {
        System.out.println(female + " ♥ " + male);
    }
}
```

for-each문은 배열과 컬렉션은 물론 `Iterable`을 구현한 객체도 순회할 수 있다.

## 😩 for-each를 사용할 수 없는 상황

다음과 같은 경우에는 for-each를 사용할 수 없다.  
전부 

- 파괴적인 필터링(destructive filtering): `Iterator`의 `remove()`메서드를 호출하면 컬렉션을 순회하며 원소를 제거할 수 있다.

    📝 이렇게 말이다.
    ```java
    List<String> males = new ArrayList<>(List.of("짱구", "훈이", "철수", "맹구"));

    // 짱구는 액션가면 보러 갔습니다.
    for (Iterator<String> j = males.iterator(); j.hasNext(); ) {
        if (j.next().equals("짱구"))
            j.remove();
    }
    // 출력 : [훈이, 철수, 맹구]
    System.out.println(males);
    ```

- 변형(tranforming): 리스트나 배열을 순회 하면서 원소를 교체하고 싶다면 `Iterator`나 인덱스를 사용해야 한다.

- 병렬 반복(parallel iteration): 여러 컬렉션을 병렬로 순회해야 한다면 `Iterator`나 인덱스가 필요하다. (`🤔 다른 예제`가 이런 사례에 해당한다.)

## ➕ 추가

이번 예제에서 사용했던 코드를 컴파일 하면 어떻게 될까?  
for-each, for 상관없이 모두 while로 바뀐다.

🧩 첫번째 예제 
```java
List<String> females = List.of("유리", "수지");
List<String> males = List.of("짱구", "훈이", "철수", "맹구");
Iterator<String> i = females.iterator();

while(i.hasNext()) {
    String female = (String)i.next();
    Iterator<String> j = males.iterator();

    while(j.hasNext()) {
        System.out.println(female + " ♥ " + (String)j.next());
    }
}
```

🧩 두번째 예제
```java
List<String> males = List.of("짱구", "훈이", "철수", "맹구");
Iterator<String> i = males.iterator();

while(i.hasNext()) {
    Iterator<String> j = males.iterator();

    while(j.hasNext()) {
        PrintStream var10000 = System.out;
        String var10001 = (String)i.next();
        var10000.println(var10001 + " ♥ " + (String)j.next());
    }
}
```

🧩 세번째 예제
```java
List<String> females = List.of("유리", "수지");
List<String> males = List.of("짱구", "훈이", "철수", "맹구");
Iterator var2 = females.iterator();

while(var2.hasNext()) {
    String female = (String)var2.next();
    Iterator var4 = males.iterator();

    while(var4.hasNext()) {
        String male = (String)var4.next();
        System.out.println(female + " ♥ " + male);
    }
}
```
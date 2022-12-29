# [Item 53] 가변인수는 신중히 사용하라

가변인수(varagas) 메서드는 명시한 타입의 인수를 0개 이상 받을 수 있다. 

### 가변인수 메서드 실행 순서

1. 인수의 개수와 길이가 같은 배열을 만든다.

2. 인수들을 이 배열에 저장하여 가변인수 메서드에 건네준다.


인수의 갯수는 런타임에 배열의 길이로 알 수 있다.

```java
static int min(int... args) {
    if (args.length == 0)
        throw new IllegalArgumentException("인수가 1개 이상 필요합니다.");
    int min = args[0];
    for (int i = 1; i < args.length; i++)
        if (args[i] < min)
            min = args[i];
    return min;
}
```
위 코드는 문제가 있는 코드이다.

가장 심각한 문제는 인수를 하나도 넣지 않고 호출하면 런타임에 실패한다는 점이다.

그리고 코드도 지저분하다.

args 유효성 검사를 더욱 명시적으로 할 필요가 있고, `min`의 초기값을 설정하지 않으면 for-each문도 사용할 수 없다.

```java
static int min(int firstArgs, int... remainingArgs) {
    int min = firstArgs;
    for (int arg : remainingArgs)
        if (arg < min)
            min = arg;
    return min;
}
```

위 코드는 매개변수를 2개 받게 하여 이전 코드의 문제를 해결하였다.

가변인수는 인수의 갯수가 정해지지 않았을 때 아주 유용하다.

---

이러한 유용함에도 불구하고 성능에 민감한 상황이라면 가변인수는 걸림돌이 될 수 있다. 

가변인수 메서드는 호출될 때마다 배열을 새로 하나 할당하고 초기화 해야한다.

다행히 가변인수의 유연성이 필요할 때 선택할 수 있는 패턴이 있다.

예를 들어 사용하는 메서드의 호출 대부분이 인수를 3개 이하로 사용한다면, 인수가 0개인 것부터 4개인 것까지 총 5개를 다중정의 한다.

```java
public void foo() {...}
public void foo(int a1) {...}
public void foo(int a1, int a2) {...}
public void foo(int a1, int a2, int a3) {...}
public void foo(int a1, int a2, int a3, int... rest) {...}
```

위와 같은 패턴을 사용한다면 대부분의 요청은 평범한 매개변수로 처리를 하고, 나머지의 경우 새로운 배열을 생성하게 된다.

꼭 필요한 상황에서 사용하기에 좋을것이다.

EnumSet의 정적 팩터리도 이 기법을 사용해 열거 타입 집합 생성 비용을 최소화한다고 한다.

> ### 정리
> 
> 인수의 갯수가 일정하지 않은 메서드를 정의해야 한다면 가변인수가 반드시 필요하다.    
> 메서드를 정의할 때 필수 매개변수는 가변인수의 앞에 둔다.    
> 가변인수를 사용할 때에는 성능 이슈까지 확인하자.    

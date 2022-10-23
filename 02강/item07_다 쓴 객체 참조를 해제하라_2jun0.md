# 아이템 7 : 다 쓴 객체 참조를 해제하라

GC는 참조하지 않는 객체를 알아서 회수해준다.  
하지만 그렇다고 해서 메모리 관리에 신경을 안 쓸 수는 없다.

사용은 안하지만 참조를 해제하지 않는 코드는 주의해야 한다.

아래는 메모리 누수가 일어나는 스택 예제다.

```java
class Stack {
    private Integer[] numbers;
    private int size = 0;

    public Stack() {
        numbers = new Integer[10000];
    }

    public void push(Integer number) {
        numbers[size++] = number;
    }

    public Integer pop() {
        return numbers[--size];
    }
}
```

문제가 없어 보이지만, `pop()`함수는 `numbers`에서 들어있는 객체를 해제하지 않는다.

<img width="800px" src="https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FrNYcS%2FbtrPh9ueAik%2FQCtcj5NDFFKI1ItBHvpMyk%2Fimg.png">

실제로 `numbers`의 크기가 줄어드는게 아니기 때문에 참조하고 있는 값들은 살아있다.  
GC는 이렇게 남아있는 객체들을 해제 해주지 않는다.

참조값을 `null`로 수정해서 GC가 회수하게끔 수정해야 한다.

```java
class Stack {
    private Integer[] numbers;
    private int size = 0;

    public Stack() {
        numbers = new Integer[10000];
    }

    public void push(Integer number) {
        numbers[size++] = number;
    }

    public Integer pop() {
        Integer result = numbers[--size];
        numbers[size] = null; // 메모리 참조 해제
        return result;
    }
}
```

## 메모리 사용량의 차이를 보고 싶다!

이번에는 직접 스택을 생성해 메모리 사용량을 비교해본다.  
아래는 테스트에 쓰일 코드로, 10000개의 스택을 생성한 뒤 랜덤한 값으로 10000개를 채웠다가 모두 pop한다.

```java
class Scratch {
    public static void main(String[] args) throws InterruptedException {
        ArrayList<Stack> stacks = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 10000; i++) {
            Stack stack = new Stack();

            for (int j = 0; j < 10000; j++) {
                stack.push(new Integer(random.nextInt()));
            }

            for (int j = 0; j < 10000; j++) {
                stack.pop();
            }

            stacks.add(stack);
            Thread.sleep(1);
        }
    }
}
```

메모리 사용량은 VisualVM으로 확인해 보았다.  
아래는 메모리 누수가 일어나는 스택을 사용한 결과로 메모리를 **2,000MB**까지 잡아먹었다

![메모리 누수 heap 그래프](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fdaedyp%2FbtrPnglBlL8%2FbrBvcisVBKMnm6DGAckLNk%2Fimg.png)

참조를 null로 수정해준 스택을 사용하면 메모리를 **500MB**도 차지하지 않는다.

![메모리 해제 heap 그래프](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FV3MyD%2FbtrPmFeAn23%2FgFwrfvSdPhKKT2tFy5rkBk%2Fimg.png)

## 캐시도 메모리 누수의 범인!

위의 예시에서 사용한 스택과 같이 `HashMap`도 주의해야 할 녀석이다.  
키 값을 더 이상 사용하지 않지만 캐시가 참조하고 있다면 메모리에 그대로 살아있다.

`WeakHashMap`을 사용하면 외부에서 참조하지 않는 키의 엔트리는 자동으로 제거시킬 수 있다.  
아래는 `WeakHashMap`에서 키 객체 참조를 해제하는 예제다.  
(주의해야 할 점이 있다. key값만 이렇게 할 수 있다. value 참조를 해지해서 엔트리를 제거할 수는 없다)

```java
class Scratch {
    public static void main(String[] args) {
        WeakHashMap<Integer, String> map = new WeakHashMap<>();

        Integer key1 = 1000;
        Integer key2 = 2000;

        map.put(key1, "1K");
        map.put(key2, "2K");

        // 객체 1000 값을 더 이상 참조하지 않겠다.
        key1 = null;

        // 강제로 gc 실행
        System.gc();

        map.forEach((key, value)->{
            System.out.println(key + ": " + value);
        });
    }
}
```

결과는 다음과 같다.

```
2000: 2K
```

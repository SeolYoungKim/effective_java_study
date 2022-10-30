### **이펙티브 자바 스터디 2주차** 

#### **개인 주제 |** 아이템 9. try-finally 보다는 try-with-resources를 사용하라.

> 미리 양해 말씀 구합니다.  
> Suppress -> Supress로 p를 하나 빼먹고 표기하는 실수를 저질렀습니다. 글을 다 작성하고 발견해버려서.. 양해 부탁드립니다..^^;;

자바 라이브러리에는 **close** 메서드를 호출해 직접 닫아줘야 하는 리소스가 많습니다.

-   InputStream
-   OutputStream
-   java.sql.Connection
-   etc

자원을 닫는 것은 사용자가 놓치기 쉬운 부분입니다. 사실 저도 BufferedReader를 사용하고 닫지 않은 적이 훨씬 많았던 것 같습니다.

다행히도 해당 리소스 사용처가 "백준 알고리즘"이었기 때문에, 별 다른 문제를 야기하지 않았던 것 같습니다. 하지만, 런타임 환경에서 구동되고 있는 애플리케이션의 경우에는 안 닫아줬다면.. 뭔가 끔찍한 일이 발생했을 것 같습니다. (메모리 누수 등...)

위와 같이 close를 호출해서 닫아줘야 하는 자원들은, 아이템8에서 언급된 finalizer를 안전망으로써 활용하고 있지만, 여러 가지 위험요소가 있기 때문에 믿을만 하지 못하죠. 

그래서 자바에서는 전통적으로 "자원이 제대로 닫힘"을 보장하는 수단으로써 try-finally가 쓰였습니다. 

#### try-finally

이는 아래와 같이 사용할 수 있습니다.

```java
public static String BaekJoonSolve(String a, String b) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    try {
        br.readLine();
    } finally {
        br.close();
    }

    return "아무튼 정답임.";
}
```

이렇게만 보면 별 문제가 없어 보입니다. 

하지만, 2개 이상의 닫아줘야 하는 자원을 사용할 경우에는 아래와 같이 코드가 지저분 해질 수 밖에 없습니다.

```java
public static String BaekJoonSolve(String a, String b) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    try {
        br.readLine();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        try {
            bw.write("백준 문제는 너무 어렵습니다..");
        } finally {
            bw.close();
        }
    } finally {
        br.close();
    }

    return "아무튼 정답임.";
}

// 이렇게 작성하면 되는거 아님?
finally {
    bw.close(); -> 여기서 예외가 발생하면
    br.close(); -> 아래 구문은 실행되지 않습니다. 결론적으로, br은 닫히지 않게 됩니다. 
                -> 결국 leak을 야기합니다.
}
```

또한, try-finally 구문은 **"두 번째 발생한 예외가 첫 번째 발생한 예외를 삼킨다."**라는 심각한 문제를 야기합니다.

즉, stack trace에 두 번째(마지막) 예외만 기록됩니다. 이렇게 될 경우, 디버그가 매우 힘들어질 수 있습니다.

이게 무슨 말인지 확인하기 위해, 아래와 같이 무조건 예외가 발생하는 BufferedReader를 하나 만들었습니다.

(책에서 설명해주지 않는 이 코드를 [백기선 선생님께서는 잘 설명해 주십니다. 강의 짱좋음 ㄱㄱ!](https://www.inflearn.com/course/%EC%9D%B4%ED%8E%99%ED%8B%B0%EB%B8%8C-%EC%9E%90%EB%B0%94-1/unit/108774?tab=curriculum))

```java
public class TestBufferedReader extends BufferedReader {

    public HaHaBufferedReader(Reader in) {
        super(in);
    }

    public HaHaBufferedReader(Reader in, int sz) {
        super(in, sz);
    }

    @Override
    public String readLine() throws IOException {
        throw new IllegalArgumentException("첫번째 예외");
    }

    @Override
    public void close() throws IOException {
        throw new IllegalArgumentException("두번째 예외");
    }
}
```

그리고 나서, 메서드를 만들고 테스트 코드를 구성하였습니다.

```java
// 메서드
public static String supressEx(String a, String b) throws IOException {
    BufferedReader br = new HaHaBufferedReader(new InputStreamReader(System.in));
    try {
        br.readLine();
    } finally {
        br.close();
    }

    return "아무튼 정답임.";
}

// 테스트
class BadCaseTest {

    @DisplayName("첫번째 예외는 사라진다.")
    @Test
    void exTest() throws IOException {
        BadCase.supressEx("딱", "구");
    }

    @DisplayName("첫번째 예외는 사라진다.")
    @Test
    void exTest2() throws IOException {
        try {
            BadCase.supressEx("딱", "구");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fc5MFDC%2FbtrPU1BhdiG%2FEBaznQPnr2gNh0VOK33E7K%2Fimg.png)

실행 결과,

-   두 번째 예외가 발생했다는 것과, 두 번째 메시지만 출력 해줍니다.
-   supressed 예외가 있음을 알려는 주지만, 상세하게 명시해주지 않습니다.

물론, 방법적으로 두번째 예외 대신 첫번째 예외를 기록하도록 코드를 수정할 수 있다고는 하지만, 코드가 너무 지저분해지기 때문에 실제로 그렇게 하는 경우가 거의 없다고 합니다.

이러한 문제들은, try-finally 상에서는 해결하기 어려워 보입니다. 선조들은 이 문제를 어떻게 해결했을까요??

---

#### try-with-resources

위와 같은 문제들은, Java 7에서 나온 try-with-resources 덕분에 모두 해결되었다고 합니다. 

해당 구조를 사용하려면, 사용하고자 하는 자원이 AutoCloseable 인터페이스를 구현해야 합니다. 

```java
public interface AutoCloseable {
    void close() throws Exception;
}

public interface Closeable extends AutoCloseable {
    public void close() throws IOException;
}
```

AutoCloseable은 단순히 close() 메서드 하나만 있는 인터페이스 입니다. 

-   AutoCloseable을 좀 더 자세히 표현한 것이 Closeable 입니다. 던지는 예외가 IOException으로 더 상세하다는 특징이 있습니다.
-   AutoCloseable은 java.lang에 소속되어 있으며, Closeable은 java.io에 소속되어 있습니다.

BufferedReader, BufferedWriter 등 자바에서 기본적으로 제공하는 "닫아야 하는 자원"들은 대부분 이를 구현하고 있습니다. 때문에, 특별히 구현하지 않아도 try-with-resources 구문을 바로 사용할 수 있습니다.

```java
public static String BaekJoonSolve(String a, String b) throws IOException {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out))) {

        br.readLine();
        bw.write("너무어렵다!!!!!");
    }

    return "아무튼 정답임.";
}
```

위와 같이 작성할 경우, try에 선언된 리소스들은 자동으로 close()를 호출 해주기 때문에, 따로 닫아줄 필요가 없어져 try-finally 구문에 비해 코드가 짧아집니다.

또한, **"두 번째 발생한 예외가 첫 번째 발생한 예외를 삼킨다."**는 문제도 해결 해줍니다. 

```java
// 무조건 예외가 발생하는 TestBuffredReader는 try-finally에서 사용한 것과 동일합니다.
public static String supressEx(String a, String b) throws IOException {
    try (BufferedReader br = new TestBufferedReader(new InputStreamReader(System.in))) {
        br.readLine();
    }

    return "아무튼 정답임.";
}
```

위 코드에서 br.close()는 코드 상에 없지만, try-with-resource 구문이기 때문에 반드시 호출됩니다. 따라서, br.readLine()에서 첫 번째 예외가 발생하고, br.close()에서 두 번째 예외가 발생하게 됩니다. 어떻게 수행되는지 알아보기 위해 테스트 코드를 작성하였습니다.

```java
@DisplayName("첫번째 예외를 보여준다. 두번째 예외는 supressed로 명시")
@Test
void exTest() throws IOException {
    GoodCase.supressEx("딱", "구");
}

@DisplayName("첫 번째 예외를 보여준다.")
@Test
void exTest2() throws IOException {
    try {
        GoodCase.supressEx("딱", "구");
    } catch (Throwable e) {
        System.out.println("첫 번째 예외 메시지" + e.getMessage());
        System.out.println("Suppressed : " + Arrays.toString(e.getSuppressed()));
    }
}
```

![](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FxUhpu%2FbtrPXs6qnUu%2F3c14S7nJzieWrPQk1YKDPk%2Fimg.png)

try-finally와는 달리, try-with-resources에서는 첫 번째 예외가 메인 예외가 됩니다. 그리고 두 번째 예외는 supressed 예외가 되지만, 그래도 stackTrace에 명시해주는 것을 확인할 수 있습니다. 이로써, 디버그하기 더 수월한 코드가 되었습니다. 또한, 두 번째 테스트 코드를 보시면, Throwable의 getSuppressed를 호출하여 해당 예외에 접근할 수 있음을 확인할 수 있습니다.

그렇다면, try-with-resources는 어떻게 간결한 문법 하나로 이 모든게 가능한 것일까요 ??

#### try-with-resources의 바이트 코드

사실, 바이트 코드를 뜯어볼 생각을 스스로 한 것은 아닙니다.

책과 인터넷 만으로는 해결이 안되는 부분이 있어서, 백기선 선생님 강의도 참고를 하고 있는데, 기선쌤이 어떻게 동작하는지를 보려면 바이트 코드를 봐야한다 하셔서, 그때서야 함께 바이트 코드를 확인 해봤습니다.

앞으로는 무언가가 있을 땐 와 신기하다 아 그렇구나 하고 넘어가는게 아니라, 바이트 코드를 뜯어보고 원리를 파악하는 습관을 들여야 겠습니다 ㅎㅎ;;

아래는 테스트 할 때 사용했던 메서드의 바이트 코드입니다.

```
public static String supressEx(String a, String b) throws IOException {
    BufferedReader br = new TestBufferedReader(new InputStreamReader(System.in));

    try {
        br.readLine();
    } catch (Throwable var6) {
        try {
            br.close();
        } catch (Throwable var5) {
            var6.addSuppressed(var5);
        }

        throw var6;
    }

    br.close();
    return "아무튼 정답임.";
}
```

실행 순서는 다음과 같습니다.

**예외가 발생하는 경우**

1.  br.readLine() 수행
2.  1번에서 발생한 예외를 Throwable로 캐치
3.  br.close() 수행
4.  3번에서 발생한 예외를 Throwable로 캐치 및 addSuppressed()를 이용해 suppressed 예외로 등록
5.  2번에서 캐치했던 Throwable을 던짐

**예외가 발생하지 않는 경우**

1.  br.readLine() 수행
2.  br.close() 수행
3.  return 수행

이로써, 예외가 발생하든, 발생하지 않든 br.close()는 무조건적으로 수행되는 것을 확인할 수 있습니다. 또한, 이러한 특징 때문에 close()메서드는 몇번이 실행되어도 같은 결과가 발생해야 하므로, 멱등성(idempotent)을 보장해야 한다고 합니다. 

---

#### 정리

> 반드시 회수해야 할 자원을 다룰 때는 try-with-resources를 사용하자.  
> \- 코드가 간결하고 분명해진다.  
> \- 만들어지는 예외 정보도 유용하다.  
> \- 정확하고 쉽게 자원을 회수할 수 있다.

#### 느낀 점

백준에서 문제를 풀 때는 br.close()를 사용하지 않았었는데, 앞으로는 닫아줘야 하는 리소스는 try-with-resource를 사용해야 함을 깨달았습니다. 이러한 사소한 부분들이 memory leak을 야기할 수 있고, 예외가 숨겨짐에 따라 디버그가 어려울 수 있다는 점도 배웠습니다.

# 네이티브 메서드는 신중히 사용하라

## JNI란?

자바코드에서 네이티브 메서드를 호출할 수 있게 하는 기술  
네이티브 메서드는 C나 C++같은 네이티브 언어로 작성한 메서드를 말한다.

## 쓰임

1. 레지스트리 같은 플랫폼 특화 기능을 사용할 수 있다.
2. 네이티브 코드로 작성된 기존 라이브러리를 활용할 수 있다.
3. 성능 개선 목적으로 사용할 수 있다.

## 네이티브 메서드의 단점

플랫폼 특화 기능을 사용하려면 네이티브 메서드를 사용해야 하지만,  
자바는 점차 이러한 기능들을 지원해 내가고 있다.

자바 9부터 `ProcessHandler`가 추가돼, 프로세스에도 자바 코드로 접근할 수 있다.

```java
public static void main(String[] args) {
    ProcessHandle current = ProcessHandle.current();
    long pid = current.pid();
    System.out.println("pid = " + pid);
    Info info = current.info();
    System.out.println("info = " + info);
}
```

```text
pid = 5491
info = [user: Optional[2jun0], cmd: .../java, ...
```

성능을 개선할 목적으로는 네이티브 메서드를 사용하지 말자.

1. 많은 기능이 JVM에서 지원하고, 다른 플랫폼에 견줄만한 성능이 나온다.

    `java.math`가 처음 추가된 자바 1.1의 `BigInteger`는 C의 고성능 라이브러리에 의존했지만, 자바 3부터 순수 자바로 구현하면서 원래의 네이티브 메서드를 사용하는 방식보다 빨라졌다.
2. 네이티브 언어는 안전하지 않다.

    Item50에 나온 것처럼, C, C++는 메모리 충돌 오류가 일어나기 쉽다.
3. 디버깅이 어렵다.
4. 네이티브 메모리는 가비지 컬렉터가 자동으로 회수할 수 없다.
5. 접착 코드(glue code)는 가독성이 떨어진다.
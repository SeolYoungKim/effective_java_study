# finalizer와 cleaner 사용을 피하라
자바는 두 가지 객체 소멸자를 제공
finalizer는 예측할 수 없고, 상황에 따라 위험할 수 있다
기본적으로 쓰지말아야 한다. 자바9 부터는 deprecated
대안으로 cleaner가 생겼지만 이것도 쓰면 안됨

이 책에서는 try-with-resource와 try-finally를 사용하여 해결한다.

finalizer와 cleaner로는 제때 실행되어야 하는 작업을 절대 할 수 없고 언제 실행될지는 전적으로 GC 알고리즘에 달려있다.

```java
public static void main(String[] args) throws Throwable {

    int testCode = 1;

    if(testCode == 1) {
        FinalizerTest finalizerTest = new FinalizerTest();
        finalizerTest.hello();
        System.gc();
        Thread.sleep(1000l);
        System.gc();
    } else if (testCode == 2) { // finalize 예외만 발생
        FinalizerTest finalizerTest = null;
        try {
            finalizerTest = new FinalizerTest();
            finalizerTest.hello();
        } finally {
            finalizerTest.finalize();
        }
    } else if (testCode == 3) { // hello와 close예외 둘 다 보여줌
        try (FinalizerTest finalizerTest = new FinalizerTest()) {
            finalizerTest.hello();
        }
    } else if (testCode == 4) { // hello에서 예외발생 후 죽음
        FinalizerTest finalizerTest = new FinalizerTest();
        finalizerTest.hello();
        finalizerTest.close();
    } else if (testCode == 5) {// close에서만 예외발생
        FinalizerTest finalizerTest = null;
        try {
            finalizerTest = new FinalizerTest();
            finalizerTest.hello();
        } finally {
            finalizerTest.close();
        }
    }
}
```

프로그램 생애주기와 상관없는, 상태를 영구적으로 수정하는 작업에서는 절대 finalizer나 cleaner에 의존하면 안됨
ex) DB connection close같은것

잡지 못한 예외로 인해 불완전한 객체가 남아있을 수 있으며 어떻게 동작할지 예측 불가능하다.
finalizer에서 일어난 예외는 출력되지 않는다.

성능이슈까지 있다.

AutoCloseable을 구현하고 close 메소드를 호출하게 해야한다.
예외가 터져도 제대로 종료되도록 try-with-resource 사용할 것

아래는 AutoCloseable을 사용한 예제 코드이다.
```java
public class Room implements AutoCloseable{

    private static final Cleaner cleaner = Cleaner.create();

    private static class State implements Runnable {

        int numJunk;

        State(int numJunk) {
            this.numJunk = numJunk;
        }

        @Override
        public void run() {
            System.out.println("Junk 청소");
            numJunk = 0;
        }
    }

    private final State state;

    private final Cleaner.Cleanable cleanable;

    public Room(State state, Cleaner.Cleanable cleanable) {
        this.state = state;
        this.cleanable = cleanable;
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }
}
```

finalizer와 cleaner는 안전망으로만 사용한다.

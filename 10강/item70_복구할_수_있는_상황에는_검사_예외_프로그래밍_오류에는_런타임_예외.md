# 복구할 수 있는 상황에는 검사 예외를, 프로그래밍 오류에는 런타임 예외를 사용하라 
## 자바가 문제 상황을 알리는 타입(throwable)
- 검사 예외 
- 런타임 예외 
- 에러 

## 검사 예외  
```java
public static void checkedException() throws IOException {
    throw new IOException();
}
        
public static void main(String[] args) {
    try {
        checkedException();
    } catch (IOException e) {
        System.out.println("검사 예외 발생. 정보=" + e.getClass().getSimpleName());
    }
}
```
- 호출하는 쪽에서 복구하리라 여겨지는 상황에 사용 
  - 검사/비검사 예외를 구분하는 기본 규칙 
- 검사 예외를 던지면 호출자가 그 예외를 catch로 잡아 처리하거나, 더 바깥으로 전파하도록 강제하게 됨
  - 메서드 선언에 포함된 검사 예외 -> 메서드를 호출했을 때 발생할 수 있는 유력한 결과임을 사용자에게 알려주는 것 
  - 즉, API 사용자에게 검사 예외를 던져주어 그 상황에서 회복해내라고 요구한 것 

## 비검사 throwable
```java
public static void uncheckedException() {
    throw new RuntimeException();
}

public static void main(String[] args) {
    uncheckedException();
}

public static void error() {
    throw new AssertionError();
}

public static void main(String[] args) {
    error();
}
```
- 런타임 예외, 에러 두가지가 있음 
- 프로그램에서 잡을 필요가 없거나, 혹은 통상적으로는 잡지 말아야 함 
- 비검사 예외/에러를 던졌다 == 복구가 불가능하다 or 더 실행해봐야 득보다는 실이 많다 
- 예외를 잡아서 처리하지 않은 스레드는 적절한 오류 메시지를 내뱉으며 중단 됨 


## 프로그래밍 오류를 나타낼 때 
- 런타임 예외를 사용하자 
- 런타임 예외의 대부분은 전제조건을 만족하지 못했을 때 발생함
  - 단순히 클라이언트가 API 명세에 기록된 제약을 지키지 못했다는 뜻임 
- 복구 가능하다고 믿을 때 -> 검사 예외 사용
- 복구가 불가능하다고 믿을 때 / 확신이 어려울 때 -> 비검사 예외 사용


## 에러 
- JVM이 자원 부족, 불변식 깨짐 등 더이상 수행을 계속할 수 없는 상황을 나타낼 때 사용 
- Error 클래스를 상속하지는 말자 
- Error 클래스를 직접 던지지 말자 (AssertionError는 예외임)
- 비검사 throwable은 모두 RuntimeException의 하위 클래스여야 한다. 


## 예외는 객체다 
```java
static class CustomException extends RuntimeException {
    private static final String EXCEPTION_MESSAGE = "이 예외는 ooo한 이유로 발생하였습니다. 확인할 것 : 메서드";
    public CustomException() {
        super(EXCEPTION_MESSAGE);
    }
}
```
- 어떤 메서드라도 정의할 수 있는 완벽한 객체다 
- 예외의 메서드는 예외를 일으킨 상황에 관한 정보를 코드 형태로 전달하는 데 쓸 수 있다.
- 예외 상황에서 벗어나는 데 필요한 정보를 알려주는 메서드를 함께 제공하자. -> 아이템 75에서 더 얘기한다고 함 


## 정리
- 복구할 수 있는 상황이면 검사 예외를 던져라
- 복구 불가능한 상황이라면(프로그래밍 오류라면) 비검사 예외를 던져라 
- 확실하지 않다면 비검사 예외를 던져라
- 검사 예외도, 런타임 예외도 아닌 throwable은 정의하지 말자
- 검사 예외일 경우 복구에 필요한 정보를 알려주는 메서드도 제공하자 
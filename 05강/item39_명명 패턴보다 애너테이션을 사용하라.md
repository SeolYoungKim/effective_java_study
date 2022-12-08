# 명명 패턴보다 애너테이션을 사용하라

전통적으로 도구나 프레임워크에서 다뤄야 하는 프로그램 요소에는 명명 패턴이 사용되어 왔다.  
예를 들면 JUnit은 버전 3까지 테스트 메서드 명에 test를 붙여야 했다.

이런 방식의 테스트 단점
1. 오타가 나면 안된다.

   `testA`를 `tsetA`로 잘못지으면 테스트가 실행되지도 않고 알아차리기 어렵다.
2. 올바른 요소에 사용되는 것을 보증되지 않는다.  

   메서드가 아닌 클래스에 test를 붙이면 JUnit은 무시한다.
3. 프로그램 요소를 매개변수로 전달 할 수 없다.

   예를 들면, 특정 예외가 던져지는 지 테스트하고 싶을때, 명명 패턴으로 명시하기 어렵다. 

## @Test 마커 애너테이션

```java
/**
 * 테스트 메서드 선언용 애너테이션
 * 정적 메서드에만 사용 가능
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Test {
}
```

위와 같이 @Test를 선언했다. 
@Test 애너테이션 타입에 다른 애너테이션이 달려 있는 걸 볼 수 있는데, 이는 `메타애너테이션(meta-annotation)`이라고 한다.

`@Retention(RetentionPolicy.RUNTIME)`은 애너테이션이 런타임에도 남아 있음을 의미하고  
`@Target(ElementType.METHOD)`은 메서드에 @Test를 달 수 있다는 의미을 나타낸다.

@Test 애너테이션은 아무 매개변수가 없는데, 이런 애너테이션을 `마커(marker) 애너테이션`이라고 한다.

명명 패턴이 아닌 애너테이션을 사용하면   
1. 메서드에만 달 수 있다는 오류를 컴파일러에게 받을 수 있고,
2. 오타도 방지할 수 있다.

### @Test 마커 애너테이션 사용 예제

```java
class Sample {

   @Test
   void m1() {
   } // 잘못 사용함 (static 이 아님)

   @Test
   static void m2() {
   } // 성공

   @Test
   static void m3() { // 실패
      throw new RuntimeException("실패!");
   }
}
```

위 메서드를 테스트 해보겠다.  
`m1()`메서드는 static이 아니니 실패할 것이니 성공 1 실패 2가 나와야 한다. 

```java
public static void main(String[] args) {
   int tests = 0;
   int passed = 0;
   Class<?> testClass = Sample.class;
   for (Method method : testClass.getDeclaredMethods()) {
      if (method.isAnnotationPresent(Test.class)) {
            tests++;
            try {
               method.invoke(null);
               passed++;
            } catch (InvocationTargetException e) {
               Throwable cause = e.getCause();
               System.out.println("실패: " + method + " (" + cause + ")");
            } catch (Exception e) {
               System.out.println("잘못 사용한 @Test: " + method);
            }
      }
   }

   System.out.println("성공 : " + passed + ", 실패 : " + (tests - passed));
}
```

테스트는 위 코드에서 진행한다.  
`isAnnotationPresent`메서드는 애너테이션이 달려 있는지 확인할 수 있다.

try-catch문에서 `InvocationTargetException`는 리플렉션 메커니즘이
`method.invoke`에서 발생한 예외를 감싸서 던져준 것이다.  
이외의 예외는 아래 catch문에서 잡힌다. (@Test 애너테이션을 잘못 사용한 경우)

테스트 결과는 아래와 같다.

```
잘못 사용한 @Test: void Scratch$Sample.m1()
실패: static void Scratch$Sample.m3() (java.lang.RuntimeException: 실패!)
성공 : 1, 실패 : 2
```

## 매개변수를 받는 애너테이션

이번에는 예외 클래스를 매개변수로 받는 애너테이션을 사용해보자.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface ExceptionTest {
   Class<? extends Throwable> value();
}
```

### @ExceptionTest의 사용 예제

던지는 예외가 `ArithmeticException.class`인지 확인하는 테스트 3개를 작성했다.

```java
class Sample {

   @ExceptionTest(ArithmeticException.class)
   static void m1() { // 성공
      int i = 1 / 0;
   }

   @ExceptionTest(ArithmeticException.class)
   static void m2() { // 실패 (다른 예외 발생)
      int[] a = new int[0];
      int i = a[1];
   }

   @ExceptionTest(ArithmeticException.class)
   static void m3() { // 실패 (예외 없음)
   }
}
```

테스트 도구에 예외 타입이 같은지 확인하는 코드가 추가되었다.  
아래 코드로 매개변수로 전달한 클래스를 가져온다.
`method.getAnnotation(ExceptionTest.class).value();` 

```java
public static void main(String[] args) {
   int tests = 0;
   int passed = 0;
   Class<?> testClass = Sample.class;
   for (Method method : testClass.getDeclaredMethods()) {
      if (method.isAnnotationPresent(ExceptionTest.class)) {
            tests++;
            try {
               method.invoke(null);
               System.out.println("실패 (예외 없음): " + method);
            } catch (InvocationTargetException e) {
               Throwable cause = e.getCause();
               Class<? extends Throwable> excTypes =
                        method.getAnnotation(ExceptionTest.class).value();
               if (excTypes.isInstance(cause)) {
                  passed++;
               } else {
                  System.out.println("실패 (다른 예외): " + method + " (" + cause + ")");
               }
            } catch (Exception e) {
               System.out.println("잘못 사용한 @Test: " + method);
            }
      }
   }

   System.out.println("성공 : " + passed + ", 실패 : " + (tests - passed));
}
```

결과는 아래와 같다.

```
실패 (다른 예외): static void Scratch$Sample.m2() (java.lang.ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 0)
실패 (예외 없음): static void Scratch$Sample.m3()
성공 : 1, 실패 : 2
```

## 반복 가능한 애너테이션

위에서 검증한 예외는 하나였지만, "둘 중 하나"와 같은 테스트를 진행하려면 어떻게 해야 할까?  
애너테이션을 반복해서 달아주면 된다.

다만 컨테이너 애너테이션을 정의해야 한다.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ExceptionTestContainer.class)
@interface ExceptionTest {
   Class<? extends Throwable> value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface ExceptionTestContainer {
   ExceptionTest[] value();
}
```

애너테이션이 여러개가 달려있으면 컨테이너 애너테이션인 `ExceptionTestContainer`를 이용해서 한번에 가져온다.

기존 애너테이션에는 `@Repeatable(ExceptionTestContainer.class)`을 정의해야 한다.

### 반복 가능한 @ExceptionTest의 사용 예제

위와 같은 예제에서 애너테이션을 하나 더 붙여주었다.

```java
class Sample {

   @ExceptionTest(ArithmeticException.class)
   static void m1() { // 성공
      int i = 1 / 0;
   }

   @ExceptionTest(ArithmeticException.class)
   @ExceptionTest(ArrayIndexOutOfBoundsException.class)
   static void m2() { // 성공
      int[] a = new int[0];
      int i = a[1];
   }

   @ExceptionTest(ArithmeticException.class)
   @ExceptionTest(ArrayIndexOutOfBoundsException.class)
   static void m3() { // 실패 (예외 없음)
   }
}
```

위 Sample 클래스는 컴파일 하면 아래와 같이 바뀐다.

```java
class Sample {
   Sample() {
   }

   @Scratch.ExceptionTest(ArithmeticException.class)
   static void m1() {
      int i = 1 / 0;
   }

   @Scratch.ExceptionTestContainer({@Scratch.ExceptionTest(ArithmeticException.class), @Scratch.ExceptionTest(ArrayIndexOutOfBoundsException.class)})
   static void m2() {
      int[] a = new int[0];
      int i = a[1];
   }

   @Scratch.ExceptionTestContainer({@Scratch.ExceptionTest(ArithmeticException.class), @Scratch.ExceptionTest(ArrayIndexOutOfBoundsException.class)})
   static void m3() {
   }
}
```

이에 맞추어 테스트 도구 소스도 수정했다.  
주의해야 할 점이 있는데, 반복가능한 애너테이션을 여러개 달면 컨테이너로 묶이기 때문에 아래처럼 모두 다 받아야 한다.
```java
if (method.isAnnotationPresent(ExceptionTest.class) ||
      method.isAnnotationPresent(ExceptionTestContainer.class))
```

`method.getAnnotationsByType`를 이용하면 애너테이션 여러개를 받을 수 있다.

```java
public static void main(String[] args) {
   int tests = 0;
   int passed = 0;
   Class<?> testClass = Sample.class;
   for (Method method : testClass.getDeclaredMethods()) {
      if (method.isAnnotationPresent(ExceptionTest.class) ||
               method.isAnnotationPresent(ExceptionTestContainer.class)) {
            tests++;
            try {
               method.invoke(null);
               System.out.println("실패 (예외 없음): " + method);
            } catch (InvocationTargetException e) {
               Throwable cause = e.getCause();
               int oldPassed = passed;
               ExceptionTest[] excTests = method.getAnnotationsByType(
                        ExceptionTest.class);

               for (ExceptionTest excTest : excTests) {
                  if (excTest.value().isInstance(cause)) {
                        passed++;
                        break;
                  }
               }

               if (oldPassed == passed) {
                  System.out.println("실패 (다른 예외): " + method + " (" + cause + ")");
               }
            } catch (Exception e) {
               System.out.println("잘못 사용한 @Test: " + method);
            }
      }
   }

   System.out.println("성공 : " + passed + ", 실패 : " + (tests - passed));
}
```

결과는 아래와 같다.

```
실패 (예외 없음): static void hello.jdbc.Scratch$Sample.m3()
성공 : 2, 실패 : 1
```

## 마무리

명명패턴을 이용하지 말고 되도록이면 에너테이션을 사용하자.  

다만, 대부분의 개발자는 애너테이션을 정의할 일이 없다.  
가능한 한 자바에서 제공하는 표준 애너테이션을 사용하자. 
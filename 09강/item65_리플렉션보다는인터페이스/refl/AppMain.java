package com.example.scratcher.refl;


import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class AppMain {

  public static void main(String[] args) throws Exception {
    String myClsName = "com.example.scratcher.refl.MyClass";
    String yourClsName = "com.example.scratcher.refl.YourClass";
    String mthdName1 = "doSomeThing";
    String mthdName2 = "doSomeThingPrivately";
    String mthdName3 = "doSomeThing2";
    String fldName1 = "base";
    //코드를 작성하는 시점에는 클래스를 모르고, 런타임에 클래스를 불러오기 위해서는 args로 입력받는게 마땅하지만
    //매번 ide의 실행 인자로 넣어주기 귀찮으므로 상수값으로 입력

//    runReflectionUsually(myClsName, mthdName1);
//    runReflectionToPrivate(yourClsName, fldName1, mthdName2);
//    runWithOutExceptionHandling(myClsName, mthdName3);
    // 위 모든 메소드 시그니처를 보면 throws Exception 이라고 선언되어 있는데,
    // 리플렉션은 뭔가 하나 할때마다 예외를 발생시킬 가능성이 있으므로 바람직하게 사용하기 위해서는 일일히 try-catch를 사용해야한다.

    for (var name : List.of(myClsName, yourClsName, "notExistClassName")) {
      try {
        System.out.println("\nrun : " + name);
        runUsingInterface(name);
      } catch (RuntimeException e) {
        e.printStackTrace();
      } // printstacktrace가 느려서 로그가 섞여서 나올수 있음
    }
  }

  // 일반적인 리플렉션의 이용
  static void runReflectionUsually(String clsName, String mthdName) throws Exception {
    Class cls = Class.forName(clsName);
    // 1. 생성 대상이 될 클래스를 찾음
    // 클래스와 패키지 명을 다 써줘야 함

    Constructor ctor = cls.getDeclaredConstructor();
    // 2. 생성 대상의 생성자를 찾음.
    // 생성자가 여러개라면, 인자값으로 파라메터 '타입'을 넣어주고, 맞는 생성자를 찾을수 있도록 해야함.

    var inst = ctor.newInstance();
    // 3. 실제 생성 대상의 인스턴스화
    // 2에서 찾은 생성자의 인자값에 맞는 인자를 넣어주어야 함.
    // 리플렉션은 생성 대상이 어떤 타입인지 컴파일 타임에 알수 없기 때문에 Object 형이 리턴된다.

    Method mthd = cls.getDeclaredMethod(mthdName, int.class);
    // 4. 실행시킬 메소드를 찾음.
    // 생성자와 마찬가지로 파라메터 타입을 같이 넘겨야 함.
    // (item 61에서 이야기한것과 다르게 int.class Integer.class 둘다 사용가능하며, 오토박싱이 지원되지 않으므로 타입을 엄격히 구분함)

    mthd.invoke(inst, 2);
    // 5. 실제 인스턴스의 메소드가 실행되는 부분
    // 인스턴스의 메소드를 실행시키는 것이 아니라, 찾은 메소드의 인자로 인스턴스와 인자를 넘겨줘야 함에 유의
  }

  // private 으로 설정된 멤버와 메소드에 접근
  static void runReflectionToPrivate(String clsName, String fldName, String mthdName)
      throws Exception {
    Class cls = Class.forName(clsName);
    Constructor ctor = cls.getDeclaredConstructor();
    var inst = ctor.newInstance();

    Field fld = cls.getDeclaredField(fldName);
    // cls.getField(name) 는 super에서 상속된 필드를 가져올 수 있으나, private 에는 접근하지 못함
    fld.setAccessible(true); // 접근 가능하도록 변경
    var bfFldValue = fld.get(inst); // 인스턴스의 멤버 값을 읽어옴
    System.out.println("before field value : " + bfFldValue);

    fld.set(inst, "its mine now "); // 새 값 세팅
    var aftFldValue = fld.get(inst); // 세팅된 새 값 읽어오기
    System.out.println("after field value : " + aftFldValue);

    Method prvtMthd = cls.getDeclaredMethod(mthdName); // private 메소드 가져오기
    prvtMthd.setAccessible(true); // 접근가능한 메소드로 변경
    prvtMthd.invoke(inst); // 실행
  }

  // 체크 예외를 무시하고 메소드 실행
  static void runWithOutExceptionHandling(String clsName, String mthdName) throws Exception {
    Class cls = Class.forName(clsName);
    Constructor ctor = cls.getDeclaredConstructor();
    var inst = ctor.newInstance();

    Method mthd = cls.getDeclaredMethod(mthdName);

    for (int i = 0; i < 10; i++) {
      mthd.invoke(inst);
    }
    // doSomeThing2 의 시그니처에는 체크 예외인 IOException이 정의되어 있지만, 리플렉션은 무시한다.
    // 이러한 에러 핸들링도 컴파일 타임에 지적해주지 못하므로 프로그래머가 직접 신경써야한다.
  }

  // 책에서 권장하는 안전한 방법
  static <T extends CoreInterface> void runUsingInterface(String clsName) {
    Class<T> cls;
    try {
      cls = (Class<T>) Class.forName(clsName);
    } catch (ClassNotFoundException e) {
      // fallback or failfast
      throw new RuntimeException(e);
    }

//    T inst; // 혹은 좀 더 분명하게
    CoreInterface inst;
    try {
      Constructor<T> ctor = cls.getDeclaredConstructor();
      inst = ctor.newInstance();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }

    inst.doSomeThing(10); // invoke에 비해 인자 타입이 무엇인지 좀더 명확하고 컴파일 타임에 검사가 가능함
    try { // 컴파일 타임에 체크 예외를 강제할 수 있음
      inst.doSomeThing2();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // 다만 이 경우 sub가 가진 메소드는 전혀 이용할 수 없음
  }
}

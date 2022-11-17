# 톱레벨 클래스는 한 파일에 하나만 담으라

### 톱 레벨 클래스란?

<br/>

```
// in Foo.java:

public class Foo { // top level class

    public static class NestedBar { // nested class
    }

```

톱 레벨 클래스란 '파일' 내에서 중첩되지 않은 클래스를 의미하며 위의 코드의 경우 Foo 는 톱 레벨 클래스로 볼수있다.

<br/>

이제 톱 레벨 클래스의 활용 시 문제가 생길만한 부분을 살펴보자

```
public class Foo{

    public static void main(String[] args){
        System.out.println(Test1.NAME+ Test2.NAME)
    }
}

```

위와 같이 단순 test1.NAME+ test2.NAME 의 호출을 위한 Foo class를 생성하였다.

```
// in TestList.java:

class Test1{
    static final String NAME ='hi'
}
class Test2{
    static final String NAME ='bye'
}

```

그리고 Test1,Test2 를 가지고 있는 TestList 파일을 만들어 static 기능들을 정리해두었다.

위의 상황에서는 컴파일 시 정상적으로 작동하며 문제가 생기지 않는다. 이제 문제가 생길만한 상황을 추가해보겠다.

```
// in TestList1.java:

class Test1{
    static final String NAME ='hi2'
}
class Test2{
    static final String NAME ='bye2'
}

```

위의 패키지 내에 TestList1 을 선언하였으며 내부 클래스는 동일하게 맞춰주었다. 이제 해당 파일을 컴파일 한다면
컴파일 순서에 따라 ex (javac Foo TestList or javac Foo TestList) 결과값이 다르게 나오며
이는 분명 코딩시 의도한 내용이 아닐것이다.

그럼 근본적인 해결책은 무엇일까? 이는 톱 레벨 클래스들 (Test1,Test2) 들이 TestList 라는 클래스 내부에 있기에
참조 시 발생하는 문제이므로 각각의 클래스들을 따로 파일을 만들어 관리를 해준다면 위와같은 중복에 대한 문제는 예방할 수 있다.

다른 방법으로는 Test1, Test2 에 static 을 붙혀 정적 멤버 클래스로 바꿔준다면 동일한 이름으로 타 클래스 선언이 되지 않기에 위의 문제를 방지할 수 있다.

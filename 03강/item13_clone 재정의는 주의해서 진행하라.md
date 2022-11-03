

# clone 재정의는 주의해서 진행하라.

### 우선  기존의 clone 사용법을 확인해보자
<br>

```

class Student18 implements Cloneable {

    int rollno;
    String name;

    Student18(int rollno, String name) {

        this.rollno = rollno;
        this.name = name;
    }

    public static void main(String args[]) {

        try {
            Student18 s1 = new Student18(101, "amit");

            Student18 s2 = (Student18) s1.clone();

            System.out.println(s1.rollno + " " + s1.name);
            System.out.println(s2.rollno + " " + s2.name);

        } catch (CloneNotSupportedException c) {
        }

    }

    public Object clone() throws CloneNotSupportedException {

        return super.clone();
    }
} 


```

<br>

기존 인터페이스는 해당 인터페이스에 정의된 메서드를 구현 클래스에서 구현해주는 방식으로 진행되지만,
Clone 의 경우 Object 객체를 Native로 돌려야 되기 때문에 메서드 자체는 Object 에 표시되며 인터페이스 Cloneable 을 통해 해당 클래스의 객체가
복사 가능하다는점을 알려준다.

<br>

 이에 어짜피 빈 껍데기인 Cloneable 을 implements 하지 않아도 상관없지 않을까 라는 생각에 해당 부분을 지우면
CloneNotSupportedException 의 예외가 발생되기에 실제 사용에서는 해당 인터페이스를 붙혀줘야 clone 메서드를 사용할 수 있다.

<br>
위의 코드를 돌려보면 기능적으로는 문제 없이 객체 복사가 잘 진행되는것을 알수있다.

만약 여기서 상속을 한 후 구현해보면 어떨까?

<br>


```

public class Student18Child extends Student18 {
    List<String> i;

    Student18Child(int rollno, String name,List<String> i) {
        super(rollno, name);
        this.i = i;
    }

    public static void main(String args[]) {

        try {
            Student18Child s1 = new Student18Child(101, "amit", new ArrayList<String>(){{add("hi");add("bye");}});

            Student18Child s2 = (Student18Child) s1.clone();

            s2.i.add("cocoa");

            System.out.println(s1.rollno + " " + s1.name+" " +s1.i );
            System.out.println(s2.rollno + " " + s2.name+" " +s2.i);

        } catch (CloneNotSupportedException c) {
        }

    }
}


```
<br>

위의 코드를 돌려보면 결과값으로
<br>

101 amit [hi, bye, cocoa]
<br>

101 amit [hi, bye, cocoa]

<br>
이 나오게 되며 이는 얕은 복사가 이루어진것을 알수있다.

이에 따라 clone 의 사용보다 직접 생성자 메서드를 만들어서 처리하는것이 좋다.


```
public class Student18Child extends Student18 {
    List<String> list1;

    Student18Child(int rollno, String name, List<String> list1) {
        super(rollno, name);
        this.list1 = list1;
    }

    public Student18Child(Student18Child student18Child) {
        super(student18Child.rollno, student18Child.name);
        this.list1 = new ArrayList<>();
        for (int i = 0; i < student18Child.list1.size(); i++) {
            this.list1.add(student18Child.list1.get(i));

        }
    }


    public static void main(String args[]) {

        try {
            Student18Child s1 = new Student18Child(101, "amit", new ArrayList<String>() {{
                add("hi");
                add("bye");
            }});

            Student18Child s2 = (Student18Child) s1.clone();

            Student18Child s3 = new Student18Child(s1);
            s3.list1.add("bye");
            System.out.println(s1.rollno + " " + s1.name + " " + s1.list1);
            System.out.println(s2.rollno + " " + s2.name + " " + s2.list1);
            System.out.println(s3.rollno + " " + s3.name + " " + s3.list1);
        } catch (CloneNotSupportedException c) {
        }

    }
}



```

<br>
101 amit [hi, bye]
<br>
101 amit [hi, bye]
<br>
101 amit [hi, bye, bye]
<br>


#### 결론적으로 clone 사용시 참조 객체에 대한 복사가 꼬일 수 있으므로 최대한 사용을 지양하고 깊은복사를 사용하는 방식으로 진햏을 권유한다( 복사 생성자)
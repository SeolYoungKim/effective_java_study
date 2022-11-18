### **이펙티브 자바 스터디 5주차** 

#### **개인 주제 |** 아이템 26. 로 타입은 사용하지 말라.

#### 용어 정리

-   제네릭 클래스 : 선언에 타입 매개변수가 쓰인 클래스
-   제네릭 인터페이스 : 선언에 타입 매개변수가 쓰인 인터페이스
-   제네릭 타입 : 제네릭 클래스와 제네릭 인터페이스를 통틀어 부르는 용어

#### 제네릭 타입

- 일련의 매개변수화 타입(Parameterized type)을 정의한다.

```java
// 정의 방법
// T: 정규 타입(formal)
public class MyClass<T> {  
  
	private final T genericField;  
  
	public MyClass(T genericField) {  
	    this.genericField = genericField;  
	}
}

// 사용 방법
// String: 실제 타입(actual)
MyClass<String> myClass = new MyClass<>("제네릭 필드!");  
```

- 제네릭 타입을 하나 정의하면 그에 딸린 **로 타입(raw type)** 도 함께 정의됨

- 로 타입이란?
	- 제네릭 타입에서 타입 매개변수를 전혀 사용하지 않을 때를 말함
	- `MyClass<T>`의 로 타입 == `MyClass`
	- `List<E>` 의 로 타입 == `List`

로 타입을 사용하면 안되는 이유

```java
public static void main(String[] args) {  
    MyClass<String> myClass = new MyClass<>("제네릭 필드!");  
    System.out.println(myClass.genericField);  
  
    MyClass rawTypeMyClass = new MyClass("히히 난 raw type이야.");  
    System.out.println(rawTypeMyClass.genericField);  
  
    List list = new ArrayList();  // Raw 타입 사용중이라는 경고 발생
    list.add(1000);               // Unchecked call 경고 발생
    list.add("1000원");  
    list.add('W');  
    list.add(List.of("ㅎㅎ", "ㅋㅋ", "ㅎㅎㅋㅋ"));  
    System.out.println(list);  
  
    String obj1 = (String) list.get(0);  
    System.out.println(obj1);  // ClassCastException 발생
}
```

- 컴파일러가 보내주는 경고만 뜰 뿐, 컬렉션에서 값을 꺼내기 전 까지 오류를 알아챌 수 없음.
- 오류는 가능한 한 발생 즉시, 이상적으로는 컴파일할 때 발견하는 것이 좋다.
- 로 타입을 사용하면, 오류가 발생하고나서 한참 뒤인 런타임에야 알아챌 수 있다.

그러니, 로 타입을 사용하지 말고 제네릭을 활용하여 정보가 타입 선언 자체에 녹아들 수 있게 하자.

```java
List<String> list = new ArrayList();
```

위와 같이 선언하면, 컴파일러는 list에 String 타입의 값만 넣어야 함을 인지할 수 있게 된다. 따라서, 아무런 오류 없이 컴파일이 된다면 의도대로 동작할 것을 보장한다. 

위 list에 Integer나 ArrayList같은 것을 넣으려고 시도한다면 컴파일 오류가 발생하고, 무엇이 잘못됐는지를 정확히 알려준다.

또한, 컴파일러는 컬렉션에서 원소를 꺼내는 모든 곳에 보이지 않는 형변환을 추가 해줌으로써, 절대 실패하지 않음을 보장한다.

```java
// 바이너리 코드에서의 제네릭
List<String> strList = new ArrayList();  
strList.add("스트링!!!!!!");  

System.out.println((String)strList.get(0)); // 컴파일러가 자동으로 추가해준 형변환
```


그럼에도 불구하고 로타입을 쓴다?
당신은 타입 안정성, 표현력과 같은 제네릭의 장점을 잃어버리는 것이다.

그놈의 하위 호환성 때문에 제네릭 클래스의 로 타입을 만들어놨다고 한다. (자바가 제네릭을 받아들이기 까지 10년이 걸렸기 때문이라고...)

만약, List에 모든 타입을 허용하고 싶다면 아래와 같이 작성하자

```java
List<Object> objList = new ArrayList<>();
```

로타입인 `List`와 위 코드의 차이는 무엇일까
로타입은 제네릭 타입에서 완전히 발을 뺀 것이고, `List<Object>`는 모든 타입을 허용한다는 의사를 컴파일러에 명확히 전달한 것이라고 한다.

또 큰 차이가 있는데...

```java
// 로 타입을 받는 메서드
static void printRaw(List rawList) {  
    System.out.println(rawList);  
}

// List<Object>를 받는 메서드
static void print(List<Object> objList) {  
    System.out.println(objList);  
}

List<String> strList = new ArrayList<>();
List<Object> objList = new ArrayList<>();  

printRaw(objList);  // OK  
printRaw(strList);  // OK

print(strList);  // 컴파일 에러 남
print(objList);  // OK
```

메서드의 파라미터에 로 타입을 선언하면 어느 리스트던 간에 넘어올 수 있지만,
`List<Object>`를 선언하면 `List<Object>`만 넘어올 수 있다.

하지만, 메서드에서 `List<Object>`가 아닌, 정말 모든 타입의 List를 받고 싶을 수 있다. 그럴 때는 아래와 같이 비한정적 와일드카드 타입을 사용하도록 하자.

```java
static void printWildCard(List<?> rawList) {  
    System.out.println(rawList);  
}

printWildCard(objList);  // OK  
printWildCard(strList);  // OK
```

근데 `List<?>`가 로 타입이랑 다른게 뭘까?

```java
// List<?>에는 null 외에는 아무것도 넣을 수 없다.
List<?> wildList = new ArrayList<>();  
wildList.add(null);  // OK
wildList.add("hi");  // 컴파일 에러
wildList.add(1);     // 컴파일 에러
wildList.add('h');   // 컴파일 에러
```

- 와일드 카드 타입은 안전하고, 로 타입은 안전하지 않다.
	- 와일드 카드 타입은 null외에는 아무것도 넣을 수 없는 타입이 된다.
	- 로 타입은 닥치는 대로 아무거나 다 넣을 수 있다.

- 와일드 카드 타입이 null 외에는 아무것도 넣을 수 없다
- == 컬렉션에서 꺼낼 수 있는 객체의 타입도 전혀 알 수 없다.


로 타입을 사용하지 말라는 규칙에는 몇 가지 소소한 예외가 있다.
- class 리터럴에는 로 타입을 써야한다.

```java
List.class;          // OK
List<String>.class;  // 컴파일 에러
List<?>.class;  // 컴파일 에러
```


- `instanceOf`연산자는 비한정적 와일드카드 타입 이외의 매개변수화 타입에는 적용할 수 없다.
	- 런타임에는 제네릭 타입의 정보가 지워지기 때문이다.
	- 로 타입이든, 비한정적 와일드카드 타입이든 `instanceOf`는 똑같이 동작한다.
	- 즉, `instanceOf`에는 그냥 로 타입을 사용하자.
	- 주의 : o의 타입이 Set임을 확인한 뒤, `Set<?>`로 형변환 해야한다.

```java
static void check(Object o) {  
    if (o instanceof Set<String>) {  // Illegal generic type 
        Set<?> s = (Set<?>) o;  
    }
}

static void check(Object o) {  
    if (o instanceof Set) {    // OK
        Set<?> s = (Set<?>) o;  
    }
}
```


결론은, "로 타입은 class를 뽑을 때와 instanceOf에서 타입 체크를 할 때 빼고는 사용하지 말자." 입니다.

사실 저는 과거의 자바(11미만)에 대한 경험이 없어서, 이번 단원은 너무 당연한(?) 얘기였습니다. 로타입을 사용하는 곳은 자바도 5 미만을 사용할텐데.. 만나지 않길 기도해야겠습니다.

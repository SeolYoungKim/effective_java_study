# 이왕이면 제네릭 타입으로 만들라

### 제네릭 타입으로 바꿔야 하는 이유

<br/>

```
public interface InterfaceA{
    public Object search (String x):
}

```





위 처럼 모든 객체의 상위객체인 Object 형으로 코딩을 하는경우
타입관리의 엄밀함이 떨어지게 된다 .

따라서 아래와 같이 제네릭을 사용해주면 타입관리의 장점을 취할수 있다.

```
public interface InterfaceA<E,T>{
    public T search (E x):
}
```


다만 위처럼 변환시 기존 new Object[] 같은 방식으로 생성된 부분들은 오류가 날수 있는데
이는 제네릭 자체가 실체화 불가 타입이기에 발생하는 오류이다.

<br/>

좀더 구체적인 이해를 위해 예제를 확인하자

```
public class Stack{
    private Object[] elements;
    private int size =0;

    public Stack(){
        elements = new Object[16]
    }

    public E pop(){
        Object result =elements[-size]
    }
}
```
변환에 필요한 부분을 제외하고는 모두 생략하였다.

위의 코드의 경우 Object 로 처리하는 부분이 많기에 타입 관리의 장점을 살리기 위해 제네릭으로 바꾸면 오류가 난다.

```
public class Stack<E>{
    private E[] elements;
    private int size =0;

    public Stack(){
        elements = new E[16]
    }

    public E pop(){
        E result =elements[-size]
    }
}
```

위의 코드중 elements = new E[16] 부분에서 오류가 나는 이유는 제네릭이 실체화 되지 않기 때문이다. 
이를 해결하기 위해서는

1. 오브젝트로 생성한 후 제네릭으로 변경 후 오류 감추기
   @SuppressWarning("unchecked)
   public Stack(){
   elements = (E[]) new Object[];
   }

위와 같이 변환시 해당 값이 저장되는 elements 필드는 private 이기에 해당 객체 내에서만 사용되므로 안전성이 보장된다. 다만 컴파일러는 해당 타입이 안전한지 증명할 방법이 없기에 우리 스스로 해당 부분을 확인해야한다


2. 필드의 타입을 E[] 에서 Object[] 로 바꾸는 것

위의 경우에 따르면 E[] 로 맴버변수가 제네릭 형변환 상태로 저장되기에 배열의 요소를 사용하는경우 오류는 발생하지 않는다.
이와 반대로 멤버변수를 Object 배열로 받은다음 해당 배열에서 값을 추출해서 사용하는 경우 형변환 시키는 방법도 존재한다

@SuppressWarning("unchecked) E result =(E) elements[-size]; 


이 두가지 경우는 모두 지지를 받고 있으며 첫번째 방법은 
비교적 현업에서 많이 쓰이고 있다 (형변환 한번만 해주면 되기에 간편함)

다만 해당방법은 배열의 런타임 타입이 컴파일타임 타입과 달라 힙 오염을 일으키기에 두번째 방법을 고수하는 프로그래머도 있다.
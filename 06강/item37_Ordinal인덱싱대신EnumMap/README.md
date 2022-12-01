## Item 37 : Ordinal 인덱싱 대신 EnumMap을 사용하라
### Conclusion
enum값으로 원소를 그룹화 시켜야 할 필요가 있을때,    
enum의 Ordinal 에 의존하지 말고, enum 값 자체를 이용하여 그루핑하라.

### In case
item 34, 35에 이어 Ordinal값을 이용하여 개념적인 프로그램을 짜기보다는  
자바가 지원하는 기능을 이용하여 명시적인 프로그램을 작성하여 성능과 유지보수성을 늘리기를 권고한다.

![](https://raw.githubusercontent.com/mycode01/linkimages/master/effective_java/efj_item37_01.png)
EnumMap 의 선언부이다.  
아시다시피, Enum 은 추상타입이고,   
우리가 선언하는 enum은 그것을 상속한 하위타입이기 때문에 `K extends Enum` 이고, V 는 원소들을 담기 위한 타입으로 선언되어 있다.

책에 적혀있는대로, EnumMap은 내부 배열을 사용하므로  
아래의 `Object[] vals` 는 V를 담기위한 내부 배열이다.

![](https://raw.githubusercontent.com/mycode01/linkimages/master/effective_java/efj_item37_02.png)

직접 값을 map에 넣는 put 의 구현부이다.  
주어진 Key, 그러니까 enum의 ordinal 을 배열의 인덱스삼아 배열의 위치에 V를 할당하고 있는 부분이다.

책에서는 다음과 같은 코드를 사용하지 말라고 하고 있는데, 
![](https://raw.githubusercontent.com/mycode01/linkimages/master/effective_java/efj_item37_03.png)
배열을 이용해 그룹을 짓는데는 성공하였으나,  
- List가 실질적으로는 raw 타입이라는것(선언이 아닌 실체화가 되기 위한 ArrayList가)
- 배열의 인덱스가 Ordinal 값이라는 것은 알겠으나, 명시적이지 못하여 가독성이 좋지는 못하다
두가지 단점이 있다. 

아마 이따위 코드를 왜 작성해? 그냥 Map 쓰면 되는거 아님? 하고 순간적으로 떠올린 사람들이 많을텐데, 맞는 이야기다.

![](https://raw.githubusercontent.com/mycode01/linkimages/master/effective_java/efj_item37_04.png)
위의 아이디어에서 착안한 Map의 생성이다. 

groups1 은 일반적인 groupingBy를 이용한 Map의 생성,  
groups2 는 EnumMap 으로 만들었을 경우이다.

실 구현체가 HashMap(Collectors.groupingBy 기본 전략), EnumMap으로 차이가 있는데, 저자는 EnumMap의 이용을 추천한다.

사실 Ordinal 을 인덱스로 하는 grouping 을 이용하는 이유는 퍼포먼스 말고는 다른 이유가 없다.  
그리고 이미 EnumMap은 내부적으로 Ordinal 을 이용한 배열을 사용하고 있으므로  
기본적으로 put이나 get을 할때마다 hash를 실행하는 HashMap보다 성능이 좋을수밖에 없다.  
작가는 이런 부분을 두고 HashMap을 이용하는것에 대해 '성능과 공간 잇점을 잃어버렸다' 표현하였다.  

자. 이제 여러분은 자바가 기본적으로 제공하는 Map 의 구현체 HashMap, LinkedHashMap, TreeMap, ConcurrentMap에 추가로 특수목적 Map인 EnumMap 을 안다고 이야기할수 있게 되었다.


또, 책에 나와있는 Enum을 두개 사용하는 방식의 경우 도저히 예를 들만한 상황이 생각이 나지 않아 대충 코멘트로 보충합니다.  
scratch_40.java 참고

## in my opinion
너무 간단한 내용이라 정리할만한 내용이 없습니다.

굳이 지적하자면 Collectors.groupingBy 를 이용하는 경우 Enum이 선언되어 있으나 실제 그 값에 해당하는 원소가 없을 경우 비어 있는 그룹은 생성되지 않으므로 주의 해야한다는 점?

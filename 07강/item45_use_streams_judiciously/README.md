## Item 45 : 스트림은 주의해서 사용하라 
### Conclusion
스트림을 사용해야할 때와 그렇지 않을때를 구분하라 

### In case
스트림 API의 추상 개념 두가지
- 원소의 유한 혹은 무한의 시퀀스
- 스트림 파이프라인
파이프라인은 단계별 출력이 다음 단계의 입력값이 되는 형태로 이어지는 구조를 말함.  
파이프라인은 지연 평가(lazy evaluation) 라는 특성이 있다. 
![](https://raw.githubusercontent.com/mycode01/linkimages/master/effective_java/efj_item45_04.png)


스트림의 대상은 무엇이든 될수있다.  
컬렉션, 배열, 파일 - 여기까지가 많이 사용해본것들이고,   
regex 패턴 매쳐, 난수 생성기, 다른 스트림

![regex 패턴 매쳐의 결과 스트림](https://raw.githubusercontent.com/mycode01/linkimages/master/effective_java/efj_item45_01.png)
![Random에 의해 생성된 스트림](https://raw.githubusercontent.com/mycode01/linkimages/master/effective_java/efj_item45_02.png)
![스트림 두개를 합친 스트림](https://raw.githubusercontent.com/mycode01/linkimages/master/effective_java/efj_item45_03.png)

스트림은 플루언트 패턴을 이용한 API이다 => 메소드 체이닝을 사용한다.

스트림에 사용하는 람다나 메소드 레퍼런스으로는 바깥 스코프의 지역변수를 재할당하는 것이 불가능하고  
<s>스트림은 반복문 도중에 pass, return 의 사용이 불가능하다.</s> >> 스트림은 가능하지만 람다가 불가능  
람다는 break continue return 을 사용해서 스코프 바깥의 반복문을 제어할수가 없다.

위에서 기술한 특성들때문에 생기는 오해들에 주의하며 코드를 작성하자.

또, IntStream, LongStream, DoubleStream 의 스트림이 표준 제공되고 있고, 그에 따른 op도 제공하고있으니 사용하자.

스트림 vs for-loop

|조건| 스트림  | for-loop  |
|---|:----:|:---------:|
|데이터를 다루는데 유리한가|  x   |     o     |
|자료구조의 흐름을 다루는데 유리한가|  o   |     x     |
|primitive타입에 유리|  x   |     o     |
|루프안에서 다른 인덱스에 접근|  x   |     o     |
|가독성|  -   |     -     |
|디버깅 난이도|  상   |     하     |
|패러다임| 함수형  |   절차지향    |



--- 



## in my opinion
스트림은 남용하면 지저분해보인다. 기본적으로 람다를 사용하기 때문.  

스트림 메소드 체인이 스트림을 리턴한다고 해서 해당 스트림을 재사용 할수있는것은 아니다.  
메소드 체이닝을 위해서 자신의 객체를 리턴할뿐, 새로운 객체를 만들어 리턴하는것이 아님.

forEach() 를 최대한 덜 사용하라. 평가가 이루어지기 때문에 스트림답게 사용하기가 힘들어진다.  
내부 값 확인이 필요하다면 peek() 를 사용하자.

스트림과 for-loop가 어울리는 곳이 다르다.  
57,58,59,60 참고

빠지지 않는 성능 이슈

참고 

https://ryan-han.com/post/dev/java-stream/

https://jypthemiracle.medium.com/java-stream-api%EB%8A%94-%EC%99%9C-for-loop%EB%B3%B4%EB%8B%A4-%EB%8A%90%EB%A6%B4%EA%B9%8C-50dec4b9974b
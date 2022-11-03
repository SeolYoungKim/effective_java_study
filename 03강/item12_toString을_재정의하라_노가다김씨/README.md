## Item 12 : toString() 을 항상 재정의 하라  
### Conclusion
구체 클래스를 작성할땐 `toString()` 메소드를 구현하라.  

객체가 가진 '주요'정보를 명확하고 (사람이) 읽기 좋게 반환해야한다.  
'읽기좋다' 라는 뜻은 반드시 포멧을 지정해야 한다는 뜻이 아니다.


### In case
여러 클래스를 작성하며 디버깅을 해야할일이 많았을 것이다.  
![](https://raw.githubusercontent.com/mycode01/linkimages/master/effective_java/efj_item12_02.png)  
실제로 위와 같은 코드를 사용했음을 본인이 가진 타 프로젝트 소스에서 발견했으며,   
logger가 내부적으로 info 로그를 찍기 위해서 `ArtworkType`(enum)<sup>[1](#fn_01)</sup>, `ArtworkId`(concret class)의 toString()을 이용하고 있다.  
만약 `ArtworkId` 클래스가 `toString()` 을 재정의 하지 않았더라면 해당 로그는 의미 없는 클래스명과 해시코드의 조합으로 출력될 것이다.

***

![](https://raw.githubusercontent.com/mycode01/linkimages/master/effective_java/efj_item12_03.png)  
obj1은 익명클래스로 `Object`의 `toString()` 을 재정의 하였고,  
obj2는 `Object` 클래스를 그대로 사용하여 `println()` 의 파라메터로 주어졌을시  
객체가 가진 name 멤버를 출력하거나, `Object`의 `클래스명@hashcode` 가 출력되었다.

***

![](https://raw.githubusercontent.com/mycode01/linkimages/master/effective_java/efj_item12_04.png)  
특히, Collection 을 사용할때 굉장히 편하게 내용을 볼수 있다.
![](https://raw.githubusercontent.com/mycode01/linkimages/master/effective_java/efj_item12_05.png)  





### in my opinion
이미 우리는 로그를 쌓거나, 테스트나 디버그시에 콘솔에서 결과를 확인하기 위해서 알게 모르게 `toString()` 을 사용하고 있었다.   
예를들면 + 오퍼레이터나, 로그 작성시가 그렇다.   


저자는 `toString()` 을 작성하는 가이드 4개를 제시하고 있는데, 
1. '모든' 유익한 정보를 리턴해야한다.
2. 자기 설명적이어야한다.
3. vo일 경우 반환 값에 대한 포멧을 명시하기를 추천한다.
4. toString이 제공하는 값은 프로그램적으로 접근할 방법을 제공해야한다.

특히, 3번의 경우 값을 이용하는 객체가 아니라 VO 그 자체일 경우 각각을 getter로 가져와서 포멧을 맞춰 사용하는것도 좋지만, 애초에 포멧을 맞춘 상태로 리턴하게끔 만들라는 이야기로 들린다.  

예를들면, 본인은 예전에 결제 정보를 csv형태로 월말즘에 청구 시스템에게 넘기는 작업을 프로그램으로 만든 적이 있었다.   
각 데이터 객체를 getter와 String.fomat 을 이용해서 생성하기보다,
csv 출력용도의 VO를 생성하고, csv 형태로 뱉어내는 toString 을 재정의 해서 사용했었으면 됐을것이라고 생각이 든다. 

4번은 `toString()` 으로 리턴되는 값은 모두 getter가 있어야한다는 뜻으로,
그렇지 않으면 `toString()` 이 뱉어내는 값을 파싱해서 사용하는 일이 벌어질수 있다고 한다.  
너무 쉬운 이야기 뿐이라 억지로 늘리는것도 한계입니다...

***

<a name="fn_01">1</a>: enum은 `Enum` 타입에 상속구조를 가지며, `enum` 타입은 내부적으로 `toString()`을 다음과 같이 정의하고 있다.
```java
public String toString() { return name; }
```
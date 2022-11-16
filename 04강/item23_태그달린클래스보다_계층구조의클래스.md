## Item 23 : 태그달린 클래스 대신 계층구조의 클래스를 생성하라
### Conclusion
내부 멤버 객체를 태그삼아 여러개의 의미를 가지는 클래스를 생성하지마라.  
비효율적이며, 확장하기 쉽지 않으며 런타임 오류를 내기 쉽다.  

### In case
일단 '태그달린 클래스' 라는 말을 정의 하자.
```java
class Figure {
   enum Shape { RECTANGLE, CIRCLE };
   final Shape shape;
   ...
   double area() {
      switch(shape) {
         case RECTANGLE:
            return length * width;
         case CIRCLE:
            return Math.PI * (radius * radius);
         default:
            throw new AssertionError(shape);
      }
   }
}
```
책의 코드를 모두 옮기지 않고, 단어 정의를 위해 일부만 발췌하였다.    
내부 멤버(태그)에 따라 행위가 결정되는 클래스이다.(태그? 플래그?)   
위의 enum shape 에 따라 Figure가 가진 행위인 area() 메소드가 다른 계산을 하게 된다. 

일단 좋지 않은 점만 이야기 하자면 
- 여러 구현이 한 클래스에 집중되므로 가독성이 좋지 않고,  
- 태그에 따라 사용하지 않아도 될 멤버까지 초기화를 해줘야 하고(메모리, 가독성),  
- 태그에 새로운 의미가 추가되었을때 확장이 쉽지 않다(기존코드를 수정해야하므로).

책에서는 서브타이핑을 이용한 계층구조가 있는 클래스로 만드는 법을 알려준다.  
짧게 줄이자면,  
1. 슈퍼타입을 정의하고(추상, 인터페이스)
2. 태그값에 따라 달라지는 행동을 추상 메서드로 정의한다.
3. 태그값에 해당하는 서브타입을 정의하고, 슈퍼타입을 상속(혹은 구현)한다.

이렇게 해서 얻을수 있는 이득은
- 서브타입 각각의 생성자가 존재하기 때문에 불필요한 멤버 초기화가 없음
- 태그에 새로운 의미가 추가되었을때(즉 새로운 타입이 생겼을때) 다른 타입에 영향을 주지 않고 확장이 쉽다



## in my opinion
약간 노련한 개발자라면 이미 책의 저자가 예로 든 코드자체가 리팩토링 대상임을 눈치챘을것이다.  

위의 클래스는 태그달린 클래스이기 때문에 문제가 있는것이 아니라,   
클래스가 하는 역할이 여러 개이므로 문제가 있는것이라고 생각한다.

이런 저런 이유 댈것없이 Figure는 두가지 역할을 하는 클래스이고, 추후 확장이 될것이 분명하기 때문에 인터페이스로 만드는것이 좋다.  
어느 곳에서는 원으로, 어느 곳에서는 직사각형으로서 존재하기 때문에 면적을 구하는 area() 내부에서 switch 같은 분기문이 존재하게 되고,  
추가적인 메서드가 정의되어야 할때도 원으로서, 직사각형으로서의 고려가 이루어진 메서드를 추가해야한다.

분기문때문에 발생하는 복잡성과 추후 추가되어야 할 메서드 혹은 타입의 확장성을 고려해서 클래스를 만들자.  
변하지 않는 것은 '변하지 않는 것은 없다' 라는 원칙 말고는 없다.  
작은것이 아름답다.  
두가지 프로그래밍계 격언을 떠올리며, 이만 마칠까 했는데...  


생각해보니 사실 본인도 이와 같은 클래스를 마주친적이 있다.

판매자 정보를 핸들링하는 엔티티였는데,  
판매자 타입에 따라서 생성 및 저장하는 정보가 다르고,   
타입에 따라 사용자에게 보내야 할 이메일의 내용이 달랐다.  
이외에도 어플리케이션 많은곳에서 판매자 타입에 의존하는 메서드들이 있었고,  
이 모든 행동에 대한 정의를 엔티티에서 할수 없기 때문에 태그값이 정의된 클래스를 만든거같다.  
(패키지 구조가 달랐을뿐 전통적 방식의 mvc패턴을 이용한 어플리케이션 이었으므로 엔티티의 모양이 어땠을지는 상상이 될거라 믿는다.  
다른점이라면 예제처럼 하나의 클래스 안에서 처리를 하지는 않았다)

바람직한 방법은 처리 행동 하나하나에 대해 도메인 클래스를 만들고 엔티티에게서 값을 얻어와 세팅 한 후 처리를 하는것이 맞지만,   
팀마다 컨벤션(코딩 관례뿐만 아니라 버릇이나 관성도 포함한다)이라는것이 존재하고,   
클래스 파일이 많아지는거 자체는 안티패턴이 아니라고들 하지만 비대해지는 프로젝트, 점점 어려워지는 패키지 구조잡기, 클래스 이름짓기를 생각했을때, 이런 태그값이 있는 클래스를 만들고 서비스 핸들링시에 if 분기를 태우는것이 가장 편하다고 생각했다.

아마 같은 상황이 온대도 간단하지 않은 혹은 앞으로 어떤 요구사항이 올지 모르는 상황에서는 또 그리하지 않을까 싶다.

아래는 해당 계좌정보 저장에 대한 부분을 리뉴얼 할 기회가 있어 다시 작성한 메소드인데,  
클래스 관계 자체를 수정한다면 여러곳에서 영향을 받기 때문에 클래스 관계에 끌려다니다시피 작성되었다.
```java
class SellerInfoFacade {
  SellerInfoService sellerInfoService;
  CreatorUserService creatorUserService;
  AccountEvents accountEvents;
  ...
  // SellerInfoType 은 enum 이며, BankAccount 를 생성하는 등의 메소드를 가지고 있음.
  void 계좌정보저장(UserId creatorId, SellerType sellerInfoType,
      계좌정보 info) {
    BankAccount bankAccount = sellerInfoType.buildBankAccount(sellerInfoService, creatorId,
        info); // 사실상 if 분기, 
    // 책의 figure에 해당하는 부분이다. 
    // info는 간단하게 표현한것이고, 실제는 많은 갯수의 파라메터이다. 
    // 타입에 따라 필요 없는 정보도 입력받고 있으며, 그 경우엔 null값이나 emptystring이 넘겨진다.

    SimpleAssert.notNull(bankAccount,
        ERR_MSG_FORMAL_BANK_ACC_INFO_CANNOT_REGISTER,
        WrongParameterException.class);
    // 입력받은 정보가 악의적으로 편집되어 보내졌을 경우 계좌정보를 생성하지 못하고 null을 리턴하게끔 작성되어 있기 때문에 여기서 catch하여 throw시킴

    Creator creator = creatorUserService.findByUserId(creatorId);

    sellerInfoService.deleteCreatorBankAccount(creatorId);
    sellerInfoService.saveCreatorBankAccount(creatorBankAccount);
    sellerInfoType.decideSellerType(creator);

    accountEvents.creatorWasUpdated(creator);
    타_플랫폼으로_이벤트_전파(creatorId);
  }
}
---
enum SellerInfoType {
  PERSONAL_SELLER{
    @Override
    public BankAccount buildBankAccount(
        SellerInfoService service, UserId creatorId, 계좌정보 info)
    { return service.individualBankAccount(info1); }

    @Override
    public void decideSellerType(Creator creator) {
      creator.makeSellerTypePersonal();
    }
  },      // 개인 판매자
  SOLE_PROPRIETORSHIP{
    @Override
    public BankAccount buildBankAccount(
        SellerInfoService service, UserId creatorId, 계좌정보 info)
    { return service.companyBankAccount(info1,2,3,4); }

    @Override
    public void decideSellerType(Creator creator) {
      creator.makeSellerTypeCompany();
    }
  },  // 개인 사업자
  ...
}
```



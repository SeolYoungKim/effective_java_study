package org.example;

public class AppMain {

  // 119 페이지 발췌
  // 레퍼클래스는 단점이 거의 없다. 한가지, 래퍼클래스가 콜백 프레임워크와는 어울리지 않는다는 점만 주의 하면 된다.
  // 프레임워크 라는 단어에 현혹되지 마시고, 콜백 구조의 어플리케이션 구성에서는 캡슐화가 깨지게 되므로 주의 해야 한다는 뜻이다.
  public static void main(String[] args) {

    var owner = new Owner();
    // 주인이 만들어짐

    // 애완동물은 태어나자마자 주인에게 입양됨.
    var pet = new Platypus(new Mammal(owner));
    // 왜 이런 구조를 해야하는가?
    // 주인은 펫을 가질수 있고, 펫도 주인을 알아보잖아!
    // 둘의 관계성을 맺어주는 부분이라고 생각해야함.
    // 이는 jpa에서 연관관계를 맺어줄때 자주 볼수 있는 패턴이다.
    // 정확히는 new Platypus(owner) 가 되어야겠지만,
    // 오너가 정확한 타입을 명시했을경우이다.
    // 이해가 잘 안된다면 jpa에서 연관관계를 맺어줄때를 생각해보자
    // n:m 일때, n이 m을 정확한 타입으로 인자를 삼고 있다면 상속을 하지 않은 합성으로는 답이 없다.

    owner.애완동물의_이름은();
    pet.당신은_누구입니까();
    if (pet.owner() == owner) {
      System.out.println("펫은 주인을 알아봅니다.");
    }
    if(pet.hashCode() == owner.pet().hashCode()){
      System.out.println("하지만 같은 펫일까요?");
    } else {
      System.out.println("상속 관계조차 아니기 때문에 pet == owner.pet() 도 불가합니다.");
    }
  }
}
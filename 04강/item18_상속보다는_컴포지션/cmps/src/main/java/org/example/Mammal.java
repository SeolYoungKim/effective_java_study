package org.example;

public class Mammal { // 이 장에서 이야기는 interface가 아니라 구체 클래스에 대한 상속 이야기기 때문에 굳이 class 사용
  private final static String name = "포유류";

  private Owner owner;


  public Mammal(Owner owner) {
    this.owner = owner;
    this.owner.펫을_입양함(this);
  }

  public String name(){
    return this.name();
  }

  public Owner owner(){
    return owner;
  }

  public boolean 알을_낳는가(){
    return false;
  }

  public boolean 털이_있는가(){
    return true;
  }

  public boolean 부리가_있는가(){
    return false;
  }

  // 조합? 합성? 에서는 정확한 타입을 리턴할수가 없기때문에
  // (상속을 한게 아니니까 is-a 구조가 아니기 때문에) 어쩔수 없이 필요할때는
  // 내부 멤버 객체를 리턴할 수 밖에 없다.
  // 문제는 이게 콜백 패턴일때 내부 객체를 넘겨주게 되는데,
  // 이때 sub(조합) 객체의 메소드를 찾지 못하는 문제가 있다.
  public void 당신은_누구입니까(){
    System.out.println("나는 "+name+"입니다.");
  }
}

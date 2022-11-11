package org.example;

public class Platypus {

  private final static String name = "오리너구리";
  private final Mammal _super;
  // 의미적으로는 상속이 아니기 때문에 super는 아니지만..



  public Platypus(Mammal _super) {
    this._super = _super;
  }

  public Owner owner(){
    return _super.owner();
  }
  public String name() {
    return this.name();
  }

  public boolean 알을_낳는가() {
    return true;
  }

// 오리너구리는 털이 있으므로 멤버의 클래스를 그대로 사용함.

  public boolean 부리가_있는가() {
    return _super.부리가_있는가();
  }

  public void 당신은_누구입니까() {
    System.out.println("나는 "+name+"입니다.");
  }
}

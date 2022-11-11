package org.example;

public class InheritedPlatypus extends Mammal{

  private final static String name = "오리너구리";
  private final Mammal _super;

  public InheritedPlatypus(Mammal _super) {
    super(_super.owner());
    this._super = _super;
  } // 이부분은 예를 맞추려다보니 억지가 좀 있네요.

  @Override
  public String name() {
    return _super.name();
  }

  @Override
  public Owner owner() {
    return _super.owner();
  }

  @Override
  public boolean 알을_낳는가() {
    return true;
  }

  @Override
  public boolean 부리가_있는가() {
    return true;
  }

  @Override
  public void 당신은_누구입니까() {
    System.out.println("나는 "+name+"입니다.");
  }


  // 이러면 안되지만, 비교 테스트를 위해 원본을 제공하도록 작성함
  public Mammal getOrigin(){
    return _super;
  }
}

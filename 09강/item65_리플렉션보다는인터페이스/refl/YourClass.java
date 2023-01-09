package com.example.scratcher.refl;

public class YourClass implements CoreInterface {

  private String base = "its your class! ";

  @Override
  public void doSomeThing(int a) {

    System.out.println("input param : " + a);

  }
  // 디폴트 메서드는 구현하지 않겠음

  private void doSomeThingPrivately() {
    System.out.println("telling you your dirty secret");
  }
}

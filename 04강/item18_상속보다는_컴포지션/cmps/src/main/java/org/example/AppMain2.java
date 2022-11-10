package org.example;

public class AppMain2 {
  // 그렇다면 어떻게 해결하라는 뜻인가?
  // 뭘 어떻게해... 상속 + 컴포지션이지


  public static void main(String[] args) {
    var owner = new Owner();
    var pet = new InheritedPlatypus(new Mammal(owner));

    owner.애완동물의_이름은();
    pet.당신은_누구입니까();
    if (pet.owner() == owner) {
      System.out.println("펫은 주인을 알아봅니다.");
    }
    if (pet == owner.pet()) {
      System.out.println("하지만 같은 펫일까요?");
      System.out.println("내! 맞워요!");
    } else {
      System.out.println("이쪽은 진짜 상속 구조이므로 == 비교도 가능합니다.");
    }
    System.out.println(pet.부리가_있는가());
    System.out.println(pet.알을_낳는가());
    var original = pet.getOrigin();
    System.out.println(original.부리가_있는가());
    System.out.println(original.알을_낳는가());

  }
}

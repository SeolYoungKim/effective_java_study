import java.time.LocalDateTime;
import java.util.Objects;

class Scratch {

  public static void main(String[] args) {
    String str = new String("너 사탄들렸어?");
    System.out.println("str :: " + Objects.hashCode(str));
    str += "아닙니다 목사님 매일같이 회개하고 뉘우치고 있습니다";
    System.out.println("str :: " + Objects.hashCode(str));
    // immutable object
    System.out.println();

    StringBuilder sb = new StringBuilder("아이고 우리 강프로 ");
    System.out.println("sb :: "+Objects.hashCode(sb));
    sb.append("식사는 잡쉈어?");
    System.out.println("sb :: "+Objects.hashCode(sb));
    System.out.println("sb.toString :: "+Objects.hashCode(sb.toString()));
    // mutable object
    // 사실, StringBuilder 는 String이라는 불변 객체를 보조하기 위한 컴패니언 클래스다.
  }
}
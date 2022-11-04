import java.util.Arrays;
import java.util.stream.Collectors;

class Scratch {

  public static void main(String[] args) {

    var msg = "청계천의 피라냐는 기회주의자들의 고기를 아주 좋아한다.";
    var list = Arrays.stream(msg.split("\\s")).map(SomeStrContainer::new)
        .collect(Collectors.toList());

    System.out.println(list);
    
  }
  static class SomeStrContainer{
    private final String str;

    public SomeStrContainer(String str) {
      this.str = str;
    }

    @Override
    public String toString() {
      return "{" +
          "str='" + str + '\'' +
          '}';
    }
  }
}
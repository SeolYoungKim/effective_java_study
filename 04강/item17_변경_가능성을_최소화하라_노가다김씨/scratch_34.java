import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Scratch {

  public static void main(String[] args) {
    List<String> elements = new ArrayList<>();
    elements.add("coffee");
    elements.add("popcorn");

    var failed = new FailedImmutable(elements);
    var expectImmutable = new ExpectImmutable(elements);

    elements.add("anything");

    System.out.println(elements);
    failed.print(); // 2. 결과적으로 FailedImmutable 는 내부 멤버를 공개하지 않았음에도 불변함을 깨트리게 되었음.
    expectImmutable.print(); // 3. 제대로 구현된 불변객체는 멤버와 외부의 참조가 차단되므로 안전함.

  }

  static class FailedImmutable {
    private final List<String> list;

    FailedImmutable(List<String> list) {
      this.list = list;
    } // 1. 외부에서 전달받은 파라미터를 그대로 참조함
    public void print(){
      System.out.println(list);
    }
  }

  static class ExpectImmutable{
    private final List<String> list;

    ExpectImmutable(List<String> list) {
      this.list = new ArrayList<>(list);
//      this.list = Collections.unmodifiableList(list);
    } // 가능한부분은 불변이도록 만드는게 좋지만, 용도에 따라 결과적으로 불변함을 만족시킬수 있다면 가변 멤버를 만들어도 괜찮다.
    public void print(){
      System.out.println(list);
    }
  }
}
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Scratch {

  public static void main(String[] args) {

    var list = new FailedImmutable(new ArrayList<>());
    list.add("123");
    list.add("321");
    var innerList = list.getList();
    System.out.println(innerList);
    list.add("999");
    System.out.println(innerList);
    var expectUnmodifiable = list.getUnmodifiable();
    expectUnmodifiable.add("000"); // cause exception
  }

  public static class FailedImmutable {

    private final List<String> list;

    public FailedImmutable(List<String> list) {
      this.list = list;
    }

    // 일급 컬렉션을 만든답시고 하게 되는 실수, 일급 컬렉션은 불변함을 보장해야한다.
    public void add(String s) {
      list.add(s);
    }

    // 내부 가변 멤버의 참조를 얻을수 있도록 허용해서는 안됨
    public List<String> getList() {
      return list;
    }

    // 방어적 복사로 제공하는 내부 상태
    // 불변함을 지키는 방법은 크게 두가지가 있는데, 방어적 복사와 변경을 허용하는 메소드를 제공하지 않는것이다.
    // 아래와 같은 방어적 복사는 unmodifiable이기 때문에 안전하겠지만, 딥카피를 하지 않는 컬렉션을 사용할때는 유의해야한다.
    public List<String> getUnmodifiable(){
      return Collections.unmodifiableList(list);
    }

  }
}
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

class Scratch {

  // @formatter:off
  public static void main(String[] args) {
    Stream.generate(dummy())
        .filter(e-> e > 0).limit(5).map(slice())
        .forEach(System.out::println);

    // 이하로 변경이 가능하다.
    var source = dummy();
    var slicer = slice();
    int c = 0;
    while(c!=5){
      long e = source.get();
      if (e < 0) continue;
      c++;
      System.out.println(slicer.apply(e));
    }

  }
  // @formatter:on

  private static Function<Long, String> slice() {
    return l -> {
      String s = String.valueOf(l);
      int len = s.length();
      return s.substring(len - 3, len);
    };
  }

  private static Supplier<Long> dummy() {
    var r = new Random();
    return r::nextLong;
  }
}
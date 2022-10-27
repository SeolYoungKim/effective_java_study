import java.time.Instant;
import java.util.Scanner;

class Scratch {

  public static void main(String[] args) {
    Scanner s = new Scanner(System.in);
    s.next();
    var start = Instant.now().getEpochSecond();
    System.out.println(sum());

    var end = Instant.now().getEpochSecond();
    System.out.println(end - start);
  }

  private static long sum() {
//    Long sum = 0L;
    long sum = 0L; // 이렇게 수정함으로 힙메모리 사용을 250mb 언저리에서 20메가 언저리로 줄일수 있었다.
    for (long i = 0; i <= Integer.MAX_VALUE; i++) {
      sum += i;
    }
    return sum;
  }
}
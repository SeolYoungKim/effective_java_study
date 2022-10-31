class Scratch {

  public static void main(String[] args) {
    System.out.println(Integer.valueOf(10) == Integer.valueOf(10));
    System.out.println(Integer.valueOf(1000) == Integer.valueOf(1000));

    Integer a = 10;
    Integer b = 10; // 컴파일러가 Integer.valueOf(10) 로 해석을 한다는 증거 
    System.out.println(a == b);
    Integer c = 1000;
    Integer d = 1000;
    System.out.println(c == d);
    // 이해할수 있는지? 100 == 100 은 true지만 1000 == 1000 은 false?
  }
}
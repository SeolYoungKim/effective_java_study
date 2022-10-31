class Scratch {

  public static void main(String[] args) {
    var obj1 = new Object(){
      private String name = "object1";
      @Override
      public String toString() {
        return name;
      }
    };

    var obj2 = new Object();

    System.out.println(obj1);
    System.out.println(obj2);
  }
}
class Scratch {

  public static void main(String[] arg){
    Mutable obj = new Mutable(4);
    Immutable immObj = (Immutable)obj; // 2. 이렇게 된 순간부터 immutable이 가변성을 가지므로 계약이 깨짐. 사용자는 Immutable의 불변함을 믿기 때문.
    System.out.println(immObj.getValue());
    obj.setValue(8);
    System.out.println(immObj.getValue());
  }


  public static class Mutable extends Immutable {
    private int realValue;

    public Mutable(int value) {
      super(value);
      realValue = value;
    } // 1. 이렇게 사용했을때, 적절한 예라고 생각하지 않을수 있지만..

    public int getValue() {
      return realValue;
    }
    public void setValue(int newValue) {
      realValue = newValue;
    }
  }

  public static class Immutable {
    private final int value;

    public Immutable(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

}
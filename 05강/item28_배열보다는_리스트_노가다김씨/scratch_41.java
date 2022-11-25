import java.util.ArrayList;
import java.util.List;

class Scratch {

  public static void main(String[] args) {
    MyList<SuperType> myList = new MyList<>();
    //  myList 자체는 아직도 불공변이지만, SuperType의 상속구조인 객체들에게는 공변으로 대함.
    myList.add(new SubType());
    List<Object> list2 = new ArrayList<>();
    list2.add("저는기어다닐거예요"); list2.add(123);
    myList.addAll(list2);
    List<MyInterface> list3 = new ArrayList<>();
    list3.add(new SuperType()); list3.add(new AnotherType());
    myList.addAll(list3);


//    List[] da = new ArrayList<String>[10]; // 자바는 제네릭타입을 가진 배열을 허용하지 않음, 배열은 런타임에도 타입정보가 필요한데, 제네릭 타입정보는 런타임에는 알수없기때문.
//    new E[10]; // 클래스에 제네릭 타입 E가 정의 되어 있어도 생성할수 없다.(line30에 복사붙여넣기 해보면 알수있음) 왜냐면 E가 기본 생성자를 지원하는지 안하는지 알수없기 때문.
    List[] dd = new ArrayList[10]; // raw 타입 arraylist를 10개만들어 배열에 할당
    List<?>[] df = new ArrayList[10]; // ?타입의 arraylist를 10개만들어 배열에 할당. ? 타입이기 때문에 의미있게 사용하기는 힘들다. 아래를 보자.
    df[0] = new ArrayList<String>();
//    df[0].add("먹을만하네요"); // df는 ? 타입의 arraylist 를 원소로 가지는 배열이다. ?는 소각되지 않지만 ?타입을 실제로 정의한 클래스는 없기때문에 null 말고는 넣을수가 없다.
  }

  public static class MyList<E extends SuperType> extends ArrayList<E>{
    // SuperType 를 extends 한 타입 E를 제네릭 타입으로 허용한다.
    @Override
    public boolean add(E e) {
      return super.add(e);
    }

    public boolean addAll(List<? super E> c) { //반공변
      for (var d : c ){
        System.out.println(d.getClass());
        if(d instanceof MyInterface){
          ((MyInterface) d).getName();
        }
        if(d instanceof AnotherType){
          ((AnotherType) d).getName();
        }
//        add(d);
// 파라미터인 c는 E의 super 타입만 허용하기때문에, E가 무엇임을 생각해보면 Object와 MyInterface만 허용한다는 뜻이다.
// 당연히 Object는 SuperType의 서브타입이 아니기때문에(E는 SuperType의 서브라고 한정지었다!)
// add(E e) 의 사용이 불가능하다.
      }
      return true;
    }
// 여기서 MyInterface 이긴 하지만 상속구조인 AnotherType이 들어올수있으니 이상한거같은데? 라고 생각할수 있으나,
// 여기서는 AnotherType 이 아닌 MyInterface 타입으로 들어온 것이다.
// 위에서 작성한것처럼 if instanceof 로 타입 체크후 메소드들을 사용할수 있으나,
// 이 경우 제네릭의 의미자체가 무색해지므로 지양하자.
  }

  public static class SuperType implements MyInterface {}
  public static class SubType extends SuperType {}

  public static class AnotherType implements MyInterface{}

  interface MyInterface {
    default void getName(){
      System.out.println("in getName :: "+this.getClass());
    }
  }

}
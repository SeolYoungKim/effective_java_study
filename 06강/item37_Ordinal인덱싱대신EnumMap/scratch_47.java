import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class Scratch {

  public static void main(String[] args) {

    Derived[] emoticons = dummy();

    // Ordinal을 인덱스로 하는 그룹을 생성한다.
    // 배열 0번에는 Ordinal 0 에 해당하는 동물콘,
    // 1번에는 Ordinal 1 에 해당하는 만두콘
    // 이런식으로 파생된 객체들을 리스트 타입으로 그룹을 짓는다.
    List<Derived>[] groups = new ArrayList[Emoticon.values().length];
    for (var d : emoticons) {
      int index = d.type.ordinal();
      if (Objects.isNull(groups[index])) {
        groups[index] = new ArrayList<>();
      }
      groups[index].add(d);
    }
    Arrays.stream(groups).forEach(System.out::println);
    //[Derived{type=동물콘, name='오들오들 동물콘'}]
    //[Derived{type=만두콘, name='만두콘1'}]
    //[Derived{type=창고콘, name='낡은창고콘'}, Derived{type=창고콘, name='짭창고콘1'}, Derived{type=창고콘, name='짭창고콘2'}]
    //[Derived{type=케장콘, name='케장콘1'}, Derived{type=케장콘, name='케장콘2'}, Derived{type=케장콘, name='케장콘3'}]
    //[Derived{type=아이카츠콘, name='아이카즈콘'}, Derived{type=아이카츠콘, name='아이마스콘'}, Derived{type=아이카츠콘, name='버튜버콘'}]
    // 성공적으로 enum의 순서를 간직한채 인덱스 자체로 그룹을 생성하였다.
    // 다만, List가 배열과 함께 사용되기 위해 raw 타입을 사용하고 있고, 배열의 인덱스가 Ordinal 이라는 건 알고 있지만 명시적이지 못하다.

  }

  private static Derived[] dummy() {
    Derived[] userCon = new Derived[11];
    userCon[0] = new Derived(Emoticon.동물콘, "오들오들 동물콘");
    userCon[1] = new Derived(Emoticon.만두콘, "만두콘1");
    userCon[2] = new Derived(Emoticon.창고콘, "낡은창고콘");
    userCon[3] = new Derived(Emoticon.창고콘, "짭창고콘1");
    userCon[4] = new Derived(Emoticon.창고콘, "짭창고콘2");
    userCon[5] = new Derived(Emoticon.케장콘, "케장콘1");
    userCon[6] = new Derived(Emoticon.케장콘, "케장콘2");
    userCon[7] = new Derived(Emoticon.케장콘, "케장콘3");
    userCon[8] = new Derived(Emoticon.아이카츠콘, "아이카즈콘");
    userCon[9] = new Derived(Emoticon.아이카츠콘, "아이마스콘");
    userCon[10] = new Derived(Emoticon.아이카츠콘, "버튜버콘");
    return userCon;
  }

  enum Emoticon {동물콘, 만두콘, 창고콘, 케장콘, 아이카츠콘}

  final static class Derived {

    private final Emoticon type;
    private final String name;
    // more


    public Derived(Emoticon type, String name) {
      this.type = type;
      this.name = name;
    }

    @Override
    public String toString() {
      return "Derived{" +
          "type=" + type +
          ", name='" + name + '\'' +
          '}';
    }
  }
}
import java.util.Arrays;
import java.util.EnumMap;
import java.util.stream.Collectors;

class Scratch {

  public static void main(String[] args) {
    Derived[] emoticons = dummy();

    var test = new EnumMap<>(Emoticon.class);
    var groups1 =
        Arrays.stream(emoticons).collect(Collectors.groupingBy(e->e.type));
    // 원소중 키가 될 기준을 지정하여 Map<E.Key, List<E>> 의 맵을 만들어주는 groupingBy


    var groups2 =
        Arrays.stream(emoticons).collect(Collectors.groupingBy(e->e.type,
            ()-> new EnumMap<>(Emoticon.class), Collectors.toList()));
    // 원소중 키를 지정하고, 기본제공하는 HashMap이 아닌 다른타입의 Map을 제공,
    // CustomMap<E.Key, List<E>>을 만들어줌
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

  enum Emoticon {동물콘, 만두콘, 창고콘, 케장콘, 아이카츠콘, 둘리콘}

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
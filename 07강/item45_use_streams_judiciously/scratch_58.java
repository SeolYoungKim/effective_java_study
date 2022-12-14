import java.util.List;

class Scratch {

  // 40세 미만의 결혼한 사람중 김씨를 한명 찾는다
  public static void main(String[] args) {
    var peoples = dummy();

    var p1 = peoples.stream()
        .filter(e -> e.age < 40)
        .filter(Person::isMarriages)
        .filter(e -> e.name().startsWith("김"))
        .findAny().get();
    System.out.println(p1);

    Person p2 = null;
    for (var p : peoples) {
      if (p.age < 40) {
        if (p.isMarriages) {
          if (p.name.startsWith("김")) {
            p2 = p;
            break;
          }
        }
      }
    }
    System.out.println(p2);
    // 만약 여기서 조건이 추가된다면?

  }

  private static List<Person> dummy() {
    return List.of(new Person(Person.Gender.FEMALE, 20, false, 0, "김말숙"),
        new Person(Person.Gender.MALE, 30, false, 0, "김모솔"),
        new Person(Person.Gender.MALE, 40, true, 2, "김늙은이"),
        new Person(Person.Gender.FEMALE, 36, true, 2, "이배우자"),
        new Person(Person.Gender.FEMALE, 22, true, 1, "김둥지"),
        new Person(Person.Gender.MALE, 28, true, 0, "김개똥"),
        new Person(Person.Gender.MALE, 35, false, 0, "이피자"),
        new Person(Person.Gender.MALE, 35, false, 0, "박치킨"),
        new Person(Person.Gender.FEMALE, 30, true, 3, "류냉면"),
        new Person(Person.Gender.FEMALE, 42, true, 5, "애국자"),
        new Person(Person.Gender.FEMALE, 19, false, 1, "사고침"),
        new Person(Person.Gender.MALE, 19, false, 1, "얘도임"));
  }

  static class Person {

    private final Gender gender;
    private final int age;
    private final boolean isMarriages;
    private final int child;
    private final String name;

    public Person(Gender gender, int age, boolean isMarriages, int child, String name) {
      this.gender = gender;
      this.age = age;
      this.isMarriages = isMarriages;
      this.child = child;
      this.name = name;
    }

    public Gender gender() {
      return gender;
    }

    public int age() {
      return age;
    }

    public boolean isMarriages() {
      return isMarriages;
    }

    public int child() {
      return child;
    }

    public String name() {
      return name;
    }

    @Override
    public String toString() {
      return "Person{" +
          "gender=" + gender +
          ", age=" + age +
          ", isMarriages=" + isMarriages +
          ", child=" + child +
          ", name='" + name + '\'' +
          '}';
    }

    public enum Gender {
      FEMALE, MALE
    }
  }
}
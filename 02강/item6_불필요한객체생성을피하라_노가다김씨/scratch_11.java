import java.util.regex.Pattern;

class Scratch {

  public static void main(String[] args) {

    if (isRomanNumeral("VII")) {
      System.out.println(true);
    } else {
      System.out.println(false);
    }

    if (RomanNumerals.isRomanNumeral("VII")) {
      System.out.println(true);
    } else {
      System.out.println(false);
    } // 물론 확장성이나 Pattern 의 퍼포먼스를 생각한다면 객체를 만들어 사용해야하는것은 명확하다.
    // 하지만 param.matches() replaceAll() split() 의 간단한 표현에 매력을 느끼지 않을 사람이 얼마나 될까?
  }

  public static boolean isRomanNumeral(String s) {
    return s.matches("^(?=.)M*(C[MD]|D?C{0,3})"
        + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
  }

  public static class RomanNumerals {

    private static final Pattern ROMAN = Pattern.compile(
        "^(?=.)M*(C[MD]|D?C{0,3})"
            + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$"
    );

    public static boolean isRomanNumeral(String s) {
      return ROMAN.matcher(s).matches();
    }
  }

}
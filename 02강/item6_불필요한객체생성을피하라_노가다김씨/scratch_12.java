import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class Scratch {

  public static void main(String[] args) {
    Map<String, Object> map = new HashMap<>(){{
      put("개똥아", null);
      put("똥쌌니", null);
    }};

    Set<String> keys = map.keySet();
    map.put("아니오", null);
    Set<String> keysAfterPut = map.keySet();

    System.out.println(keys == keysAfterPut);  // returns true
    System.out.println(keys.size()); // 3
    System.out.println(keysAfterPut.size()); // 3
  }
}
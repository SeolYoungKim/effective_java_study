import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Scratch {

  public static void main(String[] args) {
    var t = Phase.Transition.from(Phase.SOLID, Phase.GAS);
    System.out.println(t);
  }

  enum Phase {
    SOLID, LIQUID, GAS,
//    PLASMA
// 나중에 새로운 타입이 추가되더라도 Ordinal을 다시계산할 필요도 없고,
    // 연관되는 transition만 추가 하면 된다.
    ;

    enum Transition {
      MELT(SOLID, LIQUID),
      FREEZE(LIQUID, SOLID),
      BOIL(LIQUID, GAS),
      CONDENSE(GAS, LIQUID),
      SUBLIME(SOLID, GAS),
      DEPOSIT(GAS, SOLID),
//      IONIZE(GAS, PLASMA),
//      DEIONIZE(PLASMA, GAS)
      ;

      private final Phase from;
      private final Phase to;

      Transition(Phase from, Phase to) {
        this.from = from;
        this.to = to;
      }

      private static final Map<Phase, Map<Phase, Transition>> transitionMap;

      static {
        transitionMap = Stream.of(values())
            .collect(Collectors.groupingBy(t -> t.from, // 바깥 Map의 Key, Map<"FROM" 
                () -> new EnumMap<>(Phase.class), // 바깥 Map의 구현체 "Map<..>"
                // 보통은 Map<Key, List<Values>> 로 가지만, 리턴될 Map의 목적은 from, to가 주어졌을때의 전이 방법을 리턴하는것이므로
                Collectors.toMap(t -> t.to, // 안쪽 Map 생성부, Key에 해당, Map<from, Map<"TO", transition>>
                    t -> t, // 안쪽 Map의 Value, Map<from, Map<to, "TRANSITION">>
                    (x, y) -> y, // 만약 Key충돌이 발생할시의 대처(이 케이스에는 해당x)
                    () -> new EnumMap<>(Phase.class)))); // 안쪽 Map의 구현체, Map<from, "Map<..>">

        // Map<상태, Map<상태, 전이방법>>
        // Map<from, Map<to, transition>> 의 모양이 됨.
      }

      public static Transition from(Phase from, Phase to) {
        return transitionMap.get(from).get(to);
      }
    }
  }

}
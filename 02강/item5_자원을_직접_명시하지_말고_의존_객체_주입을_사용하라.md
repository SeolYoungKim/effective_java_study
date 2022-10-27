### 정적 유틸리티 클래스로 의존관계를 구현한 경우

```java
public class MacBook {

    /**
     * 이렇게 되면 Chip을 갈아 끼울 때 마다 MacBook 클래스도 고쳐줘야 합니다.
     * 이런 경우, 몇 가지 문제가 발생하는데, 문제점은 아래와 같습니다.
     * - 이는 유연하지 못한 설계입니다.
     * - 이는 MacBook 클래스에 대한 테스트를 어렵게 만듭니다. (Mock 객체를 사용할 수 없습니다. 연산 비용도 고려해야 합니다.)
     */
    public static final M1 m1 = new M1();

    private MacBook() {
    }

    public boolean on(boolean power) {
        return power;
    }

    public void cpuCheck() {
        System.out.println("CPU core : " + m1.cpuCore());
        System.out.println("GPU core : " + m1.gpuCore());
        System.out.println("Memory   : " + m1.memory());
    }
}
```

---

### 의존 객체 주입을 사용하라
```java
public class MacBook {

    /**
     * 아래와 같이 구성할 경우, MacBook은 AppleSiliconChip이 무엇이 온다 해도 변경하지 않아도 되는 코드가 됩니다.
     * - 스프링에서 빈의 의존관계 주입을 설정할 때, 아래와 같은 방식을 많이 사용합니다.
     * - 유연한 코드가 되었습니다! (개방 폐쇄의 원칙)
     */

    private AppleSiliconChip appleSiliconChip;

    private MacBook() {
    }

    public MacBook(AppleSiliconChip appleSiliconChip) {
        this.appleSiliconChip = appleSiliconChip;
    }

    public boolean on(boolean power) {
        return power;
    }

    public void cpuCheck() {
        System.out.println("CPU core : " + appleSiliconChip.cpuCore());
        System.out.println("GPU core : " + appleSiliconChip.gpuCore());
        System.out.println("Memory   : " + appleSiliconChip.memory());
    }
}
```

### 의존 객체 주입 응용: 팩터리 메서드 패턴을 사용할 수 있다.
```java
public class MacBook {

    /**
     * 아래와 같이 구성할 경우, MacBook은 AppleSiliconChip이 무엇이 온다 해도 변경하지 않아도 되는 코드가 됩니다.
     * - 스프링에서 빈의 의존관계 주입을 설정할 때, 아래와 같은 방식을 많이 사용합니다.
     * - 유연한 코드가 되었습니다! (개방 폐쇄의 원칙)
     */

    private AppleSiliconChip appleSiliconChip;

    private MacBook() {
    }

    public MacBook(AppleSiliconChipFactory appleSiliconChipFactory) {
        this.appleSiliconChip = appleSiliconChipFactory.getAppleSiliconChip();
    }

    public MacBook(Supplier<AppleSiliconChip> appleSiliconChipSupplier) {
        this.appleSiliconChip = appleSiliconChipSupplier.get();
    }

    public boolean on(boolean power) {
        return power;
    }

    public void cpuCheck() {
        System.out.println("CPU core : " + appleSiliconChip.cpuCore());
        System.out.println("GPU core : " + appleSiliconChip.gpuCore());
        System.out.println("Memory   : " + appleSiliconChip.memory());
    }
}
```



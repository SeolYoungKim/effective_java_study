
# 인스턴스화를 막으려거든 private 생성자를 사용하라


특정 기능들을 제공하는 클래스들 ex) Math 처럼 공통적으로 사용되는 클래스 또는 상수값을 모아두는 클래스 등등은 객체화를 위해 만든것이 아니므로
상속과 인스턴스화를 막는것이 좋다. 만약 클래스 자체에 생성자가 없는경우 jdk 는 자동으로 NoArgsConstructor를 생성해주므로 임의로 기본 생성자를 private 으로 구성하여 객체화를 막으면 된다.



```java

    public class UtilityClass
    {
        private UtilityClass(){
            throw new Exception();
        }
    }


```
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Scratch {

  static class 시간적_결합제거 {

    public static void main(String[] args) {
      var myWallet = new Wallet();
      myWallet.setPaper_1000(2);
      myWallet.setCoin_500(1);
      System.out.println(myWallet);
      // 이 상태에서 sout이 setter 보다 위로 올라가는 찐빠를 행하더라도 컴파일러는 전혀 문제가 없다고 판단한다.
      // 지금은 너무 단순한 예제지만,
      // 연산이 차례대로 여러번 이루어져야하는 로직이 된다면 그것을 외우거나, 어딘가에는 적어놔야 할것이다.
      // 정상적인 수행은 돈을 두 차례에 걸쳐 넣고, 이후 sout을 시켜줘야한다 라는 순서
      // 비정상적인 수행은 set 이전에 sout을 시켜줬을 때.
      // 가변 객체가 해야하는 연산의 순서를 일일히 기억하는 것은 당신의 몫이 된다는 뜻.

      var moneyBag = new MoneyBag(2, 1);
      System.out.println(moneyBag);
      // 불변객체의 경우 인스턴스 생성과 값의 assign을 분리할수 없기때문에 위의 예제같은 찐빠가 일어날수가 없다.
      // 객체 초기화와 사용이 완전히 분리될수 있으므로 시간적인 결합에 자유로울수 있다.
      // 이는 뒤에 설명될 완전한 객체 생성에과도 연관이 있다.
    }
  }

  static class 부수효과_제거 {

    public static void main(String[] args) {
      var myWallet = new Wallet();
      myWallet.setPaper_1000(2);
      myWallet.setCoin_500(1);
      // setup
      printWallet(myWallet);
//      ..대충 많은 추가 연산들
      System.out.println(myWallet);
      // 여기까지 왔을때 클라이언트는 뭔가 잘못되었다는것을 알게되고 디버깅을 시도할것이다.
      // 그리고 추가적인 연산을 다 뒤져본 이후에야 printWallet 에 잘못된 연산이 있다는것을 깨닫고 수정을 할것이다.
      // 반면 불변 객체는 어디서든 불변이므로 위의 케이스가 발생하지 않을것임을 확신할수 있다.
      // 또한 누군가는 '불변 객체는 기본적으로 readonly 이므로 해당 케이스와는 아예 어울리지 않는거 아님?' 이라고 이야기 할수 있다.
      // 이것도 맞는 말이며, 판단은 스스로..
      System.out.println();
      var moneyBag = new MoneyBag(2, 1);
      printMoneyBag(moneyBag);
      System.out.println(moneyBag);
      // printMoneyBag 에서 연산을 수행했지만 assign을 따로 하지 않는다면 moneyBag이 가르키는 객체는 불변하다.
      // 아예 assign 조차도 막아버릴수 있다. 아래 주석을 풀어보자.
//      final var sealedMoneyBag = new MoneyBag(2, 1);
//      sealedMoneyBag = sealedMoneyBag.payForPaper(1);
      // 하지만 가변객체는 final로 막아도 소용이 없다. 새로운 객체 assign이 아니라 내부 상태가 바뀌는 것이므로.
    }

    static void printWallet(Wallet w) {
      System.out.println("in printWallet :: " + w);
      w.payForPaper(1);// 대충 클라이언트가 엉뚱한 연산을 하게 됐다는 가정
    }

    static void printMoneyBag(MoneyBag b) {
      System.out.println("in printMoneyBag :: " + b);
      b.payForPaper(1); // 마찬가지로 클라이언트가 엉뚱한 연산을 하게 됐다는 가정
    }
  }

  static class 완전한_객체_생성 {

    public static void main(String[] args) {
      var myWallet = new Wallet();
      myWallet.setCoin_500(1);
//      myWallet.payForPaper(1);
      // 완전하지 않은 객체를 생성한 상태에서 연산을 수행하려고 하여 nullpointerexception이 발생함.
      // 이런 연산을 수행할때마다 멤버 객체에 대한 null check를 필수로 해줘야 하기 때문에 매우 비효율적이게 됨.

      var moneyBag = new MoneyBag(0, 1);
      moneyBag = moneyBag.payForPaper(1);
      System.out.println(moneyBag);
      // 객체를 의도적으로 null로 세팅하지 않는한 -값이 나올수는 있지만 연산 자체는 정상적으로 수행됨.
      // 누군가는 이것이 오히려 버그를 유발하지 않느냐고 되물을수 있지만, -값이 나오지 않게끔 하는것은 payForPaper 가 가진 책임이다.
      // 다시말해, 완전한 객체를 만들 준비가 되기 전까지는 객체를 생성할수가 없고, 사용도 불가능하므로
      // 실수로 불완전한 객체를 생성, 사용할 상황자체가 만들어지지 않는다.
      // 물론 moneybag에 setter를 추가한다면 불완전한 객체가 만들어질순 있지만, 이는 불변함을 깨게 되므로 불변 객체가 아니게 된다.

      // 그렇다면 결과값을 만들기 위해서 중간연산이 필요한데 가변 객체가 아니면 어떻게 해요? 라는 물음이 생길수 있다.
      // 물음 하기 위해 든 손으로 자신의 머리를 때리고, 게으른 자신을 반성하라. 다음으로.
    }
  }

  static class 중간_연산이_필요할때_불변객체 {

    public static void main(String[] args) {
      var username = "김개똥이";
      var posts = 유저와_유저의_포스팅_가져오기(username);
      System.out.println(posts);

      var comment = 유저와_유저의_코멘트_가져오기(username);
      System.out.println(comment);

    }

    private static UserPostPresenter 유저와_유저의_포스팅_가져오기(String username) {
      // 주어진 username으로 유저를 찾고, 그 유저와 유저가 작성한 post id를 리턴하는 기능을 만든다 가정합시다.
      // 가변객체로 만든 리턴 객체를 사용하는 방법입니다.
      var user = findByUsername(username);
      var posts = new UserPostPresenter();
      posts.setId(user.id());
      posts.setName(user.name());
      posts.setCreateAt(user.createAt());
      // 혹은.. 귀찮으니까 이렇게들 하시겠죠
//      posts = UserPostPresenter.fromEntity(user);
      posts.setPost(findPostByUserId(user.id()));
      return posts;
    }// 이 방법은 리턴된 객체가 클라이언트에 의해 다시 set 될수도 있습니다.

    private static UserCommentPresenter 유저와_유저의_코멘트_가져오기(String username) {
      // 주어진 id로 유저를 찾고, 그 유저와 유저가 작성한 comment id를 리턴하는 기능을 만든다 가정합시다.
      // 불변객체로 만든 리턴 객체를 사용하는 방법입니다.

      var user = findByUsername(username);
//      var response = new UserCommentPresenter(user.id(), user.name(), !!!! ,user.createAt());
      // 이 시점에 곧바로 리턴 객체를 만들 필요가 없습니다.
      var comments = findCommentByUserId(user.id());

      var response = new UserCommentPresenter(user.id(), user.name(), comments, user.createAt());
      return response;
    } // 또는..

    private static UserCommentPresenter 유저와_유저의_코멘트_가져오기_컴패니언(String username) {
      // 리턴 객체를 만들기 전에 컴패니언 객체를 이용해 불변 객체를 만들고 리턴하는 방법입니다.

      var user = findByUsername(username);
      var builder = new UserCommentPresenter.Builder(user.id(), user.name(), user.createAt());
      // 여기서 가변객체를 이용한 연산을 한다고 가정합시다.
      var comments = findCommentByUserId(user.id());
      builder.comment(comments);

      var response = builder.build();
      // 컴패니언(빌더)를 이용함으로 UserCommentPresenter는 불변성을 유지했습니다.
      // 그렇다면 중간에 가변 객체를 사용했으므로 setter를 이용한 방법과 차이가 없지 않느냐? 라고 이야기 하실수 있는데,
      // 컴패니언 객체의 목적은 불변 객체를 만들기 위해 도와주는 역할입니다. 컴패니언 객체를 그대로 사용하는것이 아니에요.
      return response;
    }

    //region generate mock method
    private static User findByUsername(String username) {
      //.. 대충 레파지토리에서 유저를 가져온다는 내용.. 의 mock
      return new User("userId", username,
          LocalDateTime.of(2022, 01, 01, 0, 0)
              .atZone(ZoneOffset.systemDefault()).toEpochSecond());
    }

    private static List<String> findPostByUserId(String userId) {
      // 대충 레파지토리나 서비스에서 포스팅id 를 가져온다는 내용
      return List.of("postId123", "postId321");
    }

    private static List<String> findCommentByUserId(String userId) {
      // 대충 레파지토리나 서비스에서 코멘트id 를 가져온다는 내용
      return List.of("commentId123", "commentId321");
    }

    //endregion
  }

  static class Wallet {

    private Integer paper_1000; // int 로 적당하지만, 완전하지 않은 객체의 위험성을 알리기에는 레퍼런스 타입이 적절하다 생각되어서.
    private Integer coin_500;
    //... more?

    public void setPaper_1000(int paper_1000) {
      this.paper_1000 = paper_1000;
    }

    public void setCoin_500(int coin_500) {
      this.coin_500 = coin_500;
    }

    public void payForPaper(int paper_1000) {
      this.paper_1000 -= paper_1000; // 예제니까 -값 처리라던지 이런부분은 넘어갑시다.
    }

    @Override
    public String toString() {
      return "Wallet{" +
          "paper_1000=" + paper_1000 +
          ", coin_500=" + coin_500 +
          '}';
    }
  }

  static class MoneyBag { // 이름이 이따위라 죄송합니다.

    private final Integer paper_1000;
    private final Integer coin_500;

    public MoneyBag(int paper_1000, int coin_500) {
      this.paper_1000 = paper_1000;
      this.coin_500 = coin_500;
    }

    public MoneyBag payForPaper(int paper_1000) {
      return new MoneyBag(this.paper_1000 - paper_1000, coin_500);
    }

    @Override
    public String toString() {
      return "MoneyBag{" +
          "paper_1000=" + paper_1000 +
          ", coin_500=" + coin_500 +
          '}';
    }
  }

  static class User {

    private final String id;
    private final String name;
    private final long createAt;

    public User(String id, String name, long createAt) {
      this.id = id;
      this.name = name;
      this.createAt = createAt;
    }

    public String id() {
      return id;
    }

    public String name() {
      return name;
    }

    public long createAt() {
      return createAt;
    }
  }

  static class UserPostPresenter {

    private String id;
    private String name;
    private List<String> post; // 편의상 comment의 id값만 리턴한다고 가정합시다
    private long createAt;

    public void setId(String id) {
      this.id = id;
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setPost(List<String> post) {
      this.post = post;
    }

    public void setCreateAt(long createAt) {
      this.createAt = createAt;
    }

    public UserPostPresenter() {
    }

    public UserPostPresenter(String id, String name, long createAt) {
      this.id = id;
      this.name = name;
      this.createAt = createAt;
    } // post는 주입될 타이밍이 user 객체 로딩 시점과 다르므로 생성자주입이 아니라 setter를 사용하시겠죠.

    public static UserPostPresenter fromEntity(User u) {
      return new UserPostPresenter(u.id(), u.name(), u.createAt);
    }

    @Override
    public String toString() {
      return "UserPostPresenter{" +
          "id='" + id + '\'' +
          ", name='" + name + '\'' +
          ", post=" + post +
          ", createAt=" + createAt +
          '}';
    }
  }

  static class UserCommentPresenter {

    private final String id;
    private final String name;
    private final List<String> comment; // 편의상 comment의 id값만 리턴한다고 가정합시다.
    private final long createAt;

    public UserCommentPresenter(String id, String name, List<String> comment, long createAt) {
      this.id = id;
      this.name = name;
      this.comment = Collections.unmodifiableList(comment);
      this.createAt = createAt;
    }

    @Override
    public String toString() {
      return "UserCommentPresenter{" +
          "id='" + id + '\'' +
          ", name='" + name + '\'' +
          ", comment=" + comment +
          ", createAt=" + createAt +
          '}';
    }

    // 컴패니언 객체를 이용할때, 이땐 불변객체의 생성자를 private으로 설정하자.
    public static class Builder {
      private final String id;
      private final String name;
      private final long createAt;
      private List<String> comment = Collections.emptyList(); // 필수가 아닌 값은 기본값이 필요하다.

      public Builder(String id, String name, long createAt) {
        this.id = id;
        this.name = name;
        this.createAt = createAt;
      }
      public Builder comment(List<String> comments){
        this.comment = Collections.unmodifiableList(comments);
        return this;
      }
      public UserCommentPresenter build(){
        return new UserCommentPresenter(this.id, this.name, this.comment, this.createAt);
      }
    }
  }
}
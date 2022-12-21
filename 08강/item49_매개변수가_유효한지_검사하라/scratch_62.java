import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.stream.Collectors;

class Scratch {

  public static void main(String[] args) throws IOException {
    var content = new YourHtmlContent("https://naver.com");
    System.out.println(content.get());

    // 위의 케이스는 객체를 생성할때,
    // 아래의 케이스는 객체가 사용될때 실패한다. (잘못된 url일때)

    var myContent = new MyHtmlContent("https://naver.com");
    System.out.println(myContent.get());

    // 간단한 로직에서는 별 차이가 없다고 느낄수 있으나,
    // 여러 레이어를 사용하고, 객체의 실제 사용이 로직의 먼 나중에 사용된다고 생각해보면
    // 실패했을 당시를 디버그 하고 원인을 따라가는 것보다 객체가 만들어질때 질때 실패하고 원인을 찾는것이 빠르다는 것은 당연하다.



    // 이하 실패 원자성에 대한 가정

    try {
      var f1 = new YourHtmlContent("https://naver.coms");
      f1.get(); // 잘못된 url을 가져오려고 할수 없기 때문에 실패 원자성이 지켜짐 (= 아예 객체를 만들수 없었음 = 객체의 불변함!)
      System.out.println(f1.cached == (f1.content != "")); //get() 이 성공했다면 cached가 true임을 확인하는 코드
    } catch(Exception ignored){}

    var f2 = new YourHtmlContent("https://naver.coms");
    try {
      f2.get(); // 잘못된 url에서 컨텐츠를 가져오려다 실패했기 때문에 실패 원자성을 지킬수가 없었음
    } catch(Exception ignored){}
    System.out.println(f2.cached == (f2.content != "")); //실패원자성이 지켜지지 않아 get()이 실패해도 cached는 true일수 있다
    // 물론 이 경우에도 get()메소드의 cached = true를 마지막으로 옮기면 실패원자성을 지킬수 있지 않느냐 라고 반문 할수 있으나,
    // 실패 원자성이 무엇인지 알아보기 위한 예제이므로..
  }

  private static class YourHtmlContent {
    private final URL url;
    private boolean cached = false;
    private String content = "";

    public YourHtmlContent(String url) throws MalformedURLException {
      this.url = new URL(url);
      // String을 URL클래스로 변한하면서 내부적으로 validate을 수행하는 케이스
    }
    public String get() throws IOException {
      if(cached){
        return content;
      }
      cached = true;
      // 만약 올바르지 않은 url String 이고, 검사가 수행되지 않았다면 객체를 만들때가 아니라 실제 사용될때 에러가 발생할것이다.
      BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()));
      return content = r.lines().collect(Collectors.joining());
    }
  }
  private static class MyHtmlContent{
    private final String url;
    private boolean cached = false;
    private String content = "";

    public MyHtmlContent(String url) {
      Objects.requireNonNull(url, "url must be not null");
      this.url = url;
    } // 클래스의 목적은 Url에서 콘텐츠를 가져오는 것인데, 단순히 url이 null인지만 검사하는 것은 클래스의 목적에 부합하지 않는다.
    public String get() throws IOException {
      if(cached){
        return content;
      }
      cached = true;
      BufferedReader r = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
      return content = r.lines().collect(Collectors.joining());
    } // pure java 로 url을 가져올 방법이 없어 이런식으로 작성했는데,
    // 나중에 사용해야하는 파라미터도 검사 해야한다는 맥락은 이런 방법이 아니라 어떤 라이브러리를 사용해도 마찬가지이다.
  }
}
# 상속을 고려해 설계하고 문서화하라. 그러지 않았다면 상속을 금지하라

### 아이템 18에서 자식 객체가 같은 패키지 않의 클래스가 아닌 부모 객체를 상속받을경우 위험성에 대해서는 충분히 이야기를 나눴을 것이다

### 이때 부모객체의 내부참조에 따라 직접 상속을 받는경우 자식객체의 캡슐화가 깨질 수 있기에 만약 부득이하게 상속이 가능한 클래스를 만들경우

### 해당 메서드의 내부참조에 대해 상세한 설명을 주석으로 남겨야 하며 이떄 @implSpec 을 사용 하면 자바독 도구가 Implementation Requirements 를 생성해주기에 좀 더 수월하게 해당 메서드의 오버라이드시 조심해야하는점을 공유할 수 있다.

### 또한 이 외에도 내부 동작중 끼어들 수 있는 훅을 잘 선별하여 protected 메서드 형태로 공개해야 한다 (물론 이 역시 주석을 남겨야 한다.)

<br>

### 이처럼 상속용 클래스는 고려할 점이 많으며 만약 본인의 클래스가 상속을 고려히지 않는다면 생성자에 private 을 써서 상속을 막거나

### 클래스에 final 을 추가하여 상속을 막는것이 바람직 하다.

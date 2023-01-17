package com.example.scratcher.exp;

public class AppMain2 {

  public static void main(String[] args) {
    // 뭔가 오래걸릴수 있는 중요한 작업을 합니다
    // all or nothing이 적용되어야 합니다

    var prod = ProductInfo.fake();
    var customer = UserInfo.fake();

    try {
      purchase(customer.cardInfo(), prod);
    } catch (Exception e) {
      e.printStackTrace(); // 실패한 이유를 사용자에게 리턴하고 종료
      return;
    }
    sendProduct(customer);

    // 체크 예외 대신 거의 같은 행위를 하는 validate 기능을 추가한다
    try {
      purchaseWithValidate(customer.cardInfo(), prod);
    } catch (Exception e) {
      e.printStackTrace(); // 실패한 이유를 사용자에게 리턴하고 종료
      return;
    }
    sendProduct(customer);

  }

  //region validate
  static void purchaseWithValidate(CardInfo card, ProductInfo prod) throws Exception {
    // 카드 결제 이후 결제가 승인이 되면 재고를 줄임
    if(validate(card) && validate(prod)){
      // 이 블록에선 카드 결제가 가능하고, 상품의 판매가 가능한 상태를 가짐
      // 하지만 책에서 이야기한대로 멀티스레드 환경에선 두 상태 다 달라져 있을수도
      charge2(card, prod);
      decreaseStock2(prod);
    } else {
      throw new Exception(); // 이는 클라이언트 코드가 해야할 일이 있기 때문에 적합한 체크 예외임
    }

  }

  static boolean validate(CardInfo card){
    // 카드가 적법한지 확인함
    return true;
  }

  static boolean validate(ProductInfo prod){
    // 상품이 재고가 있고, 판매 가능상태인지 확인함
    return true;
  }

  static void charge2(CardInfo cardInfo, ProductInfo productInfo){

  }
  static void decreaseStock2(ProductInfo productInfo){}

  //endregion

  // region no validate
  static void purchase(CardInfo cardInfo, ProductInfo productInfo) throws Exception {
    // 카드 결제 이후 결제가 승인이 되면 재고를 줄임
    try {
      charge(cardInfo, productInfo);
    } catch (Exception e) {
      throw e; // 카드의 사용 문제로 구매를 못했으므로 사용자에게 에러 메시지를 보여줘야합니다.
    }

    try {
      decreaseStock(productInfo);
    } catch (Exception e) {
      // 위에서 이미 카드 결제가 되어버렸으므로 다시 카드 결제 취소를 해야합니다.
      try {
        discharge(cardInfo);
      } catch (Exception se) {
        // 결제 정보를 찾을수 없다면 예외가 발생하지만, 실제 예외 처리는 할수 있는것이 없음.
      }
      throw e; // 카드결제 취소가 완료 되었고, 재고문제로 구매가 실패했다는 에러 메시지를 보여줘야함.
    }
  }

  static void decreaseStock(ProductInfo prod) throws Exception {
    // 판매된 상품의 재고를 줄입니다.
    // 재고가 없다면 예외를 던집니다.
  }

  static void charge(CardInfo card, ProductInfo prod) throws Exception {
    // 입력된 카드 정보로 과금합니다.
    // 입력된 카도 정보가 옳지 않거나 과금이 불가능할 경우 예외를 던집니다.
  }

  static void discharge(CardInfo card) throws Exception {
    // 실제로는 카드 결제가 이런방식으로 이루어지지도 않고, 결제관련 데이터도 필요하지만 편의상 이리 씁니다.
    // 전달된 결제 관련 데이터를 찾을수 없다면 예외를 던집니다.
  }

  static void sendProduct(UserInfo userInfo) {
    // 상품을 구매자에게 보냅니다.
  }
  //endregion

  //region models
  record ProductInfo(
      String name,
      String prodCd,
      long priceWon
  ) {

    static ProductInfo fake() {
      return new ProductInfo("김치의 신 갓김치", "kimch000", 30_000);
    }
  }

  record UserInfo(
      String name,
      Address address,
      CardInfo cardInfo
  ) {

    static UserInfo fake() {
      return new UserInfo("joshua bloch", new Address("joshua bloch",
          "california, United States", "somewhere, san jose", "12345"),
          new CardInfo("이동식", "portable", "1234-1234-1234-1234", "676", "master", "0012"));
    }
  }

  record CardInfo(
      String nameKr,
      String nameEn,
      String numbers,
      String cvc,
      String company,
      String password
  ) {

  }

  record Address(
      String name,
      String address1,
      String address2,
      String zipCode
  ) {

  }

  //endregion
}

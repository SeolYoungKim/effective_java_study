package com.example.scratcher.exp;

import com.example.scratcher.exp.sender.LogMessageSender;

public class AppMain {

  public static void main(String[] args) {
    // 뭔가 중요한 작업을 합니다
    // 해당 세션은 트랜잭션으로 엮여 있다 가정합니다
    // 로그 메시지는 메인 작업에 비해 중요도가 떨어집니다
    // 로그 메시지를 남기는 도중 실패해도 해당 세션은 정상 처리 되어야 합니다
    doSomeImportant();
    doLog(); // 이 경우 jpa @Transactional 안에서 수행될 경우 runtimeException 이 마킹 되므로 롤백 대상이 됨
    doLog2();
    System.out.println("트랜잭션 완료");

  }

  static void doSomeImportant() {
  }

  static void doLog() { // 체크 예외를 언체크 예외로 번역해 처리
    var msgServ = createLogMsgServ();
    int t = 0;
    while(t <= 3){ // 최대 재시도 3번
      try {
        msgServ.sendLogMessage(LogMessage.fake());
        break;
      } catch (SendLogMessageException e){
        t++;
      }
    }
  }

  static void doLog2(){ // 체크예외를 boolean으로 처리
    var msgServ = createOptLogMsgServ();
    int t = 0;
    while(t<=3){
      var ret = msgServ.sendLogMessage(LogMessage.fake());
      if(ret.get()){
        break;
      }
      t++;
    }

  }

  static MessageService createLogMsgServ() {
    return new MessageService(new LogMessageSender());
  }
  static OptionalMessageService createOptLogMsgServ(){
    return new OptionalMessageService(new LogMessageSender());
  }
}

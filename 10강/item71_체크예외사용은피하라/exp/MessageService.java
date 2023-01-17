package com.example.scratcher.exp;

import com.example.scratcher.exp.sender.Message;
import com.example.scratcher.exp.sender.MessageSender;
import java.io.IOException;

public class MessageService {
  private final MessageSender logSender;

  public MessageService(MessageSender msgSender) {
    this.logSender = msgSender;
  }

  public void sendLogMessage(Message msg){
    try {
      logSender.sendMessage(msg);
    } catch (IOException e) { // 외부 시스템 에러는 발생해도 재처리 말고는 할수 있는 것이 없는 경우가 대부분
      e.printStackTrace();
      throw new SendLogMessageException(); // 예외 번역 (checked -> unchecked)
    }
  }
}

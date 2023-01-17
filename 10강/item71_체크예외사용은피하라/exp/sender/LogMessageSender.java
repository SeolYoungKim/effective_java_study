package com.example.scratcher.exp.sender;

import java.io.IOException;
import java.util.Random;

public class LogMessageSender implements MessageSender {

  private final Random r;
  public LogMessageSender() {
    this.r = new Random();
  }

  // 외부 시스템에 로그 메시지를 쏘는 샌더
  @Override
  public void sendMessage(Message msg) throws IOException {

    if(r.nextInt()%2 == 0){
      throw new IOException("cause external system exception!");
    }
    System.out.println(msg.body() + " send");
  }

}

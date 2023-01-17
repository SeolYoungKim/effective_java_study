package com.example.scratcher.exp;

import com.example.scratcher.exp.sender.Message;
import com.example.scratcher.exp.sender.MessageSender;
import java.io.IOException;
import java.util.Optional;

public class OptionalMessageService {
  private final MessageSender logSender;

  public OptionalMessageService(MessageSender msgSender) {
    this.logSender = msgSender;
  }

  public Optional<Boolean> sendLogMessage(Message msg){
    try {
      logSender.sendMessage(msg);
      return Optional.of(true);
    } catch (IOException e) {
      return Optional.of(false);
    }
  }
}

package com.example.scratcher.exp.sender;

import java.io.IOException;

public interface MessageSender {
  void sendMessage(Message msg) throws IOException;

}

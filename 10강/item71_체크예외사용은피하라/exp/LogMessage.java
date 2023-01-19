package com.example.scratcher.exp;

import com.example.scratcher.exp.sender.Message;
import java.time.Instant;

public record LogMessage(int no, String body, long createdAt) implements Message {

  public static LogMessage fake(){
    return new LogMessage(1, "중요한 작업 완료하였음", Instant.now().getEpochSecond());
  }

  @Override
  public String toString() {
    return "LogMessage{" +
        "no=" + no +
        ", body='" + body + '\'' +
        ", createdAt=" + createdAt +
        '}';
  }
}

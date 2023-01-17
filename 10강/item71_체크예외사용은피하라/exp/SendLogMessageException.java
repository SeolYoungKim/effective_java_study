package com.example.scratcher.exp;

public class SendLogMessageException extends RuntimeException {
  private final static String ERR_MSG = "raise exception while message sending";

  public SendLogMessageException() {
    super(ERR_MSG);
  }
}

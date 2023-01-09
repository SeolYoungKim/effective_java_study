package com.example.scratcher.refl;

import java.io.IOException;

public interface CoreInterface {

  void doSomeThing(int a);

  default void doSomeThing2() throws IOException {
    throw new IOException(new UnsupportedOperationException("not implemented yet."));
  } // 체크 예외를 무시할 수 있음을 보여주기 위한 메서드

}

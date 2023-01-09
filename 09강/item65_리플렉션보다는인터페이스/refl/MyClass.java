package com.example.scratcher.refl;

import java.io.IOException;
import java.util.Random;

public class MyClass implements CoreInterface {

  private int b;

  public MyClass() {
    b = 10;
  }

  @Override
  public void doSomeThing(int a) {
    System.out.println("input param : " + a);
    System.out.println("print : " + a * b);
  }

  @Override
  public void doSomeThing2() throws IOException {
    System.out.println("base value : " + b);
    Random r = new Random();
    if(r.nextInt() % 2 == 0) throw new IOException();
  } // 1/2 확률로 ioe를 발생시키는 메소드. 시그니쳐에 박힌 체크예외를 우회할수있음을 보여주기 위한 메소드
}

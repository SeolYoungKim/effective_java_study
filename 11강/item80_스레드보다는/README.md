## Item 80 : thread 보다는 executor, task, stream
### Conclusion
스레드를 직접 이용하기 보다는 executor framework 를 이용하라.  
작업 큐나 스레드를 직접 만들어 다루지 마라.  
executor 를 사용하면 작업 단위(작업 수행 정책변경 가능), 실행 메커니즘을 분리할수 있다.


### In case
task => 작업을 표현하는 단위라고 생각하자.  
executor(실행자) => jdk 1.5 에 추가된 executor framework 혹은 그 스레드 풀  
ExecutorService 실행자로 제공되는 스레드 풀  

스레드를 다루기 위한 추상화 기술이며, 여러 스레드 관련 메서드를 제공한다.  
인터페이스이며, Executors 라는 static 생성자 혹은 ThreadPoolExecutor를 직접 생성하여 사용해야한다.  
책에서도 이야기 하듯 Executors 를 이용한 스레드 풀 생성시 서버 자원이 부족하게 될 상황이 생길수 있으므로   
무조건 적인 newSingleThreadPool() 이나 newCachedThreadPool() 사용은 지양하고,   
머신 스펙에 적절한 스레드 갯수를 미리 지정해두고 사용하도록 하자.


ForkJoinPool에 대해서도 설명하는데, 일반적으로 작업이 끝나면 반환되거나 결과값을 리턴하기 위해 대기하는 스레드와 달리   
ForkJoinPool에서 생성한 스레드들은 서브 태스크로 나뉠수 있는 ForkJoinTask를 지원하기 위해 만들어졌다.  
상세하게는 WorkStealingPool 이라고도 하는데, ForkJoinTask를 서브 태스크로 나누고 결과를 병합하기 위해 사용한다.  
좀 더 상세하게 이야기하자면 서브 태스크를 끝낸 스레드는 아직 남아있는 태스크를 가져와 처리 하기 때문에 스레드의 유휴가 줄어든다.(물론 IO가 없는 순수 컴퓨팅작업일때 이야기이다)  


## in my opinion
스레드의 적정 개수에 대해서는 cpu core 갯수 * 2 정도가 적당하다고 한다.  
하지만 내가 할 작업에서도 이 말이 맞는지는 직접 테스트해보지 않고서는 알수 없기 때문에 적정한 스레드 갯수 찾기는 어렵다.  
무작정 스레드 갯수를 늘린다고 하더라도 물리적으로 작업이 이루어지는 스레드는 결국 cpu core갯수만큼이기 때문에 오히려 스레드간 context switching 비용이 발생할 뿐이다.  





https://multifrontgarden.tistory.com/276  -- 스레드 풀 생성시의 함정

https://velog.io/@yohanblessyou/%EC%8A%A4%EB%A0%88%EB%93%9C-%ED%92%80Thread-pool%EC%9D%98-%EC%A0%81%EC%A0%95-%ED%81%AC%EA%B8%B0-feat.-%EB%A6%AC%ED%8B%80%EC%9D%98-%EB%B2%95%EC%B9%99  -- 적절한 스레드 갯수 

https://mangkyu.tistory.com/263  -- completableFuture 

https://hamait.tistory.com/612  -- fork join은 뭐가 다른가 

https://keichee.tistory.com/455  -- spring ThreadPoolTaskExecutor

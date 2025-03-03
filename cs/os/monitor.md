# Monitoring

상호배제 기능을 제공하나, 세마포어보다 고수준 동기화 도구
-> 동기화 복잡함 단순화하고, 개발자의 실수를 줄이도록 도움

한번에 하나의 스레드만 실행해야 할 때
여러 스레드와 협업이 필요할 때

## 구성 요소

- mutex : critical section에서 mutual exclusion을 보장하는 장치 ( 진입하려면 mutex lock을 취득해야 함 )
-> 취득하지 못한 스레드는 큐에 들어간 후 대기 상태로 전환
-> mutex lock 쥔 스레드가 lock 반환하면 락 기다리며 큐에 대기 상태로 있던 스레드 중 하나가 실행

- condition variable : waiting queue를 가짐, 조건 충족되길 기다리는 스레드 대기 상태로 머무는 곳

    - wait : thread가 자기 자신을 condition variable의 waiting queue에 넣고 대기 상태로 전환
    - signal : waiting queue에서 대기중인 스레드 중 하나를 깨움
    - broadcast : waiting queue에서 대기중인 스레드 중 전부를 깨움

- entry queue : critical section에 진입을 기다리는 큐
- waiting queue : 조건이 충족되길 기다리는 큐

---

## 동작 방식

- Signal-And-Continue : C1 을 깨우고, P1이 자신의 할 일을 계속함
- Signal-And-Wait : C1 을 깨우고, P1은 대기 상태 -> C1이 다 한후 release -> P1 은 다시 작동

```
// Producer
while(q.isFull()){
    wait(lock,fullCV);
}

// Consumer

while(q.isEmpty()){
    wait(lock,emptyCV);
}
```

```
public void producer(){
    while(true) {
        task myTask = ...;
       
        lock.acquire();
        
        while(q.isFull()) {
            wait(lock,fullCV);
        }
    
        q.enqueue(myTask);
        
        signal(emptyCV); or broadcast(emptyCV);
        lock.release();
    }
}        
```

이와같이 `wait` 문은 while 문 안에 있어야만 한다. - Spurious Wakeup
( 조건이 만족되어 깨더라도, 다른 스레드가 그 사이 수행해서 다시 조건이 만족 못할 수 있기 때문 )

## 모니터 in Java

직접 구현할 일은 잘 없고, 사용만 하면 된다.

자바는 모든 객체가 내부적으로 모니터를 가진다. ( Intrinsic Lock - 고유락 )
-> 모니터의 mutual exclusion 기능을 `synchornized` 키워드로 사용한다.
-> condition variable를 하나만 가진다.
( 두 가지 이상 condition variable 이 필요하다면 따로 구현이 필요하다. )

```java
public synchronized void produce(int item){
    while(count == 5) { wait(); }
    buffer[count++] = item;
    notifyAll();
}

public void consume() {
    int item = 0;
    synchronized (this) {
        while (count==0) { wait(); }
        item = buffer[--count];
        notifyAll();
    }
    System.out.println("");
}
```
하나의 condition variable 이므로 notifyAll 로 전부 깨움

## 자바 고유 락

- 재진입성 : 고유 락은 스레드 단위로 획득 가능, 이미 Lock 가진 스레드는 대기할 필요 X
- 가시성 : `happens-befor` 관계, 

### synchronized 의 문제점

명시적으로 락을 해제할 수 없다.
-> 락 획득과 해제 시점을 프로그래머가 세밀하게 제어하기 어렵다.

락 획득 시 타임아웃이나 인터럽트 처리 지원 X
-> `ReentrantLock` 과 같은 클래스에서 제공

성능 문제가 발생한다. - 락 경합
-> 나머지 스레드들은 전부 BLOCKED 상태로 대기한다.
-> 락을 반환하면, JVM 이 BLOCKED 상태 스레드 중 임의로 선택하게 한다.
-> notify 호출시에는 WAITING 상태 스레드 하나를 임의로 깨운다.
-> notify, wait, notifyAll 은 반드시 synchronized 키워드 내에서 호출해야 한다.

> 임계 영역이 매우 짧은 경우, synchronized 사용해 락 획득, 해제 비용이 더 커질 수 있다.
> 결국, lock 취득/해제 과정에서 Context Switching 이 발생한다.
> 진입이 가능하고 취득하지 못할 경우 대기 상태로 entryqueue 에서 머물게 된다. - 다시 객체 lock 획득하고 진입에 context switching

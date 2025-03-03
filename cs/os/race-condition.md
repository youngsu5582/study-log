

## Race Condition

여러 프로세스/스레드가 동시에 같은 데이터를 조작할 때 타이밍이나 접근 순서에 따라 결과가 달라질 수 있는 상황

## 동기화

여러 프로세스/스레드를 동시에 실행해도 공유 데이터의 일관성을 유지하는 것

## Critical Section

공유 데이터의 일관성을 보장하기 위해 `하나의 프로세스/스레드만 진입`(mutual exclusion)해서 실행 가능한 영역

-> 락을 사용해 mutual exclusion 을 보장한다.

```c++
volatile int lock = 0;

void critical() {
    while (test_and_set(&lock) == 1);
    ... critical section
    lock = 0;
}

int TestAndSet(int* lockPtr) {
    int oldLock = *lockPtr;
    *lockPtr = 1;
    return oldLock;
}
```
공유되는 락에 대해서 원래 가지고 있는 값을 가져와서 반환 ( 단, 1로 바꿔서 )
-> 그전값이 1이였다면, 1을반환 - 0이였다면, 0을반환

제일 처음 들어오는 스레드는 `0 != 1` 이므로 탈출한다.
그 후, 들어오는 스레드는 `1 == 1` 이므로 탈출하지 못한다.

=> 어떻게 동시에 들어와서 동시에 동작시키지 않는걸 보장하는가??
=> CPU 의 도움을 받는다.

### CPU Atomic 명령어

실행 중간에 간섭받거나 중단되지 않는다.
같은 메모리 영역에 대해 동시에 실행되지 않는다.

-> CPU가 동기화를 적절히 시켜서 실행

## 스핀락

=> 이런 방식을 스핀락이라고 한다. ( 락을 가질 수 있을때까지 반복해서 시도 )
단점으로 기다리는 동안 CPU 를 낭비한다.

## Mutex

```c
class Mutex {
    int value = 1;
    int guard = 0;
}
```

```c
Mutex::lock() {
    while (test_and_set(&guard));
    if(value == 0) {
        ... 현재 스레드를 큐에 넣음
        guard = 0;
    } else {
        value = 0;
        guard = 0;
    }
}

Mutex::unlock() {
    while(test_and_set(&guard));
    if(큐에 하나라도 대기중이라면) {
        그 중에 하나를 깨운다;
    else {
        value = 1;
    }
    guard = 0;
}
```

```
mutex->lock();
... critical section
mutex->unlock();
```

value 값을 통해 단 하나의 프로세스/스레드만 동작하게 보장
guard 값을 통해 value 값 변경이 critical section 에서 동작하게 보장

- lock을 획득하지 못하면 큐에 들어가서 대기한다. -> CPU 사이클 낭비 X
- CPU 지원 Atomic 연산을 사용한다.

=> 락을 가질 수 있을 때가지 휴식한다.

멀티 코어 환경이고, critical section 사용 시간보다 컨텍스트 스위칭이 더 빨리 끝난다면 스핀락이 뮤텍스보다 더 이점이 있다.

잠들고 깨는 과정에 컨텍스트 스위칭 발생
멀티코어여야 하는 이유는 스핀 락은 결국 CPU 작업을 하는데, 싱글코어이면 컨텍스트 스위칭이 발생한다.

## 세마포어

signal mechanism 을 가진, 하나 이상의 프로세스/스레드가 critical section에 접근 가능하도록 하는 장치

```c
class Semaphore {
    int value = 1;
    int guard = 0;
}

Semaphore::wait() {
    while (test_and_set(&guard));
    if(value == 0) {
        ... 현재 스레드를 큐에 넣음
        guard = 0;
    } else {
        value -= 1;
        guard = 0;
    }
}

Semaphore::signal() {
    while(test_and_set(&guard));
    if(큐에 하나라도 대기중이라면) {
        그 중에 하나를 깨워서 준비 시킨다;
    else {
        value += 1;
    }
    guard = 0;
}
```

```
semaphore->wait();
... critical section
semaphore->signal();
```

1외에도 여러 값을 가질 수 있다.
프로세스/스레드가 동시에 들어갈 수 있다. ( 카운팅 세마포어 )
물론, 세마포어도 value 를 1로 지정할 순 있다. - 이진 세마포어

signal 을 기반으로 순서를 정할떄 사용 가능하다.

task 1 : semaphore -> signal()
task 2 : semaphore -> wait() : task3

와 같이 되어있더라도, 변수값을 통해 task3 가 task1,2 끝나고 실행되는걸 보장한다. ( 추가로, 다른 스레드에서도 실행이 될 수 있다. )

## 뮤텍스 VS 세마포어

뮤텍스는 락을 가진자만 락을 해제 할 수 있지만, 세마포는 그렇지 않다.
뮤텍스 락은 CPU나 프로세스에 소유 ( 락을 가진자만 해제 )

뮤텍스는 priority inheritance 속성을 가진다. ( 세마포어는 속성이 없다. - 누가 signal 날릴지 알 수 없으므로 )
> 높은 우선순위 P1이 낮은 우선순위 P2에서 가진 락을 대기한다면?
> -> 무한정 대기가 발생할 수 있다.
> -> P2의 우선순위를 P1만큼 올려 P2가 빨리 critical section 을 나올수 있게 해준다.

=> 상호 배제가 필요하다면 뮤텍스를, 작업 간 실행 순서 동기화가 필요하다면 세마포어를 권장

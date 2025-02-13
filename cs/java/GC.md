# GC

> 학습 링크 : https://mangkyu.tistory.com/118

유효하지 않은 메모리를 알아서 정리해준다.
- 대부분의 객체는 금방 접근 불가능
- 오래된 객체에서 새로운 객체 참조는 매우 드뭄


> 밑에 내용들은 GC 알고리즘에 따라 조금씩 달라지나
> 기본적인 개념은 동일하다.

##  Young

새로운 객체가 할당되는 영역 ( 많은 개체가 이 부분에서 사라짐 )
Minor GC

## Old

Young 보다 크게 할당 + 큰 만큼 적게 할당 된다.
Major GC

### Card Table

Old 영역 객체가 Young 영역 객체 참조마다 정보 표시
-> Minor GC가 실행될 때 모든 Old 영역 객체 검사해, 참조되지 않는 Young 객체 식별하는게 비효율

## 동작 방식

메모리 구조에 따라 세부적 동작 방식은 다르나, GC가 실행된다고 하면 2단계를 따른다.

1. STW
2. Mark & Sweep

###  STW

Stop The World
GC 를 위해 JVM 이 애플리케이션 실행 멈추는 작업

GC 실행 제외 모든 스레드 중단후, GC 완료 후 작업 재개
=> 결국, GC 성능 개선은 이 STW 시간을 줄이는 것

## Mark & Sweep

- Mark : 사용되는 메모리와 사용되지 않는 메모리 식별
- Sweep : Mark 에서 사용되지 않는 식별 메모리 해제 작업

 ### Minor GC

Young 영역을 GC 하는 것을 의미
Young 영역은 1개의 Eden 영역과 2개의 Survivor 영역
GC 가 수밀리초 사이 끝난다.

- Eden 영역 : 새로 객체 할당되는 영역
- Survivor 영역 : 최소 1번 이상 GC 살아남은 객체 존재 영역

1. 새로 생성된 객체가 Eden 영역 할당
2. 객체가 계속 생성되어 Eden 영역에 꽉차게 되면, Minor GC 실행
    a. Eden 영역에서 사용되지 않는 객체 메모리 해제
    b. Eden 영역에서 살아남은 객체가 1개의 Survivor 영역으로 이동
3. Survivor 영역이 가득 차게 되면, Survivor 영역의 살아남은 객체 다른 Survivor 영역으로 이동
   ( 1개의 Survivor 영역은 빈 상태를 보장 )
4. 이러한 과정에서 반복해서 살아남은 객체는 Old 영역으로 이동 ( Promotion )

Object Header 에 살아남은 횟수 age 를 기록한다.

### Major GC

Old 영역을 GC 하는 것을 의미
Old 영역은 Young 영역보다 크며, Young 영역을 참조할 수도 있다.

Major GC 는 일반적으로 Minor 보다 시간이 오래 걸림 ( 10배 이상도 가능 )

### Full GC

Minor 와 Major GC 를 동시에 하는 것


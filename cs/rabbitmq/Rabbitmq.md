
### AMQP

Advanced Message Queueing Protocol - MQ ( Message Queue ) 기반 프로토콜

서로 다른 시스템 간 효율적인 방법으로 메시지 교환하기 위해 탄생

![](https://i.imgur.com/VT8wm6F.png)

- Producer ( Publisher ) : 메시지를 보내는 곳
- Consumer ( Subscriber ) : 메시지를 받는 곳
- Exchange : Producer 로부터 메시지 수신하는 곳, 수신한 메시지를 큐에 분배
- Queue : 메시지 저장하는 곳, 저장했다가 Consumer 에 전달
- Binding : Exchange 와 Queue 의 Mapping, 1:1 or 1:N

=> Exchange 가 Producer 로부터 메시지 받고 Queue 에 전달, Queue 는 Consumer 에게 전달

- Application <-> DB 라면?

애플리케이션이 DB를 사용하면 애플리케이션에서
직접 DB 커넥션 관리하고 DB에 데이터 관련 요청 및 응답 받음

-> DB에 지연 or 장애 발생 시 직접적 영향 받음

- Application <-> AMQP <-> Consumer <-> DB

애플리케이션은 큐에 데이터 전달 ( DB 장애에 대한 영향 즉시 받지 않음 )

- Application <-> AMQP <-> Consumer 1 <-> Application 2
  <-> Consumer 2 <-> Application 3

Server to Server API call 에서도 장애가 전파되지 않는다.
( 추후, Application 4 가 추가된다고 해도 수정할 필요 없다. )

=> RabbitMQ 는 AMQP 를 구현한 Message Broker 이다.

## 메시징 시스템

### 메시지

일반적으로 데이터는 바이트 스트림으로 전달 ( header 와 body 로 구성 )

publisher-subscribe : rabbitmq, `데이터를 발행하는 주체 - 구독해서 가져가는 주체`로 접근
producer-consumer : kafka, `토픽을 생산하는 주체 - 토픽을 소비하는 주체`로 접근

스타일은 굉장히 다양하다.

- 원격 프로시저 ( RPC )
- 데이터(문서) 전달
- 이벤트
- 대용량 데이터 ( 순서 중요 )
- 메시지 만료
- 데이터 포맷 일치

### 파이프필터

메시지에 대한 복잡한 처리 수행

![](https://i.imgur.com/3CDwSxz.png)

그냥 파이프라이닝 이라고 생각하자.

### 라우터

![](https://i.imgur.com/Hnip1jk.png)

라우터를 통해 메시징 시스템이 특정 큐가 받게 지정 가능하다.

> 개별 수신자들이 메시지 수신을 결정하는게 더 효과적이다.

### 변환기

보내는 쪽, 받는 쪽 사이 요구사항에 맞게 변환해주는 것
( JSON -> 변환기 -> XML - 간단한 예시 )

### 엔드포인트

발신측에서 엔드 포인트를 통해 메시지를 보냄
수신측에서 엔드 포인트를 통해 메시지를 받음


## RabbitMQ

### 용어 매핑

- Connection : 엔드포인트 - 메시지 생성, 채널 발신, 메시지 수신, 메시지 추출, 수신 애플리케이션에 전달
- Channel : 채널 - 메시지 전달하는 논리 주소 - 잘 디자인된 채널 집합읍 메시지 버스 형성
- Exchange : 파이프 필터, 라우터, 변환기
- Queue : 메시지 - 원격 프로시저, 텍스트, 이벤트(변경통지), 대용량 데이터

### Work Queue

부하 처리를 위해 consumer 를 여러 개로 두는 경우가 많다.

- Round-Robin Dispatching

consumer 가 여러 개면 producer가 생산한 메시지를 병렬적으로 처리할 수 있다.
돌아가며 메시지를 분배해, 평균적으로 같은 메시지를 받게 된다.

- Message Acknoledgement

consumer 가 중간에 죽는다면?
-> 다시 queue 에 들어가서 다른 worker 에서 처리되는걸 요구한다.

이를 위해 Message Acknoledgement 를 제공한다.

consumer 가 메시지를 받고 나서 ack 을 보낸다.
( 특정 메시지를 잘 받았고, 정상적으로 처리했다는 응답 )
-> consumer 가 ack 를 보내지 않고 죽으면, RabbitMQ 가 메시지가 완벽하게 처리 되지 않았다고 간주

task 가 처리되는 시간은 상황에 따라 다를 수 있으므로, timeout 을 잘 설정해야 한다.

- Message durability

messages, queue에 옵션을 넣어 메모리에 있는 메시지들을 디스크에 저장한다.
( 단, disk로 전달되기 전 서버가 죽을 수 있기 때문에 100% 보장은 불가능 )

- Fair Dispatch

하나의 worker 가 하나의 메시지만 받고, 메시지에 대한 ack 를 보내기 전까지 새로운 메시지를 받지 않는다.
-> 한가한 worker 에 메시지를 바로 분배 가능

![](https://i.imgur.com/5G2lgV2.png)
## Publish / Subscribe

RabbitMQ 는 producer 가 메시지를 직접 큐에 보내지 않는다.
-> exchange 에 보낸다.

exchange 는 prodcuer 로부터 온 메시지를 큐에 전달하는 역할을 수행한다.
( 메시지를 받았을 때 어떻게 전달해야 하는지 명시하는 규칙 )

### Exchange type

1. Direct : routing key와 binding key가 일치해야 한다. - 하나의 큐에 메시지 전달
2. Topic : 패턴 통해 routing key와 binding key를 매칭한다. - 하나 이상 큐에 메시지 전달
    - `*` : 단일 단어 대응 ( `quick.*.rabbit` -> `quick.oragne.rabbit` )
    - `#` : 0개 이상 단어 대응 ( `stock.#` -> `stock.usd.nyse` or `stock.europe.london` 등 )
3. Header : routing key 무시하고, `key:value` 로 이루어진 헤더 값으로 매칭한다.
   ( 이때 헤더는 Publish 할때와 Queue 와 Exchange 를 바인딩할때 설정 )
    - x-match (all or any) 를 사용해 모두 만족하거나 일부만 만족하는 메시지 받도록 설정
4. Fanout : routing key 무시하고, 연결된 모든 큐에 메시지 전달 ( 브로드캐스트 )


https://www.cloudamqp.com/blog/part1-rabbitmq-best-practice.html

## Queues

- Queue 를 짧게해라 ( 가능한 )

큐 내부 많은 메시지는 RAM 사용량을 과도하게 불러온다.
-> RAM 을 사용하기 위해, 메시지를 디스크로 flushing ( page out ) 한다.
-> page out 은 일반적으로 시간이 오래 걸리며, 할 메시지가 많을수록 큐 처리 속도가 block 된다.
=> 성능 저하

메시지가 많은 상태에서 클러스터 재 시작하면 인덱스 재구축에 시간이 많이 소요된다.
`+` 재시작 후, 노드 간 메시지 동기화(sync) 에도

- Quorum Queue 를 사용해라

3.8에서 가장 많이 바뀐 요소중 하나는 새로운 queue type Quorum Queues 의 추가

데이터 안정성과 미러링 성능을 향상시킨 큐 - Raft Consensus Algorithm 기반 HA 구성


#### Classic queue vs Quorum queue

AMQP 0.9.1 사양 기본 구현체
- 단일 노드 존재 ( 별도 복제, 동기화 메커니즘 X )

Classic Queue 는 HA 구성 + 메시지가 비효율적인 알고리즘 사용해 복제되므로 성능이 생각보다 느림

모든 읽기 & 쓰기는 master queue 통과 -> master 는 모든 명령 mirror queue 에 복제

모든 live mirror 가 메시지 받으면 master 가 publisher 에게 ack 보냄
- master 가 실패하면 mirror 가 master 로 승격
- queue 는 데이터 손실없이 사용 가능한 상태로 유지

브로커 재시작 시, mirror queue 에 있는 모든 데이터 삭제되는 문제
-> 재시작 or 실패되서 다시 올라올 때 동기화 된 메시지가 많아지면 실패 지점이 일어날 수 있는게 단점이였다.

Quorum Queue 는 디스크 기반 분산, 복제된 메시지 큐
Classic Queue, Mirrored Queue 보다 내구성과 일관성을 대폭 강화했다.

1개의 리더 큐 - N개의 팔로워 큐 -> 1+N 만큼 병렬 처리 가능

publish 시 : leader 가 로그에 기록 후, follwer 로 복제 -> 과반수(quorum) 이상 기록 완료시 Ack 리턴
-> 메시지가 디스크에 남아있다는 강한 내구성 보장

leader 노드가 장애로 내려가면, 가장 최신 로그 가진 follower 중 하나가 자동 승격 - failover

- 강한 내구성 : 모든 메시지는 디스크 로그에 쓰이고, 복제된 노드에도 안전하게 보관
- 일관성 : 과반수 복제를 거쳐 Ack 를 반환하므로, 소비자가 받은 메시지는 안전히 기록된 상태
- 온라인 복구 : 장애 리더 복구되어도, 끊긴 부분부터 복제 & 재개 ( 버리거나 재전송 필요 X )

- Non-durable 메시지 미지원 : 반드시 디스크 기록
- TTL 지원 제한 : 큐 TTL, 메시지 TTL 설정 적용 X
- Lazy Queue 미지원
- 글로벌 Qos 미지원 ( `channel.basicQos(prefetchCount, global=true)` )

### Lazy Queue 를 사용하라

즉시 디스크 기록 -> 메모리 축적 거의 X -> 일관된 디스크 I/O 패턴 -> 예측 가능한 처리 지연

- 배치성 작업 : 하루에 대량 로그 한번에 모아서 보내는 경우
- 버스트 트래픽 : 쇼핑몰 세일 이벤트처럼 짧은 시간 트래픽 폭증할 때
- 퍼블리셔 주도 : 퍼블리셔 속도가 소비자 처리 속도 압도할 것으로 보일 때

메시지를 처리(퍼블리싱 -> 컨슘) 하는 시간은 늘어나나
대량 메시지나 퍼블리셔 속도가 빠를 때도 예측 가능한 성능 제공

3.12 버전부턴 class queue 가 lazy queue 와 유사하게 동작한다.

### Limit queue size with TTL or max-length

다량 메시지 스파이크에 노출되고, 처리량이 1순위인 애플리케이션에서는
큐에 최대 길이를 설정하자.

큐의 앞쪽 오래된 메시지부터 자동으로 버려 짧게 유지한다.

### Number of queues

각 큐는 단일 스레드로 동작한다.
-> 하나의 큐는 오직 한 CPU 코어에서만 메시지를 처리한다. 큐와 그에 대응하는 소비자를 띄워야 한다.
=> 4코어라면, 큐도 4개 - 소비자도 4개로, 각 코어에 분산

클러스터 일시, 큐를 여러 노드에 고르게 분산해, 노드별 코어를 활용하게 하자.

-> 수동 분산의 번거로움을 위해 아래 2가지 플러그인을 사용할 수 있다.

#### Consistent Hash Exchange

해시 함수 기반 라우팅 키 기반으로 여러 큐에 메시지 균등 분배

- 퍼블리셔가 큐 개수 및 바인딩 몰라도, 해시 기반 자동 로드밸런싱
- 멀티코어, 멀티노드 환경에서 큐 고르게 활용 가능

#### Rabbit Sharding Plugin

논리큐를 여러 샤드 ( 물리 큐 ) 로 자동 분할해 각 노드 배치

- 큐 선언시, 클러스터 내 모든 노드에 물리 큐 자동 생성
- 백그라운드에서 여러 샤드 병렬 처리 ( 샤드 수 조정해 확장성 및 병렬 처리량 증가 )

```
rabbitmq-plugins enable rabbitmq_consistent_hash_exchange
rabbitmq-plugins enable rabbitmq_sharding

// Consistent Hash Exchange 생성
channel.exchangeDeclare("hash-exchange", "x-consistent-hash", true)

// Sharded Exchange 에 샤드 정책 설정
rabbitmqctl set_parameter shardingV2 '{"shards-per-node":5}'
```

![](https://i.imgur.com/jFoJglQ.png)

중간 Exchange 가 라우팅

### Don’t set your own names on temporary queues

임시 큐를 사용할 때는 이름을 지정하지 말자.

```java
channel.queueDeclare(
    "",    // 임시 큐 이름 대신 빈 문자열
    false, // durable
    true,  // exclusive
    true,  // auto-delete
    null   // arguments
);
```

빈 문자열 시, UUID 형태를 반환해 유일성 보장

### Auto-delete queues you are not using

사용하지 않는 큐에 자동 삭제 설정하자.

클라이언트가 비정상 종료 or 소비자 취소시
고아 큐(orphaned queue) 로 남아 리소스 낭비하지 않도록 자동 삭제 설정 가능하다.

```bash
rabbitmqctl set_policy \
  stale-queues  ".*" \
  '{"queue-ttl":2419200000}' \
  --apply-to queues
```

마지막으로 소비되지 않은 상태로 지정 기간만큼 방치되면 자동으로 삭제

```java
channel.queueDeclare(
  "",    // 빈 이름 → 서버 자동 할당
  false, // durable
  false, // exclusive
  true,  // auto-delete
  null
);

```

마지막 소비자 취소 or 해당 채널 / 연결 닫힐 때 큐 삭제

```java
channel.queueDeclare(
  "",    // 이름 생략
  false, // durable
  true,  // exclusive
  false, // auto-delete
  null
);
```

선언한 특정 연결 에서만 접근 허용
-> 연결 종료( or TCP 세션 끊김 ) 되면 자동으로 삭제

### Set limited use of priority queues

Erlang VM 내부에서 우선순위별 별도 큐가 생성되어 리소스 소모
-> 대부분 경우 우선순위 레벨 5개 이하 제한하는 것이 충분
( 3~5 레벨 사이에서 테스트 해보는 것도 방법 )

- 너무 적으면 우선순위 효과 희석, 너무 많으면 오버헤드 증가

## Payload

초당 처리해야 하는 메시지 수가 메시지 크기보다 훨씬 더 명목이 된다.

작은 메시지를 연속으로 보내면 네트워크 및 브로커 처리에 대한 오버로드가 커진다.
-> 한 번에 묶어 보내고, 소비자가 내부 다시 분할하게 처리하면 오버헤드 감소 효과가 있다.

- 번들 메시지 하나라도 장애가 나면, 그 안 모든 개별 메시지를 재처리 해야 할 수 있음 ( 판단 역시도 어려움 )
- 네트워크 대역폭, 브로커 처리 시간, 장애 복구 단위를 고려해 적절한 번들 크기 설정

## Connections & Channels

AMQP 연결 하나당 약 100KB RAM 소모 ( TLS 사용 시, 더 소모 )
-> 수천 개 연결 시, 서버 OOM 발생 가능성이 매우 높아짐!!

`newConnection` 호출 시, 하나의 TCP 소켓과 Erlang VM 프로세스 생성
-> 프로세스가 수신되는 AMQP 프레임 모두 읽고 처리해 프레임 단위 응답 전송
-> Erlang 프로세스의 힙, 스택, 메시지 큐(mailbox) 등이 필요하므로, 100KB 이상 메모리 할당

하나의 TCP 연결 위에서 여러 채널을 만들어 스레드당 분리해 사용이 가능하다.
-> multiplexing

- 연결 handshake 는 최소 7개의 TCP패킷 ( SSL/TLS 시 더 많음 ) 필요함 - 자주 열고 닫으면 지연이 커짐

프로세스당 1개의 장기 연결 유지 -> 스레드당 1개의 채널 생성 후 재사용

### Don’t share channels between threads

스레드 간 채널 공유 금지

채널을 스레드 안전하게 설계하지 않았다.
-> 채널을 여러 스레드가 동시 사용하면 내부 상태 결합 ( race condition ) 및 성능 저하 발생할 수 있다.
=> 스레드당 채널 을 명심

### Don’t open and close connections or channels repeatedly

연결·채널 반복 열기/닫기 금지

매번 `newConnection` -> `createChannel` -> `close` 하면
TCP Handshake 비용으로 지연(latency)이 늘어남

### Separate connections for publisher and consumer

발행자, 소비자 연결 분리

같은 연결에서 발행과 소비 함께 처리 시
- 서버가 백프레셔 걸 때, 소비 확인이 지연되어 전반적인 소비 처리량 감소
  -> 두개의 연결을 따로 사용하여 상호 간섭 최소화

### A large number of connections and channels might affect the RabbitMQ management interface performance

다수 연결 & 채널이 관리 인터페이스에 미치는 부하

관리 UI 는 각 연결 & 채널의 메트릭(상태,처리량,메모리 등등)을 수집,분석,표시한다.
-> 연결 & 채널이 많아질수록 UI 응답 속도가 느려지고 브로커 CPU,메모리 사용량이 추가로 증가할 수 있다.

=> 위 동일하게, 장기 연결을 하고 & 채널을 재사용 할 것

## Acknowledgements and Confirms

확인 응답과 승인

전송 도중 연결 끊기면 메시지가 유실 될 수 있고, 재전송이 필요하다.
-> Ack(확인 응답)을 통해 서버와 클라이언트가 언제 재전송 가능한지 결정한다.

클라이언트는 메시지 수신 시점에 ACK 를 보내거나, 완전히 처리 완료 시점에도 보낼 수 있다.

- 자동 ACK : 메시지 가져오는 즉시(서버 -> 클라이언트 전송) 자동으로 ACK 를 보낸다.
  ( 가장 빠른 처리량, 클라이언트가 실제 처리 중 크래시 나면, 그 메시지는 완전히 날라감 )

- 수동 ACK : 클라이언트가 명시적 `basicAck` 호출해야 서버가 메시지를 삭제
  ( 처리 완료 전 까지 메시지 보호 - 한 번 이상 전송 보장, Ack 호출 오버헤드로 인해 처리량 낮아짐 )

- Publish Confirm  : 발행자 쪽에서 `서버가 이 메시지를 받았다` 는 ACK 받는 메커니즘

한번 이상 발행 보장을 하나, Ack 처리로 인해 성능 저하 존재

```java
channel.confirmSelect(); // confirm 모드 활성화
channel.basicPublish(exchange, routingKey, props, body);
channel.waitForConfirmsOrDie(5000); // 타임아웃(ms) 내에 서버 Ack 대기
```

-> 비동기 ConfirmListener 를 등록해 콜백으로 처리하면, 병렬 발행 시 성능이 조금 더 나아질 수 있다.

### Unacknowledged messages

ACK 되지 않은 메시지는 서버 RAM 메모리에 상주한다.
-> 너무 많은 메시지는 OOM 유발
clients 측의 `prefetch` count 를 통해 메시지를 몇개를 유지할지 설정할 수 있다.

## Persistent messages and durable queues

어떤 메시지도 손실하면 안되면, queue 를 `durable` 로 설정 + 메시지를 `persistent` 배달 모드로 설정해라

- durable : 브로커가 재시작 되거나 크래시가 나도 큐 정의 자체가 살아남아 복구
- persistent : 브로커가 디스크에 기록하도록 플래그 설정 - `DeliveryMode.PERSISTENT`
  -> 메시지를 디스크에 flush ( 디스크 I/O 오버헤드 발생해, 순수 메모리 저장보다 처리량 떨어짐 )

- 절대 메시지 유실 불가 : Durable Queue + Persistent Message
- 최고 성능 : Non-Durable Queue + Transient Message
  -> 큐의 특성에 맞게 적절히 선택을 하자.

Lazy Queue 는 메시지를 가능한 디스크에 보관, 필요 시 메모리로 불러오는 모드이다.

-> 비지속성 메시지라도 Persistent Queue 못지않은 디스크 부하 발생한다.

## TLS and AMQPS

RabbitMQ 는 TLS 로 래핑한 AMQP 프로토콜을 제공한다.
-> TLS 는 모든 트래픽을 암호화/복호화 하므로 성능 저하를 일으킨다.
( 세션 수립 과정에도 다수의 패킷 교환 일어남 - 클라이언트 헬로우, 서버 헬로우, 키 교환, 인증 등등 )

VPC Peering 을 통해 같은 클라우드 환경 내 별도 암호화 계층 없이 프라이빗 네트워크 수준 트래픽 격리 가능
( 암호화 비용 없이, 네트워크 레벨 보안 가능 )

Cipher Suite 우선순위를 지정 가능하다.

> Cipher Suite : TLS 통신 할 때 실제 어떤 알고리즘 쓸지 서버와 협상
> 이때, 선택 가능한 알고리즘들 묶음이 Cipher Suite
> `TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384`
> 키 교환은 ECDHE_RSA -> 대칭 암호화는 AES_256_GCM -> 메시지 인증은 SHA384

보안과 성능 트레이드오프 ( `256 이 보안이 강하나 처리 비용 높다.`, `128이 보안 약하나 처리 비용 낮다.` 등)

- 가장 빠른 -> 안전한 순, 가장 안전 -> 빠른 순 목록 정해주면 협상 시 우선권 갖고 선택된다.

```java
// 1) SSLContext 생성  
SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
// 2) TrustManager / KeyManager 등 기본 설정  
sslContext.init(null, null, null);

// 3) SSLSocketFactory 가져오기  
SSLSocketFactory factory = sslContext.getSocketFactory();

// 4) 지원할 Cipher Suite 목록 지정  
String[] preferredCiphers = new String[] {
    "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
    "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
    "TLS_RSA_WITH_AES_128_GCM_SHA256"
};

// 5) RabbitMQ ConnectionFactory에 적용
com.rabbitmq.client.ConnectionFactory connFactory = new ConnectionFactory();
connFactory.useSslProtocol(sslContext);
// 아래 두 줄로 호스트 검증 활성화
connFactory.enableHostnameVerification();
connFactory.setPort(5671);

// 사용자 정의 소켓 팩토리로 Cipher 우선순위 설정
connFactory.setSocketConfigurator(socket -> {
    if (socket instanceof SSLSocket) {
        ((SSLSocket) socket).setEnabledCipherSuites(preferredCiphers);
    }
});
```

( 이게 뭐지... )

## Prefetch

한 번에 소비자에게 보내는 미확인 메시지의 최대 개수
-> 소비자가 가능한 계속해서 일거리 갖고 있도록 하되, 클라이언트 쪽 버퍼에 너무 많은 메시지를 쌓여있지 않게

기본 설정은 무제한 이여서, 준비 되었다고 보이는 소비자에게 최대한 많이 밀어넣음
( 클라리언트 라이브러리 쪽에서 캐싱하다, 실제 처리 일어나고 Ack 보낼 때 까지 유지 )

- Prefetch 제한 전

서버는 준비된(ready) 소비자에게 가능한 많은 메시지를 보낸다.
-> 클라이언트 버퍼에 잔뜩 쌓여서, 다른 소비자에게 메시지가 전달되지 않아 `홀드아웃` 상태 발생 가능

- Prefetch 제한 후

서버는 prefetch 개수만큼 메시지를 보낸다.
-> 클라이언트가 Ack 를 보내면, 다음 메시지 추가 전송
( 즉, 클라이언트 버퍼에 무한정 쌓이지 않는것이 중요함 )

Prefetch 로 꺼낸 메시지는 Ack 전까지 큐에서 보이지 않는다.
( 많이 잡으면, 한 소비자가 큐 과점, 너무 적게 작으면 매번 서버로부터 요청 )

### 적절한 prefetch 값

- 소비자 수가 적고 처리 속도가 빠른 경우
    - Prefetch 크게 - 50 ~ 100
    - 클라이언트 최대한 바쁘게 유지
    - Round-trip / 처리시간 = 권장 prefetch

> Round-trip : 말 그대로, RabbitMQ Server -> Client

![](https://i.imgur.com/RQAqdLA.png)

- 소비자 수가 많고 처리 시간 짧은 경우
    - Prefetch 중간 - 5 ~ 20
    - 너무 낮으면 자주 기다리고, 너무 높으면 일부만 바쁘게 된다.

- 소비자 수가 많고 처리 시간 긴 경우
    - Prefetch 1로 줄여 메시지 고르게 분배
    - 느린 소비자가 너무 많은 메시지 가져가지 않게 방지

- auto-ack true 로 설정시 prefetch 제한이 무시된다.
- 무제한 prefetch 사용 시, 한 소비자가 메모리 모두 쓰고 크래시 나며 메시지 재전달되는 상황 발생할 수 있다.

## Cluster setup with at least 3 nodes

> 노드 : 독립적으로 실행중인 Erlang VM 위 RabbitMQ 서버 인스턴스 ( 1:1 매칭이 일반적 )
> 클러스터링 노드 : 클러스터 구성하는 개별 서버 인스턴스

서로 다른 영역에 위치시키는게 일반적 - 가용성

클러스터 노드 수와 동일한 복제 인스턴스를 가지는 `합의형(Quorum) 큐`가 기본값
( `quorum_cluster_size=5` 로 설정하면, 5개의 복제본 ( 1대 리더 + 4개 팔로워 ) 이 분산 배치되어
장애 복원력(resilience)가 높아진다.)

한 노드가 장애 나도 다른 노드에서 메시지 계속 처리 및 복구 가능

-> 복제본 수가 늘어날수록 디스크 I/O 와 네트워크 사용량이 증가하므로, 성능 및 비용이 함께 올라간다.

## Routing ( Exchange )

- 라우팅 키가 정확히 일치하는 큐에만 메시지 전달하는 다이렉트 교환기가 가장 단순하고 빠르다.

- 토픽,헤더,팬아웃 등 조건이 복잡해질수록 내부에서 `어디로 보낼지`에 대한 계산을 해야한다.

---
## TTL


Dead-Lettering 과 TTL 은 주의해서 사용되야 하는 기능들이다.
-> 둘다 예상치 못한 부정적 퍼포먼스 유발한다.

### Dead Lettering

`x-dead-letter-exchange` 가 활성화 되어있으면
거절(rejected) or 승인되지 않은(nacked-not acked) or 만료된( expired with TTL ) 메시지를
dead-letter-qeue 로 보낸다.

### TTL

`x-message-ttl` 가 활성화 되어있으면
메시지는 특정 시간이 되었을때 소비 되지 않았으면 큐로부터 폐기된다.

## RabbitMQ Streams

고성능 및 지속성을 사용하거나 필요하다면 ( 로그 같은 )
streams 를 사용하면 된다.

Kafka 와 유사한 `append-only log` 구조로, 수십만 메시지/초 처리 가능 - 디스크 기반 세그먼트 저장

- 기존 큐 기반 처리 한계를 느낄 때
- 이번트 소싱 or 로그 집계 or 대용량 데이터 파이프라인으로 사용

## 기타

- 불필요한 플러그인 비활성화 - 특정 플러그인은 많은 CPU 나 높은 메모리를 차지한다.
  -> 사용하지 않는 것은 비활성화 하자. ( 운영 서버에서 추천하지 않음. + )

- 최신 클라이언트 라이브러리 : 프로토콜 개선, 버그 수정, 성능 최적화 등이 꾸준히 이뤄진다.

- 최신 안정 RabbitQM,Erlang 버전 사용 : 새로운 기능, 보안 패치, 퍼포먼스 등 활용 가능
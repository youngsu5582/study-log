# Exchange

Producer 로부터 받은 메시지를 어떤 큐로 보낼지 결정하는 일종의 Router

```
Producer ──▶ Exchange ──▶ Queue ──▶ Consumer
                    ╱│╲
          Binding  ╱ │ ╲  Binding
                 ╱  │  ╲
            QueueA QueueB QueueC
```

Producer 는 메시지를 보낼 때, `exchangeName` 과 `routingKey` 를 지정해서 보낸다.

```java
@Override  
public void convertAndSend(String exchange, String routingKey, final Object object) throws AmqpException {  
    convertAndSend(exchange, routingKey, object, (CorrelationData) null);  
}
```

자신에게 바인딩된 Queue 의 Binding Key or Pattern 과 비교해서 일치하는 Queue 에 메시지를 복사해 전달한다.

> exchange 를 빈 문자열로 보내면 기본 exchange 를 통해 동작한다. ( routingKey 를 queue 로 매핑시켜 보냄 )

![](https://i.imgur.com/70kdeqL.png)

## 장점

### 생산자 - 소비자 완전 분리 ( Decoupling )

생산자는 `메시지를 보낸다는 행위`만
소비자는 `어떤 키에 관심있다` 만 선언한다.

-> 서로 구현을 몰라도 독립적 개발,배포,확장 가능해진다.

### 다중 소비자 ( 멀티캐스트 ) 지원

Fanout Exchange 를 통해, 하나의 메시지를 여러 Queue 로 복제해서 보낼 수 있다.

EX) 로그 저장, 모니터링 알람, 통계 큐 등을 바인딩만 추가하면 된다.

#### Fanout Exchange

발행된 메시지를 자신에게 발행된 모든 큐로 무조건적 복제 해서 보낸다

```java
  @Bean
    public FanoutExchange exchange(RabbitmqConfig cfg) {
        return new FanoutExchange(
            cfg.getTopicExchangeName(),
            true,                      
            false                      
        );
    }

    @Bean
    public Queue queueA() {
        return new Queue("queue.A", true);
    }
    
    @Bean
    public Binding bindingA(FanoutExchange exchange, Queue queueA) {
        return BindingBuilder.bind(queueA).to(exchange);
    }
```

Producer 코드를 추가하지 않고, 바인딩만 추가하면 새로운 큐도 메시지를 받음
-> Queue + Binding 만 추가하면 된다.

### 유연한 라우팅 정책

- Direct : 정확하게 일치한 큐
- Topic : 와일드카드로 주제 기반 필터링
- Headers : 헤더 내용 기반 분기

#### TopicExchange

- `*` : 단어 하나 정확히 매칭 - `error.*` -> `error.db`, `error.ui`
- `#` : 0개 이상 단어 | 주제 기반 라우팅 - `#.critical` -> `system.critical`, `a.b.c.critical`
- `x-match=all` : 메시지 헤더에 넘겨준 모든 키 - 값이 모두 일치 해야만 해당 큐로 라우팅
```java
args.put("x-match", "all");
args.put("format",  "pdf");
args.put("type",    "report");
```

format, type 이 모두 일치해야함

- `x-match=any` : 메시지 헤더에 넘겨준 키 - 값중 하나라도 일치 하면 해당 큐로 라우팅

```java
args.put("x-match", "any");
args.put("format",  "pdf");
args.put("type",    "report");
```

`format`, `type` 중 하나만 일치해도 상관없음

### 운영 관리 측면 이점

RabbitMQ 모니터링 툴에서 한눈에 볼 수 있어 토폴로지 파악하기 쉬움
새 큐를 붙이거나 제외할 때 역시 서비스 중단 없이 실시간 반영 가능
`비정상 라우팅 대체 경로`, `Dead-Letter-Queue` , `메시지 TTL` 등 고급 기능을 중간에서 설정 가능

---

## 기타

- 기본 Exchange 는 Bean 으로 등록할 필요 없다.

### 선언옵션

- durable : 브로커 재시작에도 남아있어야 하는 여부
- autoDelete : 마지막 바인딩 끊길시 자동으로 삭제하는지 여부
- internal : RabbitMQ 내부 용도 exchange 로 사용할 때 사용
- arguments : 플러그인 및 정책 지정 가능
    - alternate exchange : 어떤 큐에도 라우팅 되지 못한 메시지 모아두는 교환기

등등

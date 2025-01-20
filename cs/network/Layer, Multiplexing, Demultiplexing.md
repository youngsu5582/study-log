- 학습 링크 : [다른 예제로 소켓 식별 방식을 설명합니다. segment datagram packet frame payload multiplexing demultiplexing 개념도 설명합니다.](https://www.youtube.com/watch?v=eveNtda0_yk)

### OSI Layer

Application -> Presentation -> Session -> Trasnport -> Network -> Data Link -> Physical

애플리케이션 -> 표현 -> 세션 -> 전송 -> 네트워크 -> 데이터 링크 -> 물리

- 네트워크 시스템 구축을 위해 범용적 + 개념적 모델

### TCP/IP Stack

Application -> Trasnport -> Internet -> Link

- 인터넷이 발명되며 함께 개발된 스택
- 인터넷 구조에 특화된 계층 구조

## Layer

각 레이어는 Header 와 Payload 를 포함한다.
- Header : 프로토콜이 동작할 수 있게 도와주는 부가적인 요소 및 필수적인 요소 포함
- Payload : 해당 레이어의 Header 를 제외한 나머지 요소들 ( 상대적인 요소 )

각 Layer 를 내려가면, 각 레이어에서 요청에 필요한 데이터들을 헤더에 붙인다.
각 Layer 를 거슬러 올라가면, 각 레이어에서 필요한 데이터들을 헤더에서 추출하고 제거한다.
### Application Layer

애플리케이션이 네트워크 서비스 이용하도록 인터페이스 제공
- Data(Message) : Application 에서 송 / 수신 되는 데이터
- Application Layer 는 Application Layer Header 포함

### Transport Layer

S.PORT , D.PORT 포함
데이터 송신자 - 수신자 간 신뢰성 있는 데이터 전송 보장
- TCP Segment : TCP 프로토콜에서 송 / 수신 되는 데이터
- UDP Datagram : UDP 프로토콜에서 송 / 수신 되는 데이터

### Internet Layer

S.IP_ADDRESS,D.IP_ADDRESS 포함
호스트 - 호스트 간 네트워크 연결 담당 ( 라우팅을 통해 최적 경로로 수행 )
TCP 인지 UDP 인지도 포함한다. ( 구조가 조금 다르므로 미리 알고 있어야 처리할 수 있다. )
- IP Datagram or Packet : Internet 프로토콜에서 송 / 수신 되는 데이터
### Link Layer

물리적인 데이터 전송 담당
Header 와 맨 끝 Trailer 를 붙인다. ( 오류 검출용으로 `Etherenet` 에서 사용 )
- Frame : Link 계층에서 송 / 수신되는 데이터

## Trasnport Layer 연결 식별

### UDP 식별

S.IP_ADDRESS, S.PORT, D.IP_ADDRESS, D.PORT 를 활용해
이와 일치하는 소켓에 매칭만 하면 끝이다.

### TCP 식별

두가지로 나뉜다.
#### 첫 연결
SYN 플래그가 1이며
D.PORT 에 해당하는 PORT 내 존재하는 소켓이 Listening 소켓이면 연결이 성립된다. - 3 Way Handshake

#### 이미 연결

UDP 식별과 유사하게 S.IP_ADDRESS, S.PORT, D.IP_ADDRESS, D.PORT 를 활용해
일치하는 소켓에 매칭한다.

### Demultiplexing

Internet Layer 로 부터 받은 Segment 나 Datagram 에 있는 Payload 를 적절한 소켓으로 전달한다.

### Multiplexing

여러 소켓들이 데이터를 수집해 각각 Segment (TCP), Datagram(UDP) 만든 후 Internet Layer 로 보낸다.
- 하나의 네트워크 연결로 여러 프로세스가 공유하게 도와준다

> Packet 은 인터넷 통해 송 / 수신 되는 데이터 단위로, Layer 구분 없이 범용적으로 사용한다.
### Proxy??

- 의미: `대신 처리`

-> Proxy Server 는 대신 처리를 해주는 서버
클라이언트 -  서버 통신 간 중계 서버 및 대리 수행을 해주는 서버

### Forward Proxy

일반적인 Proxy

`Client -> Forward Proxy -> Internet -> Server`

- 캐싱 : 클라이언트의 요청을 캐싱
  전송 시간을 절약, 불필요한 외부 중복 전송 X, 외부 요청 감소

흔히, 사용자가 자주 요청하는 `이미지, CSS, HTML` 등을 프록시 서버에 저장해서 재사용 할 수 있다.

- 익명성 : 클라이언트가 보낸 요청을 감춤, Forward Proxy 가 요청을 보낸 것 처럼 전송
  ( 흔히, 토르 같은 고급 VPN - 서버는 어떤 IP 로 요청이 왔는지 알지 못함 )

추가적으로, 서버 로직 중 클라이언트가 외부 서버에 요청할 때 내부 클라이언트 구조 및 IP 를 알 수 없다. - 잠재적 공격을 방어하는 첫 번째 방어선

- 접근 제어 : 내부 사용자가 특정 외부 인터넷에 접근하는 것 역시 제어 가능하다

Proxy 가 요청을 가로채서, 특정 도메인을 향하는 요청인지 확인 가능 ( 어떤 요청을 보내는지 로그 남기는 것도 가능 )

> 이는 반대로, 우회도 가능하다는 의미 - 지역 락이 걸려있어도? 특정 국가로 변경해 우회 가능

### Reverse Proxy

`Client -> Internet -> Reverse Proxy -> Server`

- 보안 : 서버 정보를 클라이언트로부터 숨길 수 있음
  ( 클라이언트는 Reverse Proxy 에 요청을 전송 -> Reverse Proxy 가 서버에 전달해줌 )


- 관심사 분리 : 실제 웹 서버는 비즈니스 로직 처리, Reverse Proxy 가 보안 등 처리 가능

암호화 / 복호화 등 처리 가능 - CPU 작업의 부담 저하
디도스 공격 Reverse Proxy 단에서 적절히 차단

서버에서 보낸 응답을 Reverse Proxy 가 압축해 클라이언트에게 전달해 데이터 전송량 줄여줌

- Load Balancing 기능
    - Round Robin ( 요청을 적당히 순서대로 공평하게 분배 )
    - Least Connections : 현재 연결(세션) 이 가장 적은 서버에게 다음 요청 보냄 - 특정 서버 부하 방지
    - IP Hash : IP 주소 해싱해 특정 서버로 전송, 사용자가 항상 같은 서버 연결되는걸 보장하게 할 수 있음

- 캐싱 : 자주 사용되는 정적 콘텐츠 캐싱 (이미지, JS, CSS 등)

### Load Balancing

들어온 요청을 분산해 서버에 전송
Scale up 의 한계로 Scale out 은 필연적
-> 여러 서버가 생기고, 여러 서버간 고루게 요청을 분산해줘야 한다

주로, L4 및 L7 에서 처리

L4 는 Transport Layer ( IP, Port ) -> 요청이 들어오면 그냥 고르게 나눠줌
L7 는 Application Layer -> 커스텀 하게 라우팅 가능 ( `/category` 는 서버 1, `/tag` 는 서버2 ... ) - Nginx, ALB 등등등

AWS 단에서는 특정 시간대에 맞춰 자동으로 서버를 늘리는 Auto Scailing 도 가능
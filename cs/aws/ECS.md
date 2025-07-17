## AWS ECS

완전관리형 컨테이너 서비스

- 뛰어난 성능 & 확장성

이미 AWS 서비스의 일부분도 ECS 로 운용되고 있다. ( SageMaker, Polly, Batch, Lex 등등 )

> ECS vs EKS?
> 단순성의 차이 ( 물론, 관리나 세세하게 차이나는 포인트들이 있음 )

![500](https://i.imgur.com/wYWHXFs.png)

## Constructs

### Cluster

- 리전 내 서비스 및 작업의 그룹화
- IAM 은 클러스터 대한 사용자 권한 제어

### Task

- ECS 클러스터에서 최소 실행 단위 ( 네트워킹, 스토리지, 파라미터, IAM 역할, 컴퓨팅 리소스 등 담당 )
- 작업 정의 내용을 기반으로 작업 배포
    - 배포 타입 설정 : Fargate, EC2
    - 컨테이너 이미지 매핑을 통해 정의 작업
    - 작업 역할 부여해 API 요청을 받을 때 권한에 따라 동작
- 작업 당 한 개 또는 최대 10개의 컨테이너 구성 가능

### Task definition

- 작업 시작하기 위해 ECS 에서 사용되는 템플릿
- docker 실행 매개변수와 유사 ( CPU/ 메모리, Container Image, Loggin, IAM Role )
- JSON 기반

![](https://i.imgur.com/mVFVZEX.png)

- 작업 정의를 여러개 하면, Revision 에 따라 다르게도 정의 가능
- 클릭으로도 생성 가능


### Service

지정된 작업 정의를 기반으로, 클러스터 내 원하는 수의 작업을 컨테이너 인스턴스에 지속적으로 유지 관리 해주는 구성 요소
ELB 와 연결 할 수 있기 때문에 여러 작업간 트래픽 분산이 가능

- ECS 위에서 여러 개의 작업을 실행
- Unhealthy Task 확인 및 교체
- 서비스 타입
    - Replica : 여러 개의 작업 개수를 유지하는 방식
    - Daemon: 컨테이너 인스턴스 하나당 한개의 작업을 배포하는 방식 ( 별도 오토 스케일링 불가능, 부가 기능을 위한 에이전트 느낌 )
- 배포 옵션 구성
    - Rolling Update : 점진 배포 ( MinimumHealthyPercent, MaximumHealthyPercent 로 작업 개수 제어 가능 )
    - Blue / Green : 블루(구버전), 그린(새버전) 문제 확인후 트래픽 전환 or 문제 발생시 트래픽 빠르게 롤백
      ( Code Deploy + Target Group 하나 더 설정해서 구성 가능 )
    - Service Auto Scailing

### Fargate Compute

작업 vCPU 및 RAM 기반 리소스 프로비저닝
- 초당 청구, 프로비저닝 만큼 비용 지불
- 온디맨드 비해 50~70% 저럼 비용으로 Fargate Spot 사용 가능

![](https://i.imgur.com/6dVxvoc.png)

하나의 EC2 에 여러개의 태스크 배치 가능
한 개의 태스크가 한 개의 Fargate Compute 에 할당 ( 커널, CPU,메모리 리소스 공유 X )

- Fargate 는 지금도 GPU 지원 X ( 2025.07.14 )
- 호스트 OS 단에 구축해야 하는 소프트웨어 요소 있다면 사용 불가능

=> 사실상, 우리 프로젝트는 Fargate 불가능

## ECS Networking

![](https://i.imgur.com/uw4xb3B.png)

- public 엔 NAT, ELB
- private 엔 Task Instance

외부로 요청을 보내면?
-> Task -> NAT Gateway -> Internet Gateway -> Internet

AWS 서비스를 사용하려면? ( ECR, S3 등등 )
-> VPC Endpoint 로 접근 가능! ( NAT 를 통한 비용 발생을 방지 가능  )

> public 에 위치한 Task Instance 가 외부로 요청을 보내면?
> -> Internet Gateway 를 사용

### Docker 는 어떻게 네트워크를 격리하는가

- Network Isolation

각 컨테이너는 자체적인 네임스페이스를 가진다. ( 라우팅 테이블, 소켓, Netfilter 등 가지고 있음 )
이러한 격리된 네트워크 환경을 연결해주는 것이 veth ( Virtual Ethernet Interface )

- veth : 컨테이너의 네트워크 네임스페이스와 호스트의 네트워크 네임스페이스를 연결하는 가상의 이더넷 인터페이스
  ( `--network=host Option` 사용하면 호스트의 네트워크 직접 사용 가능 )

호스트의 IP 와 별도 IP 를 가지게 된다.

### ECS Task Network Mode

#### host mode

![](https://i.imgur.com/mz0oxqh.png)


컨테이너가 EC2 네트워킹 네임스페이스를 사용 ( 같은 eni 를 사용 )

- 동일 포트 사용해 같은 호스트에 여러 컨테이너를 가질 수 없음
- 작업 정의에 컨테이너 매핑 부분에는 호스트 포트를 지정해야함

네트워크 성능이 중요하다면 고려할 수 있는 모드

#### bridge mode

![](https://i.imgur.com/UErYsa3.png)

컨테이너가 도커 제로의 가상 브릿지에 연결

- 동일한 호스트의 여러 컨테이너가 동일 포트 사용가능
- 작업 정의에서 컨테이너 포트를 정의할때, 호스트 포트를 0으로 지정하면 자동으로 동적 포트 할당해 호스트 포트에 매핑
  ( 임시 포트인, 32~61000 대역대 사용하므로 외부에 노출된 포트끼리도 충돌하지 않음 )

#### awsvpc

![](https://i.imgur.com/ZlIonsm.png)

사용자가 작업 한개당 하나의 ENI 를 가진다.
-> 네트워크 성능도 보장

개별 ENI 이므로 VPC 도 세밀하게 설정 가능

### ECS Service Communication

#### Service Discovery - Cloud Map

![](https://i.imgur.com/e77dvUF.png)

- 간단한 디스커버리 형식
- 클라이언트 - 프로바이더 직접 연결
- 트래픽 텔레메트리 제공 X
- DNS 는 기본 검색만 제공

ECS 가 Cluod Map 에 등록하고, Cloud Map 이 Route 53 에 등록해서
-> 클라이언트가 DNS 를 받아서 직접 요청하는 형식

#### ELB

![](https://i.imgur.com/NHFftDi.png)

- ELB 통해 연결하는 가장 쉬운 방법
- ELB 의 풍부한 기능 & 트래픽 지표 제공
- 추가 지연 시간 발생

#### APP Mesh

![](https://i.imgur.com/yFRySXn.png)

- 풍부한 트래픽 가시성 지표 제공
- 세밀한 트래픽 제어 & 암호화 및 인증 제공
- 유연성은 얻지만 복잡성 증가 ( `+` 러닝커브 )

## ECS Security

### IAM Role

![](https://i.imgur.com/wLUpXRb.png)

![](https://i.imgur.com/YW7zMHX.png)

엄연히 다른 두개의 권한이므로 잘 관리하자.

### Security Groups

![500](https://i.imgur.com/MtpdGHb.png)

같은 VPC 공유가 아니라, 보안 그룹 통해 단방향 및 적절한 권한만 갖게하자.

### Secrets


태스크가 프로비저닝 되는 시점에 파라미터 값들을 가져와 구성하게 해줘야 한다.

Secret Manager 는 Lambda 를 통해 주기적 Key Rotation 을 한다.

> 이게 현재, Devops 팀에서 하려고 하는것 ( 보안 대비 )

![](https://i.imgur.com/aKMR1bp.png)

## Logging & Monitoring

### Network and Host

- VPC Flow Log 활성화해 VPC 내 네트워크 트래픽 캡처
- CloudWatch 활용해 컨테이너 호스트 로그를 중앙 집중화 및 알람 설정
- 상태, 효율성, 가용성 보장 위해 컨테이너 호스트 모니터링

### Container

- 중앙 집중식 모니터링 및 저장 위해 컨테이너화된 애플맄이션 로깅 활성화
- 로그 라우팅 ( Fluentd, Logstash, FireLens )

## Monitoring & Observability

### 두개의 차이는 무엇인가.

모니터링은 빙산의 일각 ( 시스템의 일부만 관찰하므로 )

![](https://i.imgur.com/zKOVYlz.png)

CPU, Memory 등은 알 수 있으나 상세 파악이 어렵다.

- 모니터링되지 않은 메트릭이 비정상적으로 올라간다던가
- 비즈니스 측면에서 특정 데이터가 급감했다던가

=> 문제를 발견하더라도, 원인 분석에 오래 걸릴 수 있다.

![](https://i.imgur.com/HIeLcSw.png)

빙산 전체를 바라볼 수 있게 해준다.

- 분산 트레이싱
- 매트릭 모니터링

시스템의 근본 원인 추적, 전체 가시성을 제공해준다.

### Observability 구성 3가지 요소

- Logs : 시스템에서 발생하는 개별의 이벤트 기록 데이터
- Metrics : 시스템에서 측정한 수치 형태 데이터
- Traces : 한 사용자의 요청이 처리되는 전체 과정을 E2E 로 기록 ( 처음부터 최초 응답이 처리될 때 까지 요소를 포함 )

### Log 수집 위한 드라이버 - awslogs

- CloudWatch Logs 로 전송
- ECS 에서 가장 기본적인 로그 드라이버
- 5초에 1번씩 전송

Task Execution Role 권한만 부여하면 끝!
### Log 수집 위한 드라이버 - awslogs

![500](https://i.imgur.com/iuVAzRh.png)

- Fluent Bit / Fluentd 사이드가 컨테이너 사용하는 로그 드라이버
- 다양한 목적지 ( S3, Datadog, Splunk 등등 ) 저장할 수 있어 유연한 로그 플로우 정의 가능

### Metric 수집을 위한 드라이버 - Container Insights

클러스터 생성할 때 선택하자.

기본 제공은 클러스터 별 매트릭 수집, 활성화 시 태스크 개수 & 서비스 레벨별 지표 수집 가능

### Trace 수집 위한 - X-Ray SDK

![](https://i.imgur.com/NZXU5AV.png)

### OpenTelemetry

소프트웨어 성능 작동 이해하기 위한 분석 위한
측정 데이터 ( Metrics, Logs, Traces ) 를 계측, 생성, 수집 및 추출 가능

- 11개 다른 개발언어 지원
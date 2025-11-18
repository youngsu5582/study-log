
기초적인 지식부터 시작

## 인프라

IT 서비스를 제공하고 운영하는데 필요한 모든 기반 요소
-> 여러 리소스들로 구성

- 하드웨어 : 서버, 네트워크 장비(스위치, 라우터), 스토리지(SSD, 하드디스크)
- 소프트웨어 : OS, 미들웨어, 애플리케이션
- 네트워크 : 인터넷 연결, 서브넷, 보안 정책(인바운드/아웃바운드))

### 온프레미스

수동 인프라 관리
서버, 스토리지, 네트워크 장비 등 하드웨어를 직접 구매 & 설치 후 관리
애플리케이션 실행 환경 세팅, 운영, 장애 대응, 확장 모두 직접 수행

- 완벽한 시스템 제어
- 높은 초기 투자 비용(리소스 구매,설치), 확장성 제한(하드웨어 구매 & 설치에 시간 소요), 유지보수 부담(하드웨어 장애, 보안 패치 등 운영 업무 자체적 처리)

### 클라우드

웹에서 클릭, SDK 등으로 다양한 리소스 생성/삭제

- 필요에 따른 리소스 확장/축소
- 사용한 만큼 과금

=> 언제즈음 자동화가 필요해지는가

## 자동화의 필요성

기존 프로젝트를 직접 AWS 에서 구성했다고 해보자.
이를, 다시 처음부터 세팅하려면..?

1. 불필요한 시간과 노력이 소요가 된다.

추가로, 운영 환경과 동일한 조건에서 성능 테스트 할 수 있는 환경이 필요하다면??

-> 이런 세팅을 A to Z 손수 처리할 수 없게 된다.
Human Error 가 나올 수 밖에 없음

- 인프라 재현이 어려움
- 단순, 반복 작업인데 시간도 오래 걸림
- 휴먼 에러가 발생할 가능성이 생긴다.

### IaC

Infrastructure as Code, 인프라를 코드 처럼 관리한다.

1. 재현성 : 동일 코드로 동일 인프라 관리 가능
2. 일관성 : 코드와 실제 리소스 일치
3. 형상관리 : VCS 를 통한 변경 추적 관리, 코드 리뷰 통한 품질 관리, 롤백 & 복구
4. 자동화 : CI/CD 파이프라인과 통합 가능, 인프라 규모에 상관 없는 일정한 인프라 준비 속도
   일정한 속도가 큰 거 같다.. 이거, 여러 개를 한번에 구축하거나 VPC 등 네트워크 세팅 할 때 복잡함을 단순화 시켜준다

다양한 IaC 도구들이 존재한다고 한다.

AWS Cloud Formation, Azure Resource Manager Templates, Google Cloud Deployment Manager
Terraform(OpenTofu), Pulumi

## Terraform

https://developer.hashicorp.com/terraform

`infrastructure as code tool` ... in `human-readable configuration files` 라고 한다.

-> 사람이 읽기 쉬운 설정 파일로 리소스를 정의하는 IaC 도구

HCL 이라는 언어로 구성되어 있다.

block 과 argument 로 구성되어 있다.

```hcl
resource "aws_vpc" "vpc" {
	cidr_block = "10.0.0.0/16"
}
```

외부 객체가 block, 내부 객체가 argument

### 선언적

무엇을 원하는가에 집중
최종 상태만 코드로 정의하면, Terraform 이 알아서 필요한 작업 수행

![](https://i.imgur.com/pUiIQEJ.png)

의존성 순서 고려할 필요 없이, 자동으로 파악해서 생성해준다.

### 벤더 중립적

특정 벤더에 종속되지 않고, 여러 벤더의 서비스를 자유롭게 선택하여 사용 가능
EX)
컴퓨팅 서버는? AWS EC2
CDN 은? Cloudflare
모니터링은? Datadog

### 5,000 개 이상의 프로바이더 지원

> 정확히 어떤 의미일지.. 찾아봐야 함

- IaaS : AWS, GCP, Azure, OCI ...
- SaaS : Cloudflare, Datadog, Grafana ...
- 온프레미스 : VMware, Kubernetes ...

### Terraform 워크플로우

![600](https://i.imgur.com/khoXJ5r.png)

코드를 Write 하고
-> 실행 Plan 을 확인하고 ( DB 의 실행계획 와 유사 )
-> 실제 리소스를 Apply 한다

이런 Apply 단계를 Provisioning 이라고도 한다.

### Example

```tf
terraform {  
  required_version = ">= 1.6.0"  
  
  required_providers {  
    aws = {  
      source  = "hashicorp/aws"  
      version = "~> 5.0"  
    }  
  }  
}  
  
provider "aws" {  
  region = var.aws_region  
}

# S3 Playground Bucket  
resource "aws_s3_bucket" "playground" {  
  bucket = "${var.project_name}-s3-playground"  
  
  tags = {  
    Project = var.project_name  
    Env     = "playground"  
  }  
}
```

- provider : 사용할 프로바이더를 정의 - 제공해주는 요소
- resource : 실제 생성할 리소스 정의 - s3, cloudtrail, ec2 ...

```tf
variable "project_name" {  
  type        = string  
  description = "Prefix for all resources (must be globally unique for S3)"  
  default     = "youngsu-s3-metadata-playground"  
}  
  
variable "aws_region" {  
  type        = string  
  description = "AWS region"  
  default     = "ap-northeast-2"  
}
```

- variable : 같은 모듈 내 다른 파일에서 사용할 수 있는 변수 정의 - `var.project_name` 과 같이 사용 가능

![600](https://i.imgur.com/Ui1DkCA.png)

- backend : 상태 파일을 어디에 저장할지 결정

하지만, local 에 저장하면 혼자만 사용 가능하다.

![](https://i.imgur.com/fthj8mK.png)

S3 같은 요소를 사용해, 다른 사용자들과 공유해서 사용 가능하다.

![](https://i.imgur.com/qOkXZIz.png)

- module : 공통된 코드를 모듈로 만들어 재사용

적절히, instance type 만 다르게 적용 가능

![](https://i.imgur.com/6TWbFKh.png)

- output : 프로비저닝 된 리소스 정보를 외부로 전달, 다른 모듈에서 참조 가능

`module.network.vpc_id` 와 같이 참조 가능하다.

## Terraform 명령어

> 나는 tofu 로 진행했다.

### init

Backend 초기화 + 상태 파일 저장 경로 설정
프로바이더 플러그인 설치

![](https://i.imgur.com/vMJTxP5.png)

### plan

상태 파일과 코드를 비교해 실행 계획 생성

- created : 새로 생성될 리소스
- updated in-place : 일부 속성만 변경된 리소스
- destroyed : 삭제될 리소스
- replaced : 재생성 될 리소스 (생성은 +1, 삭제는 -1)

### apply

실제 생성하는 명령어

![](https://i.imgur.com/WeHIaUv.png)

매번 요청에 대해 yes 쳐야하는데, 귀찮다면 `-auto-approve` 를 통해 생략 가능

### destory

일괄 정리할 수 있다.

![](https://i.imgur.com/9g53bIz.png)

## Terraform 과 함께 사용하면 좋은 도구

### Terraformer

이미 존재하는 인프라를 스캔해서 tf, tfstate 파일 생성

```tf
terraformer import aws \
	--regions=ap-northeast-2 \
	--resources=vpc,subnet,ec2-instance
```

서울 리전 VPC, 서브넷, EC2 스캔

### Infracost

코드에 정의된 인프라의 총 예상 비용 계산, 코드 변경 따른 비용 변동 분석

![](https://i.imgur.com/Is9Amyp.png)

> 오 신기하당

---

그게 진짜 좋은거 같다...

직접 구성하려면, VPC 나 보안 정책 등에서 꼬일수 있는데
이걸 어느정도 패턴에 인지하면 능숙하게 처리할 수 있는점
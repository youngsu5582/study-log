## ECR

Elastic Container Registry
AWS 에서 제공해주는 일종의 Docker Hub

- S3 로 Docker Image 를 관리하거나,
- AWS IAM 인증을 통해 이미지 pull / push 권한 관리가 가능하거나
- ECS, EKS 에서 사용 가능하게 해준다거나

AWS 의 서비스들을 완벽히 누릴 수 있다.

프라이빗, 퍼블릭으로 레포지토리를 생성 가능하다.

- 프라이빗 : IAM 정책 및 레포지토리 권한이 있는 유저만 접근 가능
- 퍼블릭 : ECR 갤러리에 공개되어 누구나 접근 및 pull 가능

추가로, 이미지 스캐닝을 제공해준다.

### github action 으로 처리하는 방법은?

```yml
- name: Login to Amazon ECR  
  id: login-ecr  
  uses: aws-actions/amazon-ecr-login@v2
  
- name: Set up Docker Buildx  
  uses: docker/setup-buildx-action@v3
  
- name: Build, tag, and push docker image to Amazon ECR  
  env:  
    REGISTRY: ${{ steps.login-ecr.outputs.registry }}  
    REPOSITORY: ${{ inputs.ECR_REPO_NAME }}  
    IMAGE_TAG: ${{ inputs.VERSION }}  
    APP_OPTS: ${{ inputs.APP_OPTS }}  
  run: |  
    docker buildx build --platform linux/arm64,linux/amd64 --push -t $REGISTRY/$REPOSITORY:$IMAGE_TAG -t $REGISTRY/$REPOSITORY:latest .
```

- `.` 을 통해 현재 폴더에서 Dockerfile 을 찾아서 빌드를 한다.

- build 하면서 push 옵션을 통해 바로 push 한다.

> buildx 는 linux/arm64 와 같은 멀티 플랫폼으로 빌드 가능하게 해줌

자기 로컬에서 올라간 이미지를 확인하려면
`aws ecr get-login-password --profile mfa --region ap-northeast-2 | docker login --username AWS --password-stdin {URI}`


`aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin {계정 ID}.dkr.ecr.ap-northeast-2.amazonaws.com`

와 같이 로그인을 하고

`docker pull {계정 ID}.dkr.ecr.ap-northeast-2.amazonaws.com/{REPOSITORY}:{IMAGE_TAG}`

pull 을 통해 이미지를 확인하면 된다.

### 정책

- 수명 주기 정책

빌드 및 배포를 통해 이미지들이 쌓일 수 있는데 이 역시도 수명 주기 정책을 통해 깔끔하게 관리해준다.
EX) 오래된 이미지 제거 - 개수 제한에 따른 일정 개수만 유지

- 태그 불변성

그리고, latest 를 제외한 태그는 태그 불변을 보장해준다.
-> 실수로라도, 동일 태그에 두번 push 를 하는걸 방지

특정 이미지 태그는 항상 특정 빌드 결과물을 가르키게 하는걸 보장해준다. ( 신뢰성 및 추적 가능성 )

- 리전 간 복제

주 사용 리전 ( 서울 ) 에 푸시하면, 설정된 다른 리전에도 자동으로 이미지가 복제되게 할 수 있다.
-> 한 리전에 문제가 생겨도 다른 리전에선 사용 가능 + 해외 리전에서 pull 할 때도 지연 시간이 줄어든다.

### 비용

비용 역시 나쁘지 않다.

- 저장 비용 : ECR 에 저장된 이미지의 총 용량에 대해 월별 청구
- 데이터 전송 비용 : ECR push 비용은 무료, 동일 리전 AWS 서비스 (ECS,EKS) 가 pull 하는 비용도 무료이다.
  ( 다른 리전, 인터넷으로 데이터 전송 시에는 AWS 데이터 전송 요금이 부과 )
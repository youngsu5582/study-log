# CompletableFuture

## Future 단점과 한계

- 외부에서 완시킬 수 없고, get 의 타임아웃으로만 설정 가능
- Blocking 코드 통해서만, 이후 결과 처리 가능
- 여러개 Future 조합 및 병렬 처리 불가능
- 여러 작업을 조합하거나 예외 처리할 수 없음

-> 이를 해결하기 위해 `CompletableFuture`가 Java 8에 나왔다.

## CompletableFuture

`Future`,`CompleteStage` 인터페이스를 구현한다.

- Future : 비동기 연산 위한 인터페이스
- CompleteStage : 여러 연산 결합하도록 연산 완료되면 다음 단계 작업 수행하거나 값 연산하는 `비동기식 연산 단계` 제공 인터페이스
  ( 하단, thenCombine 같은 요소들 전부 포함하고 있는 인터페이스 )

### 비동기 작업 실행

- runAsync : 반환값 없는 경우, 비동기로 작업 실행
- supplyAsync : 반환값 있는 경우, 비동기로 작업 실행

### 작업 콜백

- thenApply : 반환 값을 받아서 다른 값 반환함
- thenAccept : 반환 값을 받아서 처리, 다른 값 반환하지 않음 
- thenRun : 반환 값 받지 않고, 다른 작업 실행

함수형 인터페이스들을 받아서 처리한다.

### 작업 조합

- thenCompose : 두 작업 이어서 실행, 앞선 작업 결 받아서 사용
먼저 실행된 후, 반환된 값 매개변수로 받아서 실행
- thenCombine : 두 작업 독립적 실행, 둘 다 완료시 콜백 실행 
각각 작업 독립적 실행후, 결과 조합해 사용
- allOf : 여러 작업 동시에 실행, 모든 작업 결과에 콜백 실행
- anyOf : 여러 작업중 가장 빨리 끝난 하나의 결과에 콜백 실행

### 예외 처리

- exceptionally : 발생한 에러 받아서 예외 처리
- handle, handleAsync : (결과,에러) 받아서 에러 발생 경우와 아닌 경우 모두 처리 가능

---

사용 코드

```java
private GithubPullRequestReviewInfo getGithubPullRequestReviewInfo(String prLink) {
    CompletableFuture<List<GithubPullRequestReview>> reviewFuture = supplyAsync(() -> reviewClient.getPullRequestReviews(prLink), apiExecutor);
    CompletableFuture<List<GithubPullRequestReview>> commentFuture = supplyAsync(() -> commentClient.getPullRequestReviews(prLink), apiExecutor);

    return reviewFuture
            .thenCombine(commentFuture, this::collectPullRequestReviews)
            .exceptionally(e -> {
                log.warn("[리뷰 API 중 깃허브 에러 발생] 발생 링크 : {}, 에러 : {}", prLink, e.getStackTrace());
                throw new CoreaException(ExceptionType.GITHUB_SERVER_ERROR);
            })
            .thenApply(GithubPullRequestReviewInfo::new)
            .join();
}

private Map<String, GithubPullRequestReview> collectPullRequestReviews(List<GithubPullRequestReview> reviews, List<GithubPullRequestReview> comments) {
    return collectByGithubUserId(Stream.concat(reviews.stream(), comments.stream()));
}
```

thenCombine 을 통해 결합후 변환
thenApply 를 통해 새로운 객체 생성
join 으로 최종 결과 반환까지 블로킹

# FirstSuccessfulFuture

특히 여러 `CompletableFuture` 중 하나라도 완료되기를 기다리는 `anyOf` 메소드도 '성공'과 '실패(예외)'를 구분하지 않고, 단순히 '가장 먼저 끝난' 작업을 기준으로 전체 결과를 결정한다.

-> 이로 인해 첫 번째로 완료된 작업이 예외를 던지면, 다른 작업들이 성공할 가능성이 있음에도 불구하고 전체 `CompletableFuture`가 즉시 실패 처리되는 문제가 발생한다.

### `anyOf` 의 문제점

- **시나리오**: `f1`은 1초 후 실패, `f2`는 2초 후 성공.
- **`anyOf(f1, f2)`의 동작**: 1초 후 `f1`이 예외를 던지면, `anyOf`는 `f2`의 결과를 기다리지 않고 즉시 `f1`의 예외와 함께 완료됩니다.
- **문제**: 우리가 원했던 것은 2초가 걸리더라도 `f2`의 성공적인 결과를 얻는 것이었지만, `anyOf`는 이를 지원하지 않습니다.

이러한 문제를 해결하기 위해, 두 Future 중 하나가 성공할 때까지 기다리고, 둘 다 실패했을 때만 최종 실패를 반환하는 커스텀 클래스를 만들어 처리해야 한다.

```java
@Getter
private static class FirstSuccessCompletableFuture<T> {
    private final CompletableFuture<T> promise = new CompletableFuture<>();

    FirstSuccessCompletableFuture(CompletableFuture<T> f1, CompletableFuture<T> f2) {
        // 각 Future에 완료 핸들러를 등록합니다.
        f1.whenComplete((res, ex) -> handleCompletion(f2, res, ex));
        f2.whenComplete((res, ex) -> handleCompletion(f1, res, ex));
    }

    private void handleCompletion(CompletableFuture<T> other, T result, Throwable ex) {
        if (ex == null) {
            // 성공 시: 메인 Promise를 성공으로 완료시키고, 다른 Future를 취소합니다.
            // promise.complete()는 단 한 번만 성공하므로, 경주에서 이긴 쪽만 이 블록을 실행합니다.
            if (promise.complete(result)) {
                other.cancel(true);
            }
        } else {
            // 실패 시: 다른 Future의 결과를 기다립니다.
            other.whenComplete((otherResult, otherEx) -> {
                if (otherEx == null) {
                    // 다른 Future가 성공하면, 그 결과로 메인 Promise를 완료합니다.
                    promise.complete(otherResult);
                } else {
                    // 다른 Future마저 실패하면, 두 예외를 모두 포함하여 최종 실패 처리합니다.
                    ex.addSuppressed(otherEx);
                    promise.completeExceptionally(ex);
                }
            });
        }
    }
}
```

### 코드 동작 원리

1.  **생성자**:
    - 두 개의 `CompletableFuture`(`f1`, `f2`)를 입력
    - `whenComplete`를 사용하여 `f1`과 `f2` 각각에 완료 핸들러(`handleCompletion`)를 등록. (어느 쪽이 먼저 끝나든 `handleCompletion`이 호출)

2.  **`handleCompletion` 메소드 (핵심 로직)**:
    - **성공 경로 (`ex == null`)** (완료된 Future가 성공한 경우)
        - `promise.complete(result)`를 호출하여 최종 결과를 설정하려고 시도
        - `CompletableFuture.complete()` 메소드는 **오직 한 번만 성공적으로 호출될 수 있으며**, 성공 시 `true`를 반환
        - 만약 `promise.complete()`가 `true`를 반환했다면, 더 이상 실행될 필요가 없는 `other` Future를 `cancel(true)`를 통해 즉시 중단시켜 리소스를 절약
ㄴ
    - **실패 경로 (`ex != null`)** (먼저 완료된 Future가 실패한 경우)
        - 여기서 바로 `promise`를 실패 처리하지 않고, `other.whenComplete(...)`를 통해 다른 Future의 결과를 기다리는 새로운 핸들러를 등록
        - **만약 다른 Future가 성공하면**, 그 결과(`otherResult`)로 `promise`를 성공적으로 완료
        - **만약 다른 Future마저 실패하면**, 두 개의 예외를 `ex.addSuppressed(otherEx)`로 합친 뒤, `promise`를 최종적으로 예외 처리.

이렇게 하면 어떤 원인들로 실패했는지 모두 추적가능

```java
return client.getObject(GetObjectRequest
                .builder()
                .key(key)
                .bucket(bucketName)
                .build(), AsyncResponseTransformer.toBytes())
        .thenApply(ResponseBytes::asByteArray)
        .whenComplete((res, ex) -> {
            if (ex == null) {
                log.info("S3에서 업로드된 파일을 가져왔습니다. region: {}, bucket: {}, key: {}", region, bucketName, key);
            }
        });
```

이와같이 비동기 체이닝에서 로그를 찍으면 되기 때문에 클래스나 정보를 몰라도 상관 없다.


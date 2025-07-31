
Async Client 는 Reactive Streams 를 받아들여 Non-blocking 지원

### 기존 동기 처리의 문제점

- 요청 당 스레드 하나 ( thread per request ) : 요청 처리가 끝날 때까지 하나의 스레드가 작업에 묶여 있었음 ( 스레드가 서버 자원 점유 )
- 동시성 한계 : 동시 1000개 요청 오면, 1000개 스레드 필요. 서버 부하 유발 및 시스템 다운 가능

![](https://i.imgur.com/f5YxfGo.png)

> DoS 공격에도 취약 ( 요청이 많이 들어오면, 스레드가 전부 사용된다. )

### 비동기

- 더 적은 스레드로 더 많은 클라이언트 요청을 효율적으로 처리 가능 - 이벤트 루프가 수많은 요청 처리
- 불필요한 컨텍스트 스위칭 절약 ( 백그라운드에서 데이터가 준비되면, 이벤트를 발생시켜 유후 스레드가 잠시동안 차후 로직 처리 )
- Netty 기반으로 동작

![](https://i.imgur.com/PCizaiA.png)


> 여전히 자격증명 가져오는 등의 작업은 동기로 돌 수 있다.

## 데이터 처리 방식

`AsyncResponseTransformer` 를 통해 들어오는 스트림 응답을 다른 형태로 매핑

- CompletableFuture 를 반환 ( Mono 로 감싸서 사용 역시 가능 )

```java
private CompletableFuture<byte[]> getUploadedImageFromKey(Region region, String bucketName, String key) {  
    var client = s3Factory.getAsyncClient(region);  
    var startTime = timeProvider.currentTimeMillis();  
    return client.getObject(GetObjectRequest  
                    .builder()  
                    .key(key)  
                    .bucket(bucketName)  
                    .build(), AsyncResponseTransformer.toBytes())  
            .thenApply(ResponseBytes::asByteArray)  
            .whenComplete((res, ex) -> {  
                if (ex == null) {  
                    log.info("S3 에서 이미지 접근 성공(소요 시간: {}ms) region: {}, bucket: {}, key: {}",  
                            timeProvider.currentTimeMillis() - startTime, region, bucketName, key);  
                }  
            });  
}
```

버퍼링을 사용하는 방식

- 대용량 파일 다운로드 시엔 절대적으로 금지 ( OOM 유발 가능 )

-> 파일 크기가 매우 작은게 보장되면 사용은 가능

```java
private Flux<ByteBuffer> getUploadedImageStreamFromKey(Region region, String bucketName, String key) {  
    var client = s3Factory.getAsyncClient(region);  
    var startTime = timeProvider.currentTimeMillis();  
  
    var request = GetObjectRequest.builder().key(key).bucket(bucketName).build();  
  
    return Mono.fromFuture(client.getObject(request, AsyncResponseTransformer.toPublisher()))  
            .doOnSuccess(response -> log.info("S3에서 이미지 스트리밍 시작(소요 시간: {}ms) region: {}, bucket: {}, key: {}",  
                    timeProvider.currentTimeMillis() - startTime, region, bucketName, key))  
            .flatMapMany(Flux::from);  
}
```

왠만하면 이런 스트리밍 방식을 사용해야 한다.

- 배압(Backpressure) : 데이터를 소비하는 속도에 맞게 조절한다. ( S3 가 100MB/s, 클라이언트가 1MB/s 일 시, 클라이언트에 맞게 속도 조절 )
- 흐름 : 단순히 연결만 하며, 서버가 최종 목적지가 아닌 거쳐 가는 통로

## 에러 처리

```java
// 해당 파일 없음
.onErrorResume(NoSuchKeyException.class, e -> {
    log.warn("File not found in S3. Key: {}", filekey);
    return Mono.just(ResponseEntity.notFound().build());
})
// 그 외 S3 관련 예외 처리
.onErrorResume(S3Exception.class, e -> {
    // 403 권한 오류
    if (e.statusCode() == 403) {
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
    // 5xx S3 서버 오류는 일시적일 수 있음
    log.error("S3 service error while getting key: {}", filekey, e);
    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
})
// 네트워크 관련 예외 처리
.onErrorResume(SdkClientException.class, e -> {
    log.error("SDK/Network error while downloading from S3. Key: {}", filekey, e);
    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
});
```


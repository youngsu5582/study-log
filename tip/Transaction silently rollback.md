# Transaction sliently rollback

```
Transaction silently rolled back because it has been marked as rollback-only
```

이와 같은 에러가 발생하는 경우를 의미한다.

직역하면, 트랜잭션이 롤백 전용 표시 되었으므로, 조용히 롤백 되었다는 뜻이다.

그러면, 코드를 살펴보자.

```java
@Async(AsyncProperties.ASYNC_LISTENER)
@Transactional(readOnly = true)
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, value = SomethingCreatedEvent.class)
public void evictCursor() {
    try {
        somethingCacheService.evictCache();
    } catch (Exception ignore) {
        log.error("캐시 초기화중 오류 발생했는데 위로 전파는 안함", ignore);
    }
}
```

무언가가 생성이 되고 커밋이 된 후에는 캐시를 비우는 이벤트 리스너가 있다.

```java
public void evictCache() {
    var cache = cacheManager.getCache(getCacheKey());
    if (cache != null) {
        cache.clear();
    }
}
```
내부 로직에선 캐시를 비우는 로직이 있다.
하지만, 이때 cacheManager 가 무언가 예외를 던진다면?
( 직렬화 예외, 캐시 서버 다운, 캐시키 부적합 등등등 )

예외가 던져졌으나, catch 를 통해 무시되고 있으니 상관없는거 아닌가 생각할 수 있다.

그렇지 않다.

트랜잭션의 내부에서 예외가 발생해서 트랜잭션 관리자는 `rollback-only` 상태로 표시한다.
예외를 외부에서 잡더라도 이미 마킹이 되어있다.

프록시가 트랜잭션을 커밋하려고 할 때, 표시를 보고 코드 실행은 정상적으로 끝났지만 롤백 표시가 되어있다는 띠용한 상황을 마주한다.

- Transaction 어노테이션을 사용하지 않는다면 제거하자.
- 또는, `NOT_SUPPORTED` 로 해당 로직에선 트랜잭션을 사용하지 않는걸 보장하자.
- 시원하게 로그 찍고, 그대로 예외 전파를 진행하자.

트랜잭션 내부에서 하위 클래스 메소드에서 예외가 발생 하는걸 잡을떄 주의하자!
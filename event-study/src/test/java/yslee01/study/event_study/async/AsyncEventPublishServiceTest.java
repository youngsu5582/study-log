package yslee01.study.event_study.async;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import yslee01.study.event_study.ThreadInfo;

@SpringBootTest
@Import(AsyncConfiguration.class)
class AsyncEventPublishServiceTest {

    @Autowired
    private AsyncEventPublishService asyncEventPublishService;

    @Autowired
    private AsyncPhaseRecorder asyncPhaseRecorder;

    @Test
    void async_case() throws InterruptedException {
        asyncEventPublishService.publishAsyncEvent("order-id");

        Thread.sleep(10);

        // taskExecutor 라는 이름을 가진 ThreadPoolTaskExecutor 없으면 SimpleAsyncTaskExecutor 를 사용
        // Async 처리를 하지 않으면, 호출한 스레드에서 메소드가 그대로 실행된다. - Test worker
        // 3개의 순서는 언제든 바뀔수 있다.
        assertThat(asyncPhaseRecorder.phases().stream().map(ThreadInfo::threadName))
            .contains("Test worker", "taskExecutor2-1", "SimpleAsyncTaskExecutor-1");

    }
}
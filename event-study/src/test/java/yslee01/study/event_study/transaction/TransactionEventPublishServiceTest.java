package yslee01.study.event_study.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.event.TransactionPhase;

@SpringBootTest
class TransactionEventPublishServiceTest {

    @Autowired
    TransactionEventPublishService service;
    @Autowired
    TransactionPhaseRecorder recorder;

    @BeforeEach
    void setUp() {
        recorder.clear();
    }

    @Test
    @DisplayName("커밋 성공 시: BEFORE_COMMIT → AFTER_COMMIT → AFTER_COMPLETION 순으로 호출")
    // 이때, 순서를 지정하지 않으면 간혈적으로 COMPLETION -> AFTER_COMMIT 으로 호출될 수 있다.
    // => 순서에 의존되지 않는 이벤트 구성을 해야한다.
    void whenCommit_thenBeforeAndAfterCommitAndAfterCompletion() {
        service.publishAndSucceed("x");
        // 서비스 메서드 종료 시점에 트랜잭션이 커밋되고, 리스너가 호출됨
        assertThat(recorder.getPhases()).containsExactly(
            TransactionPhase.BEFORE_COMMIT,
            TransactionPhase.AFTER_COMMIT,
            TransactionPhase.AFTER_COMPLETION
        );
    }

    @Test
    @DisplayName("롤백 시: AFTER_ROLLBACK → AFTER_COMPLETION 순으로 호출")
    // 왜 인지, ROLLBACK -> COMPLETION 은 발생하지 않는다.
    void whenRollback_thenAfterRollbackAndAfterCompletion() {
        assertThrows(RuntimeException.class, () -> service.publishAndFail("y"));
        // 예외로 인해 롤백된 이후 리스너 호출
        assertThat(recorder.getPhases()).containsExactly(
            TransactionPhase.AFTER_ROLLBACK,
            TransactionPhase.AFTER_COMPLETION
        );
    }
}

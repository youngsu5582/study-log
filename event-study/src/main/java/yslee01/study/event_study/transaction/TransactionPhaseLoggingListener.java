package yslee01.study.event_study.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import yslee01.study.event_study.TestEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionPhaseLoggingListener {

    private final TransactionPhaseRecorder recorder;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onBeforeCommit(TestEvent e) {
        log("onBeforeCommit");
        recorder.record(TransactionPhase.BEFORE_COMMIT);
        waitTime(10);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAfterCommit(TestEvent e) {
        log("onAfterCommit");
        recorder.record(TransactionPhase.AFTER_COMMIT);
        waitTime(10);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onAfterRollback(TestEvent e) {
        log("onAfterRollback");
        recorder.record(TransactionPhase.AFTER_ROLLBACK);
        waitTime(10);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void onAfterCompletion(TestEvent e) {
        log("onAfterCompletion");
        recorder.record(TransactionPhase.AFTER_COMPLETION);
        waitTime(10);
    }

    private void log(String message) {
        log.info("[Thread:{}] {}", Thread.currentThread().getName(), message);
    }

    private void waitTime(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted", e);
        }
    }
}

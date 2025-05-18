package yslee01.study.event_study;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import yslee01.study.event_study.transaction.TransactionPublishedEvent;

@Slf4j
@Component
public class PublishedEventListener {

    @EventListener
    public void handleEvent(PublishedEvent event) {
        // Handle the event
        log.info("handle event {}", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    @Async
    public void handleTransactionalEvent(TransactionPublishedEvent event) {
        log.info("[Thread:{}]handle transactional event {}", Thread.currentThread().getName(),event);
    }
}

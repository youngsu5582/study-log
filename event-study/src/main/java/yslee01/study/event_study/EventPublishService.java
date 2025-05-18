package yslee01.study.event_study;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yslee01.study.event_study.transaction.TransactionPublishedEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublishService {

    private final ApplicationEventPublisher publisher;

    public void publishEvent(String id) {
        // Logic to publish the event
        log.info("publish event {}", id);

        publish(id);
    }

    @Transactional
    public void publishTransactionalEvent(String id) {
        // Logic to publish the event
        log.info("publish transactional event {}", id);

        publishTransactional(id);
    }

    private void publish(String id) {
        publisher.publishEvent(new PublishedEvent(id));
    }

    private void publishTransactional(String id) {
        publisher.publishEvent(new TransactionPublishedEvent(id));
    }
}

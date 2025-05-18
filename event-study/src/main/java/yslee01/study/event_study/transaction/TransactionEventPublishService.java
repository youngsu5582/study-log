package yslee01.study.event_study.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yslee01.study.event_study.TestEvent;

@Service
@RequiredArgsConstructor
public class TransactionEventPublishService {

    private final ApplicationEventPublisher publisher;

    @Transactional
    public void publishAndSucceed(String id) {
        publisher.publishEvent(new TestEvent(id));
    }

    @Transactional
    public void publishAndFail(String id) {
        publisher.publishEvent(new TestEvent(id));
        throw new RuntimeException("fail");
    }
}

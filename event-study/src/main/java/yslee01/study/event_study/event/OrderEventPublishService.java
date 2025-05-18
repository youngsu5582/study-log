package yslee01.study.event_study.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventPublishService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishOrderEvent(String orderId) {
        OrderEvent event = new OrderEvent(orderId);
        applicationEventPublisher.publishEvent(event);
    }
}

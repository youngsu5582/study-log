package yslee01.study.event_study.async;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncEventPublishService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishAsyncEvent(String orderId) {
        AsyncEvent event = new AsyncEvent(orderId);
        applicationEventPublisher.publishEvent(event);
    }
}
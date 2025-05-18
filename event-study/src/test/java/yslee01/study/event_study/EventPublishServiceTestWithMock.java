package yslee01.study.event_study;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

@SpringBootTest
class EventPublishServiceTestWithMock {

    @Mock
    ApplicationEventPublisher publisher;

    @InjectMocks
    EventPublishService service;

    @Test
    void publishEvent_invokesPublisher() {
        service.publishEvent("id");
        verify(publisher, times(1)).publishEvent(
            new PublishedEvent("id")
        );
    }
}
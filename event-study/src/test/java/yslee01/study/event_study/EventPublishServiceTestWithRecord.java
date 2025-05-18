package yslee01.study.event_study;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

// 테스트 실행 중 ApplicationContext 에서 발행된 모든 이벤트를 기록
@RecordApplicationEvents
@SpringBootTest
public class EventPublishServiceTestWithRecord {

    @BeforeEach
    void setUp() {
        // 테스트 실행 전 ApplicationEvents 초기화
        applicationEvents.clear();
    }
    @Autowired
    EventPublishService service;

    @Autowired
    ApplicationEvents applicationEvents;

    @Test
    void publishEvent_invokesPublisher() {
        service.publishEvent("debug-id");
        assertThat(applicationEvents.stream(PublishedEvent.class).count()).isOne();
    }
}

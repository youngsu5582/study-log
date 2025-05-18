package yslee01.study.event_study.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderEventPublishServiceTest {

    @Autowired
    private OrderEventPublishService orderEventPublishService;

    @Autowired
    EventPhaseRecorder eventPhaseRecorder;

    @Test
    // 순서를 지정할 때는 EventListener 어노테이션이 있는 메소드에 지정해야 의도대로 동작한다.
    void order_test(){
        orderEventPublishService.publishOrderEvent("order");
        assertThat(eventPhaseRecorder.getPhases())
            .containsExactly("OrderBEventListener","OrderCEventListener","OrderAEventListener");
    }
}
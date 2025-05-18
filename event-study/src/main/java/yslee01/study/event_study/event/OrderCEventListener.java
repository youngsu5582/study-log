package yslee01.study.event_study.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCEventListener {

    private final EventPhaseRecorder eventPhaseRecorder;

    @EventListener
    @Order(2)
    public void onOrderEvent(OrderEvent orderEvent) {
        eventPhaseRecorder.record("OrderCEventListener");
        System.out.println("OrderCEventListener: " + orderEvent.id());
    }
}

package yslee01.study.event_study.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

public class OrderBEventListener {

    private final EventPhaseRecorder eventPhaseRecorder;

    @EventListener
    @Order(1)
    public void onOrderEvent(OrderEvent orderEvent) {
        eventPhaseRecorder.record("OrderBEventListener");
        System.out.println("OrderBEventListener: " + orderEvent.id());
    }
}

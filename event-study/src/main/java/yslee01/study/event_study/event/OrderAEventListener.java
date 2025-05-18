package yslee01.study.event_study.event;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderAEventListener {

    private final EventPhaseRecorder eventPhaseRecorder;

    public OrderAEventListener(EventPhaseRecorder eventPhaseRecorder) {
        this.eventPhaseRecorder = eventPhaseRecorder;
    }

    @EventListener
    @Order(3)
    public void onOrderEvent(OrderEvent orderEvent) {
        eventPhaseRecorder.record("OrderAEventListener");
        System.out.println("OrderAEventListener: " + orderEvent.id());
    }
}

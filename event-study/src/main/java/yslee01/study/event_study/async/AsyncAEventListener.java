package yslee01.study.event_study.async;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import yslee01.study.event_study.EventListener;

@Component
@RequiredArgsConstructor
public class AsyncAEventListener implements EventListener<AsyncPhaseRecorder, AsyncEvent> {

    private final AsyncPhaseRecorder asyncPhaseRecorder;

    @Override
    public AsyncPhaseRecorder getRecorder() {
        return asyncPhaseRecorder;
    }

    @Override
    @Async
    public void onOrderEvent(AsyncEvent event) {
        EventListener.super.onOrderEvent(event);
    }
}

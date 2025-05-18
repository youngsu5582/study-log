package yslee01.study.event_study.transaction;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;


// 호출된 phase를 기록하는 Bean
@Component
public class TransactionPhaseRecorder {
    private final List<TransactionPhase> phases = new ArrayList<>();

    public synchronized void record(TransactionPhase phase) {
        phases.add(phase);
    }

    public List<TransactionPhase> getPhases() {
        return List.copyOf(phases);
    }

    public void clear() {
        phases.clear();
    }
}

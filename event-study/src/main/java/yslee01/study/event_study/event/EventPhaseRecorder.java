package yslee01.study.event_study.event;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import yslee01.study.event_study.PhaseRecorder;

@Component
public class EventPhaseRecorder implements PhaseRecorder<String> {

    private final List<String> phases = new ArrayList<>();

    @Override
    public List<String> phases() {
        return phases;
    }
}

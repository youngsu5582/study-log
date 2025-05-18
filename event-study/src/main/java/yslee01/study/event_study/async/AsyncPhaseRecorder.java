package yslee01.study.event_study.async;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import yslee01.study.event_study.PhaseRecorder;
import yslee01.study.event_study.ThreadInfo;

@Component
public class AsyncPhaseRecorder implements PhaseRecorder<ThreadInfo> {

    private final List<ThreadInfo> phases = new ArrayList<>();

    @Override
    public List<ThreadInfo> phases() {
        return phases;
    }
}

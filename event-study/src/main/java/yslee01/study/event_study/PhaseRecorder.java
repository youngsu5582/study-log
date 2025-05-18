package yslee01.study.event_study;

import java.util.List;

public interface PhaseRecorder<T> {

    List<T> phases();

    default void record(T event) {
        synchronized (this) {
            phases().add(event);
        }
    }

    default List<T> getPhases() {
        return List.copyOf(phases());
    }

    default void clear() {
        phases().clear();
    }
}

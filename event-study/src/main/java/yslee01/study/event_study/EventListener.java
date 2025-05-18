package yslee01.study.event_study;

public interface EventListener<T extends PhaseRecorder, V> {

    T getRecorder();

    @org.springframework.context.event.EventListener
    default void onOrderEvent(V event) {
        getRecorder().record(new ThreadInfo(Thread.currentThread().getName(), this.getClass().getSimpleName(), event));
        System.out.println("Thread[%s] Class[%s] Event[%s]".formatted(
            Thread.currentThread().getName(),
            this.getClass().getSimpleName(),
            event
        ));
    }
}

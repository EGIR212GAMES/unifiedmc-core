package dev.unifiedmc.core;

/** Receives lifecycle events without imposing a logging framework. */
@FunctionalInterface
public interface LifecycleEventSink {
    void publish(LifecycleEvent event);
}

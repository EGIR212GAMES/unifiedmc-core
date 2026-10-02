package dev.unifiedmc.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory lifecycle event collector used by diagnostics and tests. */
public final class LifecycleEventCollector implements LifecycleEventSink {
    private final CopyOnWriteArrayList<LifecycleEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public void publish(LifecycleEvent event) {
        events.add(event);
    }

    public List<LifecycleEvent> events() {
        return List.copyOf(events);
    }
}

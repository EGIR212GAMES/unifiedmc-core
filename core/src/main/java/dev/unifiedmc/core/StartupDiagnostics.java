package dev.unifiedmc.core;

import java.util.List;

/** Snapshot of lifecycle events and the terminal failure, if any. */
public record StartupDiagnostics(List<LifecycleEvent> events, StartupFailure failure) {
    public StartupDiagnostics {
        events = List.copyOf(events);
    }
}

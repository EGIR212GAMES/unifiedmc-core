package dev.unifiedmc.core;

import java.util.concurrent.atomic.AtomicBoolean;

/** Installs a JVM shutdown hook and coordinates a single graceful stop request. */
public final class ShutdownCoordinator implements AutoCloseable {
    private final Runnable shutdownAction;
    private final AtomicBoolean requested = new AtomicBoolean();
    private final Thread hook;

    public ShutdownCoordinator(Runnable shutdownAction) {
        this.shutdownAction = shutdownAction;
        this.hook = new Thread(this::request, "unifiedmc-shutdown-hook");
    }

    public void install() {
        Runtime.getRuntime().addShutdownHook(hook);
    }

    public void request() {
        if (requested.compareAndSet(false, true)) {
            shutdownAction.run();
        }
    }

    @Override
    public void close() {
        if (requested.compareAndSet(false, true)) {
            try {
                Runtime.getRuntime().removeShutdownHook(hook);
            } catch (IllegalStateException ignored) {
                // JVM shutdown is already in progress.
            }
        }
    }
}

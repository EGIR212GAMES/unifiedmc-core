package dev.unifiedmc.runtime;

import java.time.Duration;

/** Control handle for a running isolated runtime. */
public interface RuntimeHandle extends AutoCloseable {
    RuntimeProcess process();

    RuntimeHealth health();

    void stop();

    void stop(Duration timeout);

    void destroyForcibly();

    boolean await(Duration timeout) throws InterruptedException;

    @Override
    default void close() {
        stop();
    }
}

package dev.unifiedmc.runtime;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;

/** Runtime-neutral view of an isolated operating-system process. */
public interface RuntimeProcess {
    long pid();

    boolean isAlive();

    Instant startedAt();

    OptionalInt exitCode();

    List<String> stdoutTail();

    List<String> stderrTail();

    boolean await(Duration timeout) throws InterruptedException;
}

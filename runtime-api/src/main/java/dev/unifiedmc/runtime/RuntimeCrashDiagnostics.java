package dev.unifiedmc.runtime;

import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;

/** Evidence captured after a failed runtime process. */
public record RuntimeCrashDiagnostics(
        OptionalInt exitCode,
        Instant startedAt,
        Instant finishedAt,
        List<String> stdoutTail,
        List<String> stderrTail) {
    public RuntimeCrashDiagnostics {
        stdoutTail = List.copyOf(stdoutTail);
        stderrTail = List.copyOf(stderrTail);
    }
}

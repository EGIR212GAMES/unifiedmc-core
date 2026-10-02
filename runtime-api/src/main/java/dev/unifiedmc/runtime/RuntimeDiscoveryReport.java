package dev.unifiedmc.runtime;

import java.util.List;

/** Result of scanning the managed runtime registry. */
public record RuntimeDiscoveryReport(List<MinecraftRuntime> runtimes, List<String> diagnostics) {
    public RuntimeDiscoveryReport {
        runtimes = List.copyOf(runtimes);
        diagnostics = List.copyOf(diagnostics);
    }
}

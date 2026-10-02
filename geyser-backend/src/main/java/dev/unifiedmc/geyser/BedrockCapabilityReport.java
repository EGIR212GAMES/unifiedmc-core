package dev.unifiedmc.geyser;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Capability matrix for one content compilation. */
public record BedrockCapabilityReport(
        Map<BedrockCapability, BedrockCapabilityStatus> capabilities,
        List<BedrockDiagnostic> diagnostics) {
    public BedrockCapabilityReport {
        capabilities = Map.copyOf(new EnumMap<>(capabilities));
        diagnostics = List.copyOf(diagnostics);
    }

    public BedrockCapabilityStatus status(BedrockCapability capability) {
        return capabilities.getOrDefault(capability, BedrockCapabilityStatus.UNSUPPORTED);
    }

    public boolean isPartial() {
        return capabilities.values().stream().anyMatch(s -> s == BedrockCapabilityStatus.PARTIAL);
    }

    public boolean isUnsupported() {
        return capabilities.values().stream()
                .anyMatch(s -> s == BedrockCapabilityStatus.UNSUPPORTED);
    }
}

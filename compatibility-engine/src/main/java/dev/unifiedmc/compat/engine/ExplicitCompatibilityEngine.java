package dev.unifiedmc.compat.engine;

import dev.unifiedmc.compat.CompatibilityEngine;
import dev.unifiedmc.compat.CompatibilityLevel;
import dev.unifiedmc.compat.CompatibilityRequest;
import dev.unifiedmc.compat.CompatibilityResult;
import java.util.List;

/** Safe default that reports unsupported decisions until a concrete adapter is selected. */
public final class ExplicitCompatibilityEngine implements CompatibilityEngine {
    @Override
    public CompatibilityResult evaluate(CompatibilityRequest request) {
        return new CompatibilityResult(
                CompatibilityLevel.UNSUPPORTED,
                List.of("No compatibility adapter is registered for this request"));
    }
}

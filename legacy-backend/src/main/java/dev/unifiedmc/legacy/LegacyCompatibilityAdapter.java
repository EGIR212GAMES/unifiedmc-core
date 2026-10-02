package dev.unifiedmc.legacy;

import dev.unifiedmc.compat.CompatibilityRequest;
import dev.unifiedmc.compat.CompatibilityResult;

/** Content/mod compatibility adapter; deliberately separate from protocol translation. */
public interface LegacyCompatibilityAdapter {
    String adapterId();

    boolean supports(CompatibilityRequest request);

    CompatibilityResult evaluate(CompatibilityRequest request);
}

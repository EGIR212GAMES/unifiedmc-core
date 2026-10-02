package dev.unifiedmc.uapi.adapter;

import dev.unifiedmc.uapi.compat.CompatibilityContext;
import dev.unifiedmc.uapi.compat.CompatibilityProfile;
import dev.unifiedmc.uapi.compat.CompatibilityReport;
import dev.unifiedmc.uapi.content.UniversalContentBundle;
import dev.unifiedmc.uapi.representation.UniversalRepresentationBundle;

/** Adapter contract that exposes normalized content and explicit compatibility metadata. */
public interface ModAdapter {
    String adapterId();

    String modId();

    CompatibilityProfile profile();

    default CompatibilityReport compatibility(CompatibilityContext context) {
        return dev.unifiedmc.uapi.compat.CompatibilityEvaluator.evaluate(
                adapterId(), profile(), context);
    }

    UniversalContentBundle content(CompatibilityContext context);

    default UniversalRepresentationBundle representations(CompatibilityContext context) {
        return UniversalRepresentationBundle.empty();
    }
}

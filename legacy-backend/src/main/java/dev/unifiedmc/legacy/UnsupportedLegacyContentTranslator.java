package dev.unifiedmc.legacy;

import java.util.List;

/** Safe default: no implicit content translation is attempted. */
public final class UnsupportedLegacyContentTranslator implements LegacyContentTranslator {
    @Override
    public LegacyContentTranslationResult translate(LegacyContentRequest request) {
        return new LegacyContentTranslationResult(
                LegacyContentTranslationResult.Status.UNSUPPORTED,
                List.of(
                        "No dedicated legacy content adapter is registered for "
                                + request.contentId()));
    }
}

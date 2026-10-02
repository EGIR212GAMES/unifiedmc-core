package dev.unifiedmc.legacy;

/**
 * Optional semantic content bridge. It never performs arbitrary bytecode or class transformation.
 */
public interface LegacyContentTranslator {
    LegacyContentTranslationResult translate(LegacyContentRequest request);
}

package dev.unifiedmc.mod.manager;

import dev.unifiedmc.mod.ModArtifact;
import dev.unifiedmc.mod.ModMetadata;
import java.util.List;

/**
 * Reads only known descriptor resources from a JAR; implementations must not define or load mod
 * classes.
 */
public interface ModMetadataReader {
    boolean supports(List<String> entries);

    List<String> entries(ModArtifact artifact) throws Exception;

    List<ModMetadata> read(ModArtifact artifact, List<String> entries) throws Exception;
}

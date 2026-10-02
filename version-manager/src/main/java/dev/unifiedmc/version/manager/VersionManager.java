package dev.unifiedmc.version.manager;

import dev.unifiedmc.version.RuntimeVersion;
import java.util.List;

/** Catalog access for pinned runtime profiles. */
public interface VersionManager {
    List<RuntimeVersion> supportedProfiles();
}

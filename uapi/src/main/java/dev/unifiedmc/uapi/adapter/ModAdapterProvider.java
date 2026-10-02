package dev.unifiedmc.uapi.adapter;

import java.util.Collection;
import java.util.Optional;

/** Provider boundary for discovering independently packaged mod adapters. */
public interface ModAdapterProvider {
    Collection<ModAdapter> adapters();

    default Optional<ModAdapter> findByModId(String modId) {
        return adapters().stream().filter(adapter -> adapter.modId().equals(modId)).findFirst();
    }
}

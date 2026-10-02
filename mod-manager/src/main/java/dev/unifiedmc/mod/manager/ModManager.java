package dev.unifiedmc.mod.manager;

import dev.unifiedmc.config.model.ModConfig;
import dev.unifiedmc.mod.ModAnalysis;
import dev.unifiedmc.mod.ModDescriptor;
import java.util.List;

/** Discovers, analyzes and indexes mods without loading their bytecode. */
public interface ModManager {
    List<ModDescriptor> list();

    void configure(ModConfig configuration);

    void refresh();

    ModScanResult lastScan();

    ModConfig configuration();

    default List<ModAnalysis> analyses() {
        return lastScan().analyses();
    }

    default ModGraph graph() {
        return lastScan().graph();
    }

    default ModDiagnosticReport diagnosticsReport() {
        return lastScan().diagnosticsReport();
    }
}

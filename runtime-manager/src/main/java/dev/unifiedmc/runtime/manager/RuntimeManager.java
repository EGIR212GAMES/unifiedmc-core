package dev.unifiedmc.runtime.manager;

import dev.unifiedmc.runtime.BackendDescriptor;
import dev.unifiedmc.runtime.BackendId;
import dev.unifiedmc.runtime.BackendLaunchRequest;
import dev.unifiedmc.runtime.BackendState;
import dev.unifiedmc.runtime.JavaRuntimeManager;
import dev.unifiedmc.runtime.UnifiedBackend;
import java.util.List;
import java.util.Optional;

/** Manages isolated backend runtimes and their selected Java installations. */
public interface RuntimeManager {
    void register(UnifiedBackend backend);

    Optional<UnifiedBackend> find(BackendId id);

    List<BackendDescriptor> list();

    void start(BackendId id, BackendLaunchRequest request);

    void stop(BackendId id);

    BackendState state(BackendId id);

    JavaRuntimeManager javaRuntimes();
}

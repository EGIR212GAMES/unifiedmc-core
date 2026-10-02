package dev.unifiedmc.runtime;

import java.util.List;
import java.util.Optional;

/** Registry for discovered, installed runtime instances. */
public interface RuntimeRegistry {
    RuntimeDiscoveryReport discover();

    List<MinecraftRuntime> list();

    Optional<MinecraftRuntime> find(String runtimeId);
}

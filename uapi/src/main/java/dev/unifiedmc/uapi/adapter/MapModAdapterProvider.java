package dev.unifiedmc.uapi.adapter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Simple immutable provider useful for bootstrap wiring and tests. */
public final class MapModAdapterProvider implements ModAdapterProvider {
    private final Map<String, ModAdapter> adapters;

    public MapModAdapterProvider(Collection<ModAdapter> adapters) {
        Map<String, ModAdapter> values = new LinkedHashMap<>();
        for (ModAdapter adapter : adapters) {
            Objects.requireNonNull(adapter, "adapter");
            if (values.put(adapter.modId(), adapter) != null) {
                throw new IllegalArgumentException(
                        "Duplicate adapter for mod id: " + adapter.modId());
            }
        }
        this.adapters = Map.copyOf(values);
    }

    @Override
    public Collection<ModAdapter> adapters() {
        return adapters.values();
    }
}

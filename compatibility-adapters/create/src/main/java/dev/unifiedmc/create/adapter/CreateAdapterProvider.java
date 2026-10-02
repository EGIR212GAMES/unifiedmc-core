package dev.unifiedmc.create.adapter;

import dev.unifiedmc.uapi.adapter.ModAdapter;
import dev.unifiedmc.uapi.adapter.ModAdapterProvider;
import java.util.List;

/** Registers the first-party Create adapter without loading the Create mod itself. */
public final class CreateAdapterProvider implements ModAdapterProvider {
    private final CreateAdapter adapter = new CreateAdapter();

    @Override
    public java.util.Collection<ModAdapter> adapters() {
        return List.of(adapter);
    }
}

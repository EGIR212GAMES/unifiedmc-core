package dev.unifiedmc.backend.api;

import dev.unifiedmc.runtime.BackendCapabilities;
import dev.unifiedmc.runtime.BackendDescriptor;

/** Versioned data-only handshake exchanged between Core and an isolated backend process. */
public record BackendHello(BackendDescriptor descriptor, BackendCapabilities capabilities) {}

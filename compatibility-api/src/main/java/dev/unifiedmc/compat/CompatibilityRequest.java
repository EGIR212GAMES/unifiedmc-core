package dev.unifiedmc.compat;

import dev.unifiedmc.mod.ModDescriptor;
import dev.unifiedmc.version.RuntimeVersion;

/** Inputs required to evaluate compatibility without loading a mod. */
public record CompatibilityRequest(ModDescriptor mod, RuntimeVersion target) {}

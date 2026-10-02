package dev.unifiedmc.runtime.manager;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.runtime.RuntimeBackend;
import org.junit.jupiter.api.Test;

class RuntimeCatalogTest {
    @Test
    void containsRequestedArchitecturalCoordinatesWithoutBuildNumbers() {
        var entries = new DefaultRuntimeCatalog().entries();
        assertTrue(
                entries.stream()
                        .anyMatch(
                                entry ->
                                        entry.gameVersion().equals("26.3")
                                                && entry.backend()
                                                        == RuntimeBackend.MODERN_NEOFORGE));
        assertTrue(
                entries.stream()
                        .anyMatch(
                                entry ->
                                        entry.gameVersion().equals("1.12.2")
                                                && entry.backend() == RuntimeBackend.LEGACY_FORGE));
        assertTrue(
                entries.stream()
                        .anyMatch(
                                entry ->
                                        entry.gameVersion().equals("1.7.10")
                                                && entry.javaRequirement().isEmpty()));
    }
}

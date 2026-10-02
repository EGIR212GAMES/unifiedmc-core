package dev.unifiedmc.mod.manager;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.mod.ModId;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ModGraphTest {
    @Test
    void topologicalOrderPutsDependenciesFirst() {
        var a = new ModId("a");
        var b = new ModId("b");
        var c = new ModId("c");
        Map<ModId, Set<ModId>> edges = new LinkedHashMap<>();
        edges.put(a, new LinkedHashSet<>(Set.of(b)));
        edges.put(b, new LinkedHashSet<>(Set.of(c)));
        edges.put(c, new LinkedHashSet<>());
        assertEquals(List.of(a, b, c), new ModGraph(edges).topologicalOrder());
    }

    @Test
    void cycleIsRejected() {
        var a = new ModId("a");
        var b = new ModId("b");
        Map<ModId, Set<ModId>> edges = Map.of(a, Set.of(b), b, Set.of(a));
        assertThrows(IllegalStateException.class, () -> new ModGraph(edges).topologicalOrder());
    }
}

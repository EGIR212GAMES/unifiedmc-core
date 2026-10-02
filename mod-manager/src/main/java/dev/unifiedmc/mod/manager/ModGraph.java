package dev.unifiedmc.mod.manager;

import dev.unifiedmc.mod.ModId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Deterministic directed graph of logical mod dependencies. Edges point dependency -> dependent.
 */
public final class ModGraph {
    private final Map<ModId, Set<ModId>> edges;

    public ModGraph(Map<ModId, Set<ModId>> edges) {
        LinkedHashMap<ModId, Set<ModId>> copy = new LinkedHashMap<>();
        edges.forEach(
                (key, value) ->
                        copy.put(key, Collections.unmodifiableSet(new LinkedHashSet<>(value))));
        this.edges = Collections.unmodifiableMap(copy);
    }

    public Map<ModId, Set<ModId>> edges() {
        return edges;
    }

    public List<ModId> topologicalOrder() {
        Map<ModId, Integer> indegree = new LinkedHashMap<>();
        edges.keySet().forEach(id -> indegree.putIfAbsent(id, 0));
        edges.values().forEach(values -> values.forEach(id -> indegree.putIfAbsent(id, 0)));
        for (var entry : edges.entrySet()) {
            for (ModId dependent : entry.getValue()) {
                indegree.merge(dependent, 1, Integer::sum);
            }
        }
        ArrayDeque<ModId> queue =
                new ArrayDeque<>(
                        indegree.entrySet().stream()
                                .filter(e -> e.getValue() == 0)
                                .map(Map.Entry::getKey)
                                .sorted((a, b) -> a.value().compareTo(b.value()))
                                .toList());
        List<ModId> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            ModId id = queue.removeFirst();
            result.add(id);
            for (ModId dependent : edges.getOrDefault(id, Set.of())) {
                int value = indegree.merge(dependent, -1, Integer::sum);
                if (value == 0) {
                    queue.add(dependent);
                }
            }
        }
        if (result.size() != indegree.size()) {
            throw new IllegalStateException("Mod dependency graph contains a cycle");
        }
        return List.copyOf(result);
    }

    public List<ModId> roots() {
        Set<ModId> dependents = new LinkedHashSet<>();
        edges.values().forEach(dependents::addAll);
        return edges.keySet().stream().filter(id -> !dependents.contains(id)).toList();
    }

    @Override
    public String toString() {
        return edges.toString();
    }
}

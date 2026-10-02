package dev.unifiedmc.create.runtime;

import dev.unifiedmc.create.model.CreateKineticModel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Minimal deterministic kinetic network used by the first Create server-side slice. */
public final class CreateKineticNetwork {
    private final Map<String, CreateKineticModel> nodes = new HashMap<>();
    private double sourceSpeed;

    public void add(CreateKineticModel model) {
        nodes.put(model.id(), model);
    }

    public void remove(String id) {
        nodes.remove(id);
    }

    public void setSourceSpeed(double speed) {
        sourceSpeed = speed;
    }

    public double sourceSpeed() {
        return sourceSpeed;
    }

    public List<CreateKineticModel> propagate() {
        if (nodes.isEmpty()) {

            return List.of();

        }
        List<CreateKineticModel> ordered = nodes.values().stream()
                .sorted(Comparator.comparingInt(CreateKineticModel::x)
                        .thenComparingInt(CreateKineticModel::y)
                        .thenComparingInt(CreateKineticModel::z)
                        .thenComparing(CreateKineticModel::id))
                .toList();
        Map<Coord, CreateKineticModel> byCoord = new HashMap<>();
        for (CreateKineticModel model : ordered) {

            byCoord.put(new Coord(model.x(), model.y(), model.z()), model);

        }

        Set<String> visited = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        CreateKineticModel first = ordered.stream().findFirst().orElseThrow();
        queue.add(first.id());
        Map<String, Double> speed = new HashMap<>();
        speed.put(first.id(), sourceSpeed);
        while (!queue.isEmpty()) {
            CreateKineticModel current = nodes.get(queue.removeFirst());
            if (!visited.add(current.id())) {

                continue;

            }
            double currentSpeed = speed.getOrDefault(current.id(), 0.0);
            for (Coord neighbourCoord : neighbours(current)) {
                CreateKineticModel neighbour = byCoord.get(neighbourCoord);
                if (neighbour == null || !isCompatibleConnection(current, neighbour)) {

                    continue;

                }
                speed.putIfAbsent(neighbour.id(), currentSpeed);
                if (!visited.contains(neighbour.id())) {

                    queue.addLast(neighbour.id());

                }
            }
        }
        List<CreateKineticModel> result = new ArrayList<>();
        for (CreateKineticModel model : ordered) {
            double modelSpeed = speed.getOrDefault(model.id(), 0.0);
            result.add(new CreateKineticModel(model.id(), model.kind(), model.x(), model.y(), model.z(),
                    model.axis(), modelSpeed, sourceSpeed == 0 ? null : networkId(), modelSpeed != 0));
        }
        return List.copyOf(result);
    }

    private long networkId() {
        return Objects.hash(nodes.keySet().stream().sorted().toList());
    }

    private static boolean isCompatibleConnection(CreateKineticModel a, CreateKineticModel b) {
        if (a.axis() != b.axis()) {

            return false;

        }
        return a.kind() == CreateKineticModel.Kind.SHAFT
                || b.kind() == CreateKineticModel.Kind.SHAFT
                || a.kind() == CreateKineticModel.Kind.MECHANICAL_PRESS
                || b.kind() == CreateKineticModel.Kind.MECHANICAL_PRESS;
    }

    private static List<Coord> neighbours(CreateKineticModel model) {
        return switch (model.axis()) {
            case X -> List.of(new Coord(model.x() - 1, model.y(), model.z()), new Coord(model.x() + 1, model.y(), model.z()));
            case Y -> List.of(new Coord(model.x(), model.y() - 1, model.z()), new Coord(model.x(), model.y() + 1, model.z()));
            case Z -> List.of(new Coord(model.x(), model.y(), model.z() - 1), new Coord(model.x(), model.y(), model.z() + 1));
        };
    }

    private record Coord(int x, int y, int z) {}
}

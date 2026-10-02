package dev.unifiedmc.create.runtime;

import dev.unifiedmc.create.model.CreateKineticModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Persistent server-side state for the first Create semantic slice. */
public final class CreateServerState {
    private final Map<String, CreateKineticModel> blocks = new LinkedHashMap<>();
    private final CreateKineticNetwork network = new CreateKineticNetwork();

    public void place(CreateKineticModel model) {
        blocks.put(model.id(), model);
        network.add(model);
    }

    public void remove(String id) {
        blocks.remove(id);
        network.remove(id);
    }

    public void setMechanicalPower(double speed) {
        network.setSourceSpeed(speed);
    }

    public List<CreateKineticModel> tick() {
        List<CreateKineticModel> updated = network.propagate();
        blocks.clear();
        updated.forEach(model -> blocks.put(model.id(), model));
        return updated;
    }

    public List<CreateKineticModel> snapshot() {
        return blocks.values().stream()
                .sorted(Comparator.comparing(CreateKineticModel::id))
                .toList();
    }

    public Map<String, Object> persistenceView() {
        List<Map<String, Object>> entries = new ArrayList<>();
        for (CreateKineticModel model : snapshot()) {
            entries.add(Map.of(
                    "id", model.id(), "kind", model.kind().name(),
                    "x", model.x(), "y", model.y(), "z", model.z(),
                    "axis", model.axis().name(), "speed", model.speed(),
                    "network", model.networkId() == null ? 0L : model.networkId(),
                    "powered", model.powered()));
        }
        return Map.of("sourceSpeed", network.sourceSpeed(), "blocks", entries);
    }
}

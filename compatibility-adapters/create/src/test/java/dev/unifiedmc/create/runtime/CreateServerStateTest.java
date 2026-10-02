package dev.unifiedmc.create.runtime;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.create.model.CreateKineticModel;
import org.junit.jupiter.api.Test;

class CreateServerStateTest {
    @Test
    void statePersistsNormalizedKineticData() {
        CreateServerState state = new CreateServerState();
        state.place(
                new CreateKineticModel(
                        "shaft",
                        CreateKineticModel.Kind.SHAFT,
                        0,
                        0,
                        0,
                        CreateKineticModel.Axis.X,
                        0,
                        null,
                        false));
        state.setMechanicalPower(8);
        state.tick();

        var persisted = state.persistenceView();
        assertEquals(8.0, persisted.get("sourceSpeed"));
        assertEquals(1, ((java.util.List<?>) persisted.get("blocks")).size());
    }
}

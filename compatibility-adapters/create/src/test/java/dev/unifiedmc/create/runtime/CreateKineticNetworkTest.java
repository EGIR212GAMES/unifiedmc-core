package dev.unifiedmc.create.runtime;

import static org.junit.jupiter.api.Assertions.*;

import dev.unifiedmc.create.model.CreateKineticModel;
import org.junit.jupiter.api.Test;

class CreateKineticNetworkTest {
    @Test
    void propagatesPowerAcrossSimpleShaftChain() {
        CreateKineticNetwork network = new CreateKineticNetwork();
        network.add(
                new CreateKineticModel(
                        "shaft-a",
                        CreateKineticModel.Kind.SHAFT,
                        0,
                        0,
                        0,
                        CreateKineticModel.Axis.X,
                        0,
                        null,
                        false));
        network.add(
                new CreateKineticModel(
                        "shaft-b",
                        CreateKineticModel.Kind.SHAFT,
                        1,
                        0,
                        0,
                        CreateKineticModel.Axis.X,
                        0,
                        null,
                        false));
        network.add(
                new CreateKineticModel(
                        "press",
                        CreateKineticModel.Kind.MECHANICAL_PRESS,
                        2,
                        0,
                        0,
                        CreateKineticModel.Axis.X,
                        0,
                        null,
                        false));
        network.setSourceSpeed(16);

        var output = network.propagate();
        assertEquals(3, output.size());
        assertTrue(output.stream().allMatch(CreateKineticModel::powered));
        assertTrue(output.stream().allMatch(m -> Math.abs(m.speed() - 16) < 0.0001));
        assertTrue(output.stream().allMatch(m -> m.networkId() != null));
    }
}

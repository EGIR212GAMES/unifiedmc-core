package dev.unifiedmc.runtime.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.unifiedmc.testkit.BackendFixtures;
import java.util.List;
import org.junit.jupiter.api.Test;

class JavaProcessLauncherTest {
    @Test
    void selectedJavaExecutableIsFirstProcessArgument() {
        var runtime = BackendFixtures.java25Manager().discover().getFirst();
        var request = BackendFixtures.launchRequest();
        var command = new JavaProcessLauncher().buildCommand(runtime, request);

        assertEquals(runtime.javaExecutable().toString(), command.getFirst());
        assertEquals(List.of(runtime.javaExecutable().toString()), command);
    }
}

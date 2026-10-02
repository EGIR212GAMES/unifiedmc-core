package dev.unifiedmc.runtime.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.unifiedmc.runtime.BackendState;
import dev.unifiedmc.testkit.BackendFixtures;
import dev.unifiedmc.version.GameVersion;
import dev.unifiedmc.version.JavaRuntimeRequirement;
import org.junit.jupiter.api.Test;

class DefaultRuntimeManagerTest {
    @Test
    void registersAndFindsBackend() {
        var manager = new DefaultRuntimeManager(BackendFixtures.java25Manager());
        var backend = BackendFixtures.stoppedBackend();
        manager.register(backend);
        assertEquals(backend, manager.find(backend.descriptor().id()).orElseThrow());
    }

    @Test
    void refusesToStartWithoutRequiredJavaRuntime() {
        var manager = new DefaultRuntimeManager(BackendFixtures.emptyJavaRuntimeManager());
        var backend = BackendFixtures.stoppedBackend();
        manager.register(backend);
        assertThrows(
                IllegalStateException.class,
                () -> manager.start(backend.descriptor().id(), BackendFixtures.launchRequest()));
    }

    @Test
    void rejectsJava21ForMinecraft26x() {
        var manager = new DefaultRuntimeManager(BackendFixtures.java21Manager());
        var requirement = new JavaRuntimeRequirement(new GameVersion("26.3"), 25, 25, "test");
        var runtime = manager.javaRuntimes().discover().getFirst();

        var validation = manager.javaRuntimes().validate(runtime, requirement);

        assertTrue(!validation.accepted());
        assertTrue(validation.diagnostic().contains("requires Java 25"));
    }

    @Test
    void selectsJava21ForBackendRequiringJava21() {
        var backend = BackendFixtures.java21CapturingBackend();
        var manager = new DefaultRuntimeManager(BackendFixtures.java21Manager());
        manager.register(backend);

        manager.start(backend.descriptor().id(), BackendFixtures.launchRequest());

        assertEquals(21, backend.selectedJava().orElseThrow().javaVersion());
    }

    @Test
    void selectsJavaRuntimeBeforeStartingBackend() {
        var backend = BackendFixtures.capturingBackend();
        var manager = new DefaultRuntimeManager(BackendFixtures.java25Manager());
        manager.register(backend);

        manager.start(backend.descriptor().id(), BackendFixtures.launchRequest());

        assertTrue(backend.selectedJava().isPresent());
        assertEquals(25, backend.selectedJava().orElseThrow().javaVersion());
        assertEquals(BackendState.STOPPED, backend.state());
    }
}

package dev.unifiedmc.runtime;

import dev.unifiedmc.version.JavaRuntimeRequirement;
import java.util.List;
import java.util.Optional;

/** Discovers, validates and selects Java installations for Core and isolated Minecraft backends. */
public interface JavaRuntimeManager {
    List<JavaRuntimeDescriptor> discover();

    Optional<JavaRuntimeDescriptor> select(JavaRuntimeRequirement requirement);

    JavaRuntimeValidation validate(
            JavaRuntimeDescriptor runtime, JavaRuntimeRequirement requirement);
}

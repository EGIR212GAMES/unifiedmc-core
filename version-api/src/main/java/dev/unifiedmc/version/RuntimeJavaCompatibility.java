package dev.unifiedmc.version;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Explicit Java compatibility catalog for the versions for which the project has authoritative
 * requirements. Unknown generations are intentionally absent rather than guessed.
 */
public final class RuntimeJavaCompatibility {
    private final Map<String, JavaRuntimeRequirement> requirements;

    private RuntimeJavaCompatibility(Map<String, JavaRuntimeRequirement> requirements) {
        this.requirements = Collections.unmodifiableMap(new LinkedHashMap<>(requirements));
    }

    public static RuntimeJavaCompatibility officialBaseline() {
        Map<String, JavaRuntimeRequirement> values = new LinkedHashMap<>();
        add(values, "1.12.2", 8, 8, "https://docs.minecraftforge.net/en/1.12.x/gettingstarted/");
        add(values, "1.18.2", 17, 17, "https://docs.minecraftforge.net/en/fg-5.x/gettingstarted/");
        add(values, "1.20.1", 17, 17, "https://docs.minecraftforge.net/en/1.20.1/gettingstarted/");
        add(values, "1.20.2", 17, 17, "https://docs.neoforged.net/user/docs/");
        add(values, "1.20.3", 17, 17, "https://docs.neoforged.net/user/docs/");
        add(values, "1.20.4", 17, 17, "https://docs.neoforged.net/user/docs/");
        add(
                values,
                "1.20.5",
                21,
                21,
                "https://www.minecraft.net/en-us/article/minecraft-java-edition-1-20-5");
        add(values, "1.20.6", 21, 21, "https://docs.neoforged.net/user/docs/");
        add(values, "1.21.1", 21, 21, "https://docs.neoforged.net/docs/1.21.1/gettingstarted/");
        add(
                values,
                "26.1",
                25,
                25,
                "https://www.minecraft.net/en-us/article/minecraft-java-edition-26-1");
        add(values, "26.2", 25, 25, "https://docs.neoforged.net/primer/docs/26.1/");
        add(values, "26.3", 25, 25, "https://docs.neoforged.net/primer/docs/26.3/");
        return new RuntimeJavaCompatibility(values);
    }

    public Optional<JavaRuntimeRequirement> requirementFor(String minecraftVersion) {
        return Optional.ofNullable(requirements.get(minecraftVersion));
    }

    public List<JavaRuntimeRequirement> all() {
        return requirements.values().stream().toList();
    }

    private static void add(
            Map<String, JavaRuntimeRequirement> values,
            String version,
            int minimum,
            int recommended,
            String source) {
        GameVersion gameVersion = new GameVersion(version);
        values.put(version, new JavaRuntimeRequirement(gameVersion, minimum, recommended, source));
    }
}

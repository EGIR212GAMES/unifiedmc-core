package dev.unifiedmc.mod.manager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

final class ModTestFixtures {
    private ModTestFixtures() {}

    static Path jar(Path directory, String filename, String... entries) throws IOException {
        Path target = directory.resolve(filename);
        try (OutputStream output = Files.newOutputStream(target);
                JarOutputStream jar = new JarOutputStream(output)) {
            for (int i = 0; i < entries.length; i += 2) {
                jar.putNextEntry(new JarEntry(entries[i]));
                jar.write(entries[i + 1].getBytes(java.nio.charset.StandardCharsets.UTF_8));
                jar.closeEntry();
            }
        }
        return target;
    }

    static String fabric(String id, String version, String minecraft, String dependencies) {
        return "{\n"
                + "  \"schemaVersion\": 1,\n"
                + "  \"id\": \""
                + id
                + "\",\n"
                + "  \"version\": \""
                + version
                + "\",\n"
                + "  \"environment\": \"server\",\n"
                + "  \"entrypoints\": {\"main\": [\"example.Main\"]},\n"
                + "  \"depends\": {\"minecraft\": \""
                + minecraft
                + "\""
                + (dependencies.isBlank() ? "" : "," + dependencies)
                + "}\n"
                + "}";
    }

    static String forgeToml(
            String id, String version, String minecraftRange, String dependencyBlock) {
        return "modLoader=\"javafml\"\n"
                + "loaderVersion=\"[1,)\"\n"
                + "license=\"MIT\"\n"
                + "[[mods]]\n"
                + "modId=\""
                + id
                + "\"\n"
                + "version=\""
                + version
                + "\"\n"
                + "displayName=\""
                + id
                + "\"\n"
                + "\n[[dependencies."
                + id
                + "]]\n"
                + "modId=\"minecraft\"\n"
                + "mandatory=true\n"
                + "versionRange=\""
                + minecraftRange
                + "\"\n"
                + "ordering=\"NONE\"\n"
                + "side=\"SERVER\"\n"
                + dependencyBlock;
    }

    static String legacyForge(String id, String version, String minecraft, String requiredMods) {
        String deps =
                requiredMods.isBlank() ? "" : ", \"requiredMods\": [\"" + requiredMods + "\"]";
        return "[{\"modid\":\""
                + id
                + "\",\"name\":\""
                + id
                + "\",\"version\":\""
                + version
                + "\",\"mcversion\":\""
                + minecraft
                + "\""
                + deps
                + "}]";
    }
}

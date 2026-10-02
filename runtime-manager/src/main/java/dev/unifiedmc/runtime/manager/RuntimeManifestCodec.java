package dev.unifiedmc.runtime.manager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.unifiedmc.runtime.RuntimeInstallationManifest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** JSON codec for trusted runtime installation manifests. */
final class RuntimeManifestCodec {
    static final String FILE_NAME = "installation.json";
    private final ObjectMapper mapper;

    RuntimeManifestCodec() {
        mapper =
                JsonMapper.builder()
                        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .build();
    }

    RuntimeInstallationManifest read(Path path) throws IOException {
        return mapper.readValue(Files.readString(path), RuntimeInstallationManifest.class);
    }

    void write(Path path, RuntimeInstallationManifest manifest) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(
                path,
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(manifest)
                        + System.lineSeparator());
    }
}

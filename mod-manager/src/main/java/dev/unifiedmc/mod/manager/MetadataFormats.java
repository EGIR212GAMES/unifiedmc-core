package dev.unifiedmc.mod.manager;

/** Authoritative metadata descriptor names used during metadata-first inspection. */
public final class MetadataFormats {
    public static final String FABRIC = "fabric.mod.json";
    public static final String NEOFORGE = "META-INF/neoforge.mods.toml";
    public static final String FORGE = "META-INF/mods.toml";
    public static final String LEGACY_FORGE = "mcmod.info";
    public static final String MANIFEST = "META-INF/MANIFEST.MF";

    private MetadataFormats() {}
}

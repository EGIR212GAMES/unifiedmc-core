package dev.unifiedmc.uapi.content;

/** Backend-neutral logical network message. No concrete packet classes are exposed. */
public interface UniversalPacket extends UniversalContent {
    String channel();

    String schemaId();
}

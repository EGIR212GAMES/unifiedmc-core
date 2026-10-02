package dev.unifiedmc.uapi.content;

/** Backend-neutral description of a world interaction exposed by an adapter. */
public interface UniversalWorldInteraction extends UniversalContent {
    String interactionType();
}

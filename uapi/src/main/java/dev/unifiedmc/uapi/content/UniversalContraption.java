package dev.unifiedmc.uapi.content;

import java.util.List;

/** Backend-neutral moving/assembled contraption definition. */
public interface UniversalContraption extends UniversalContent {
    List<UniversalIdentifier> components();
}

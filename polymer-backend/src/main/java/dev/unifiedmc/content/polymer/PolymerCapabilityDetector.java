package dev.unifiedmc.content.polymer;

import dev.unifiedmc.uapi.Capability;
import java.util.EnumSet;
import java.util.Set;

/** Explicit feature capability matrix for the current Polymer compiler boundary. */
public final class PolymerCapabilityDetector {
    private static final Set<Capability> SUPPORTED =
            Set.of(
                    Capability.BLOCKS,
                    Capability.ITEMS,
                    Capability.ENTITIES,
                    Capability.RECIPES,
                    Capability.SERVER_SIDE,
                    Capability.POLYMER_REPRESENTATION);

    public Set<Capability> supported() {
        return SUPPORTED;
    }

    public Set<Capability> unsupported(Set<Capability> requested) {
        EnumSet<Capability> unsupported = EnumSet.noneOf(Capability.class);
        for (Capability capability : requested) {
            if (isApplicable(capability) && !SUPPORTED.contains(capability)) {
                unsupported.add(capability);
            }
        }
        return Set.copyOf(unsupported);
    }

    public boolean supports(Capability capability) {
        return SUPPORTED.contains(capability);
    }

    private static boolean isApplicable(Capability capability) {
        return capability != Capability.BEDROCK;
    }
}

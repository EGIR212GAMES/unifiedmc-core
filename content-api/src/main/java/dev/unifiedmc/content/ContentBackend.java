package dev.unifiedmc.content;

/** Lightweight backend capability boundary kept independent from the Content IR module. */
public interface ContentBackend {
    String backendId();

    ContentBackendCapabilities capabilities();
}

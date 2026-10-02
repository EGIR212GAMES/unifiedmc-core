package dev.unifiedmc.content.ir;

/** Version of the stable intermediate representation schema. */
public enum ContentIrVersion {
    V1("1");

    private final String value;

    ContentIrVersion(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}

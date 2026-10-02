package dev.unifiedmc.mod.manager;

/** Conservative version constraint matcher for common Fabric and Maven-style mod ranges. */
public final class ModVersionConstraints {
    private ModVersionConstraints() {}

    public static boolean matches(String version, String constraint) {
        if (constraint == null || constraint.isBlank() || "*".equals(constraint)) {
            return true;
        }
        for (String alt : constraint.split("(?i)\\s+or\\s+|\\s+\\|\\|\\s+")) {
            if (matchesOne(version.trim(), alt.trim())) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesOne(String version, String constraint) {
        if (constraint.isBlank() || "*".equals(constraint)) {
            return true;
        }
        if (constraint.startsWith("[") || constraint.startsWith("(")) {
            return maven(version, constraint);
        }
        if (constraint.startsWith("~")) {
            String base = constraint.substring(1);
            return version.equals(base) || version.startsWith(base + ".");
        }
        if (constraint.endsWith(".*") || constraint.endsWith(".x") || constraint.endsWith(".X")) {
            return version.startsWith(constraint.substring(0, constraint.length() - 2) + ".");
        }
        if (constraint.startsWith(">=")
                || constraint.startsWith("<=")
                || constraint.startsWith(">")
                || constraint.startsWith("<")
                || constraint.startsWith("=")) {
            String operator =
                    constraint.startsWith(">=") || constraint.startsWith("<=")
                            ? constraint.substring(0, 2)
                            : constraint.substring(0, 1);
            String base = constraint.substring(operator.length()).trim();
            int comparison = compare(version, base);
            return switch (operator) {
                case ">=" -> comparison >= 0;
                case "<=" -> comparison <= 0;
                case ">" -> comparison > 0;
                case "<" -> comparison < 0;
                default -> comparison == 0;
            };
        }
        return version.equals(constraint);
    }

    private static boolean maven(String version, String constraint) {
        char open = constraint.charAt(0);
        char close = constraint.charAt(constraint.length() - 1);
        String body = constraint.substring(1, constraint.length() - 1);
        String[] parts = body.split(",", -1);
        String low = parts.length > 0 ? parts[0].trim() : "";
        String high = parts.length > 1 ? parts[1].trim() : "";
        if (!low.isBlank()) {
            int comparison = compare(version, low);
            if (comparison < 0 || (comparison == 0 && open == '(')) {
                return false;
            }
        }
        if (!high.isBlank()) {
            int comparison = compare(version, high);
            if (comparison > 0 || (comparison == 0 && close == ')')) {
                return false;
            }
        }
        return true;
    }

    static int compare(String first, String second) {
        String[] aa = first.split("[.-]");
        String[] bb = second.split("[.-]");
        int count = Math.max(aa.length, bb.length);
        for (int i = 0; i < count; i++) {
            String x = i < aa.length ? aa[i] : "0";
            String y = i < bb.length ? bb[i] : "0";
            Integer xi = parse(x);
            Integer yi = parse(y);
            int comparison = xi != null && yi != null ? Integer.compare(xi, yi) : x.compareTo(y);
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    private static Integer parse(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}

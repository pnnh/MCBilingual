package pnnh.mcbilingual.compat;

/**
 * How this mod treats one other mod. Comparison ignores case and surrounding space.
 * An unrecognized token is not a mode; callers keep Full and report it.
 */
public enum CompatMode {
    FULL,
    GENERIC,
    OFF;

    public static CompatMode parse(String raw) {
        if (raw == null) {
            return null;
        }
        String token = raw.trim();
        if (token.isEmpty()) {
            return null;
        }
        for (CompatMode mode : values()) {
            if (mode.name().equalsIgnoreCase(token)) {
                return mode;
            }
        }
        return null;
    }
}

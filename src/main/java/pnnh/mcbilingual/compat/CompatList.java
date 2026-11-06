package pnnh.mcbilingual.compat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses {@code modid=Mode} lines into an immutable snapshot.
 * This type stays free of Minecraft so unit tests can load it.
 */
public final class CompatList {
    public enum Kind {
        INVALID,
        DUPLICATE
    }

    public record Warning(Kind kind, String modId, String raw) {
    }

    public record Parsed(Map<String, CompatMode> modes, List<Warning> warnings) {
    }

    private CompatList() {
    }

    public static String id(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    public static Parsed parse(List<String> lines) {
        Map<String, CompatMode> modes = new LinkedHashMap<>();
        List<Warning> warnings = new ArrayList<>();
        if (lines == null) {
            return new Parsed(Map.of(), List.of());
        }
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            int split = line.indexOf('=');
            if (split < 0) {
                warnings.add(new Warning(Kind.INVALID, "", line));
                continue;
            }
            String modId = id(line.substring(0, split));
            if (modId.isEmpty()) {
                continue;
            }
            String rawMode = line.substring(split + 1);
            CompatMode mode = CompatMode.parse(rawMode);
            if (modes.containsKey(modId)) {
                warnings.add(new Warning(Kind.DUPLICATE, modId, line));
            }
            if (mode == null) {
                warnings.add(new Warning(Kind.INVALID, modId, line));
                modes.put(modId, CompatMode.FULL);
            } else {
                modes.put(modId, mode);
            }
        }
        return new Parsed(Map.copyOf(modes), List.copyOf(warnings));
    }

    public static CompatMode modeOf(Map<String, CompatMode> modes, String modId) {
        if (modes == null) {
            return CompatMode.FULL;
        }
        CompatMode mode = modes.get(id(modId));
        return mode == null ? CompatMode.FULL : mode;
    }
}

package pnnh.mcbilingual.dictionary;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Conservative English surface→lemma candidates. No Minecraft imports.
 */
public final class EnglishLemmas {
    private EnglishLemmas() {
    }

    public static String normalize(String word) {
        if (word == null) {
            return "";
        }
        return word.strip().toLowerCase(Locale.ROOT);
    }

    /**
     * Ordered unique candidates including the surface form first.
     */
    public static List<String> candidates(String surface) {
        String n = normalize(surface);
        if (n.isEmpty()) {
            return List.of();
        }
        Set<String> out = new LinkedHashSet<>();
        out.add(n);
        if (n.endsWith("ies") && n.length() > 4) {
            out.add(n.substring(0, n.length() - 3) + "y");
        }
        if (n.endsWith("ves") && n.length() > 4) {
            out.add(n.substring(0, n.length() - 3) + "f");
            out.add(n.substring(0, n.length() - 3) + "fe");
        }
        if (n.endsWith("ing") && n.length() > 5) {
            String stem = n.substring(0, n.length() - 3);
            out.add(stem);
            out.add(stem + "e");
            if (stem.length() >= 2 && stem.charAt(stem.length() - 1) == stem.charAt(stem.length() - 2)) {
                out.add(stem.substring(0, stem.length() - 1));
            }
        }
        if (n.endsWith("ed") && n.length() > 4) {
            String stem = n.substring(0, n.length() - 2);
            out.add(stem);
            out.add(stem + "e");
            if (n.endsWith("ied") && n.length() > 4) {
                out.add(n.substring(0, n.length() - 3) + "y");
            }
            if (stem.length() >= 2 && stem.charAt(stem.length() - 1) == stem.charAt(stem.length() - 2)) {
                out.add(stem.substring(0, stem.length() - 1));
            }
        }
        if (n.endsWith("es") && n.length() > 3) {
            out.add(n.substring(0, n.length() - 2));
            out.add(n.substring(0, n.length() - 1));
        } else if (n.endsWith("s") && n.length() > 2 && !n.endsWith("ss")) {
            out.add(n.substring(0, n.length() - 1));
        }
        return new ArrayList<>(out);
    }

    /**
     * Parse ECDICT {@code exchange} tokens like {@code d:tucked/p:tucked/i:tucking}.
     */
    public static List<String> formsFromExchange(String exchange) {
        if (exchange == null || exchange.isBlank()) {
            return List.of();
        }
        Set<String> forms = new LinkedHashSet<>();
        for (String part : exchange.split("[/|;,]")) {
            String p = part.strip();
            if (p.isEmpty()) {
                continue;
            }
            int colon = p.indexOf(':');
            String form = colon >= 0 && colon < p.length() - 1 ? p.substring(colon + 1).strip() : p;
            form = normalize(form);
            if (!form.isEmpty()) {
                forms.add(form);
            }
        }
        return new ArrayList<>(forms);
    }
}

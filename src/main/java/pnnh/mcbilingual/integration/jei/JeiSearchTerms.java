package pnnh.mcbilingual.integration.jei;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Merges cached search strings into the name list JEI already built.
 */
public final class JeiSearchTerms {
    private JeiSearchTerms() {
    }

    /**
     * @return {@code names} when nothing new was added, otherwise a new list
     */
    public static List<String> merge(List<String> names, List<String> extra) {
        if (extra == null || extra.isEmpty()) {
            return names;
        }
        Set<String> combined = new LinkedHashSet<>();
        if (names != null) {
            combined.addAll(names);
        }
        boolean changed = false;
        for (String term : extra) {
            if (combined.add(term)) {
                changed = true;
            }
        }
        if (!changed) {
            return names;
        }
        return new ArrayList<>(combined);
    }
}

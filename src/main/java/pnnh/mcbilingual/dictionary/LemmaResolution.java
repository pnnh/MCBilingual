package pnnh.mcbilingual.dictionary;

import java.util.Objects;

public record LemmaResolution(String surface, LexiconEntry entry, Via via) {
    public enum Via {
        EXACT,
        EXCHANGE,
        HEURISTIC
    }

    public LemmaResolution {
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(entry, "entry");
        Objects.requireNonNull(via, "via");
    }
}

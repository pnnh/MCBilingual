package pnnh.mcbilingual.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Offline English→Chinese learner lexicon. Supports legacy {@code word\\tgloss}
 * and rich {@code word\\tphonetic\\tpos\\texchange\\ttranslation} rows.
 */
public final class LearnerDictionary {
    public static final String DEFAULT_RESOURCE = "/data/mcbilingual/dictionary/entries.tsv";

    private final Map<String, LexiconEntry> byHeadword;
    private final Map<String, String> inflectionToHeadword;

    public LearnerDictionary(Map<String, LexiconEntry> byHeadword, Map<String, String> inflectionToHeadword) {
        this.byHeadword = Map.copyOf(byHeadword);
        this.inflectionToHeadword = Map.copyOf(inflectionToHeadword);
    }

    public static LearnerDictionary loadClasspath(String resourcePath) throws IOException {
        InputStream stream = LearnerDictionary.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IOException("Missing dictionary resource: " + resourcePath);
        }
        try (stream) {
            return load(stream);
        }
    }

    public static LearnerDictionary load(InputStream stream) throws IOException {
        Map<String, LexiconEntry> heads = new HashMap<>();
        Map<String, String> inflection = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                LexiconEntry entry = parseRow(line);
                if (entry == null || entry.headword().isEmpty() || !entry.hasChineseGloss()) {
                    continue;
                }
                String key = EnglishLemmas.normalize(entry.headword());
                heads.putIfAbsent(key, new LexiconEntry(key, entry.phonetic(), entry.pos(), entry.exchange(), entry.translation()));
                for (String form : EnglishLemmas.formsFromExchange(entry.exchange())) {
                    inflection.putIfAbsent(form, key);
                }
            }
        }
        return new LearnerDictionary(heads, inflection);
    }

    static LexiconEntry parseRow(String line) {
        String[] parts = line.split("\t", -1);
        if (parts.length < 2) {
            return null;
        }
        if (parts.length == 2) {
            return new LexiconEntry(parts[0].strip(), "", "", "", parts[1].strip());
        }
        // word, phonetic, pos, exchange, translation (translation may contain tabs — join rest)
        String word = parts[0].strip();
        String phonetic = parts.length > 1 ? parts[1].strip() : "";
        String pos = parts.length > 2 ? parts[2].strip() : "";
        String exchange = parts.length > 3 ? parts[3].strip() : "";
        String translation;
        if (parts.length == 5) {
            translation = parts[4].strip();
        } else if (parts.length > 5) {
            StringBuilder tb = new StringBuilder(parts[4]);
            for (int i = 5; i < parts.length; i++) {
                tb.append('\t').append(parts[i]);
            }
            translation = tb.toString().strip();
        } else {
            // 3–4 columns: treat last as translation
            translation = parts[parts.length - 1].strip();
            if (parts.length == 3) {
                pos = "";
                exchange = "";
                // word \t gloss-only mid? treat parts[1] as translation if no zh in last... keep simple
                translation = parts[1].strip();
            }
        }
        return new LexiconEntry(word, phonetic, pos, exchange, translation);
    }

    /** Legacy single gloss string for callers that only need ZH text. */
    public Optional<String> lookup(String englishWord) {
        return lookupRich(englishWord).map(r -> r.entry().translation());
    }

    public Optional<LemmaResolution> lookupRich(String englishWord) {
        String surface = EnglishLemmas.normalize(englishWord);
        if (surface.isEmpty()) {
            return Optional.empty();
        }
        LexiconEntry exact = byHeadword.get(surface);
        if (exact != null) {
            return Optional.of(new LemmaResolution(surface, exact, LemmaResolution.Via.EXACT));
        }
        String viaExchange = inflectionToHeadword.get(surface);
        if (viaExchange != null) {
            LexiconEntry entry = byHeadword.get(viaExchange);
            if (entry != null) {
                return Optional.of(new LemmaResolution(surface, entry, LemmaResolution.Via.EXCHANGE));
            }
        }
        List<String> candidates = EnglishLemmas.candidates(surface);
        for (String cand : candidates) {
            if (cand.equals(surface)) {
                continue;
            }
            LexiconEntry entry = byHeadword.get(cand);
            if (entry != null) {
                return Optional.of(new LemmaResolution(surface, entry, LemmaResolution.Via.HEURISTIC));
            }
            String mapped = inflectionToHeadword.get(cand);
            if (mapped != null) {
                LexiconEntry e2 = byHeadword.get(mapped);
                if (e2 != null) {
                    return Optional.of(new LemmaResolution(surface, e2, LemmaResolution.Via.HEURISTIC));
                }
            }
        }
        return Optional.empty();
    }

    public int size() {
        return byHeadword.size();
    }
}

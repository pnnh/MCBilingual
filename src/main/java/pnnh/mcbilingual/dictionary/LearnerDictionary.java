package pnnh.mcbilingual.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Offline English→Chinese learner glossary loaded from a TSV resource.
 * Does not import Minecraft types so tests can load classpath fixtures.
 */
public final class LearnerDictionary {
    public static final String DEFAULT_RESOURCE = "/data/mcbilingual/dictionary/entries.tsv";

    private final Map<String, String> entries;

    public LearnerDictionary(Map<String, String> entries) {
        this.entries = Map.copyOf(entries);
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
        Map<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int tab = line.indexOf('\t');
                if (tab <= 0 || tab >= line.length() - 1) {
                    continue;
                }
                String word = line.substring(0, tab).strip();
                String gloss = line.substring(tab + 1).strip();
                if (word.isEmpty() || gloss.isEmpty()) {
                    continue;
                }
                map.putIfAbsent(normalize(word), gloss);
            }
        }
        return new LearnerDictionary(map);
    }

    public Optional<String> lookup(String englishWord) {
        if (englishWord == null || englishWord.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(entries.get(normalize(englishWord)));
    }

    public int size() {
        return entries.size();
    }

    private static String normalize(String word) {
        return word.strip().toLowerCase(Locale.ROOT);
    }
}

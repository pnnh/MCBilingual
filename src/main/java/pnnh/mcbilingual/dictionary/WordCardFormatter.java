package pnnh.mcbilingual.dictionary;

/**
 * Formats a lexicon hit as a compact dictionary card (plain text, multi-line).
 */
public final class WordCardFormatter {
    private WordCardFormatter() {
    }

    public static String format(LemmaResolution resolution) {
        if (resolution == null) {
            return "";
        }
        LexiconEntry e = resolution.entry();
        StringBuilder sb = new StringBuilder();
        sb.append(e.headword());
        if (resolution.via() != LemmaResolution.Via.EXACT
                && !EnglishLemmas.normalize(resolution.surface()).equals(EnglishLemmas.normalize(e.headword()))) {
            sb.append(" ← ").append(resolution.surface());
        }
        if (!e.phonetic().isBlank()) {
            sb.append('\n').append(e.phonetic());
        }
        if (!e.pos().isBlank()) {
            sb.append('\n').append(e.pos());
        }
        if (!e.exchange().isBlank()) {
            sb.append('\n').append(simplifyExchange(e.exchange()));
        }
        if (!e.translation().isBlank()) {
            sb.append('\n').append(e.translation().replace("\\n", "\n"));
        }
        return sb.toString().strip();
    }

    static String simplifyExchange(String exchange) {
        var forms = EnglishLemmas.formsFromExchange(exchange);
        if (forms.isEmpty()) {
            return exchange;
        }
        return String.join(", ", forms);
    }
}

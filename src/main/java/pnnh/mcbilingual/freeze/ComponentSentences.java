package pnnh.mcbilingual.freeze;

import java.util.Optional;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.network.chat.Component;

import pnnh.mcbilingual.freeze.SentenceResolver.Node;
import pnnh.mcbilingual.language.ComponentNodes;
import pnnh.mcbilingual.language.LanguageTexts;

/**
 * Reads a component tree without changing the active language.
 * A finished string is never used as a translation key.
 */
public final class ComponentSentences {
    private ComponentSentences() {
    }

    public static Optional<UiSentence> from(Component component) {
        if (component == null) {
            return Optional.empty();
        }
        String current = LanguageTexts.get().currentLanguage();
        String other = SentenceResolver.counterpartLanguage(current);
        if (other == null) {
            return Optional.empty();
        }
        ClientLanguage table = LanguageTexts.get().clientLanguage(other);
        if (table == null) {
            return Optional.empty();
        }
        Node node = ComponentNodes.from(component, table);
        String visible = component.getString();
        if (visible == null || visible.isBlank()) {
            return Optional.empty();
        }
        String stripped = visible.strip();
        Optional<String> counterpart = SentenceResolver.resolve(current, key -> pattern(table, key), node, visible);
        // Keep visible literals for word / Ollama fallback; empty counterpart means no pack unit.
        return Optional.of(new UiSentence(counterpart.orElse(""), stripped));
    }

    /**
     * True when the sentence has a non-blank language-pack counterpart.
     */
    public static boolean hasCounterpart(UiSentence sentence) {
        return sentence != null && sentence.counterpart() != null && !sentence.counterpart().isBlank();
    }

    private static Optional<String> pattern(ClientLanguage table, String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        String value = table.getOrDefault(key, null);
        if (value == null || value.isBlank() || value.equals(key)) {
            return Optional.empty();
        }
        return Optional.of(value);
    }
}

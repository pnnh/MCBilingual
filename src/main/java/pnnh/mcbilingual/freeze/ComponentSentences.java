package pnnh.mcbilingual.freeze;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import pnnh.mcbilingual.freeze.SentenceResolver.Literal;
import pnnh.mcbilingual.freeze.SentenceResolver.Node;
import pnnh.mcbilingual.freeze.SentenceResolver.Resolved;
import pnnh.mcbilingual.freeze.SentenceResolver.Sequence;
import pnnh.mcbilingual.freeze.SentenceResolver.Translatable;
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
        Node node = read(component, table, new HashSet<>());
        String visible = component.getString();
        return SentenceResolver.resolve(current, key -> pattern(table, key), node, visible)
                .map(text -> new UiSentence(text, visible == null ? "" : visible.strip()));
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

    private static Node read(Component component, ClientLanguage table, Set<String> expanding) {
        List<Node> parts = new ArrayList<>();
        parts.add(readContents(component.getContents(), table, expanding));
        for (Component sibling : component.getSiblings()) {
            parts.add(read(sibling, table, expanding));
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        return new Sequence(List.copyOf(parts));
    }

    private static Node readContents(ComponentContents contents, ClientLanguage table, Set<String> expanding) {
        if (contents instanceof PlainTextContents plain) {
            return new Literal(plain.text());
        }
        if (contents instanceof KeybindContents keybind) {
            return new Translatable(keybind.getName(), List.of());
        }
        if (contents instanceof TranslatableContents translatable) {
            String key = translatable.getKey();
            Component replacement = table.getComponent(key);
            if (replacement != null && expanding.add(key)) {
                Node nested = read(replacement, table, expanding);
                expanding.remove(key);
                if (containsKey(nested)) {
                    return nested;
                }
                return new Resolved(flatten(nested));
            }
            List<Node> args = new ArrayList<>();
            for (Object argument : translatable.getArgs()) {
                if (argument instanceof Component component) {
                    args.add(read(component, table, expanding));
                } else {
                    args.add(new Literal(argument == null ? "null" : argument.toString()));
                }
            }
            return new Translatable(key, List.copyOf(args));
        }
        return new Literal(MutableComponent.create(contents).getString());
    }

    private static boolean containsKey(Node node) {
        if (node instanceof Translatable || node instanceof Resolved) {
            return true;
        }
        if (node instanceof Sequence sequence) {
            for (Node part : sequence.parts()) {
                if (containsKey(part)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String flatten(Node node) {
        if (node instanceof Literal literal) {
            return literal.text() == null ? "" : literal.text();
        }
        if (node instanceof Resolved resolved) {
            return resolved.text() == null ? "" : resolved.text();
        }
        if (node instanceof Sequence sequence) {
            StringBuilder builder = new StringBuilder();
            for (Node part : sequence.parts()) {
                builder.append(flatten(part));
            }
            return builder.toString();
        }
        return "";
    }
}

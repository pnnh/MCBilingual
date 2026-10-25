package pnnh.mcbilingual.language;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

/**
 * Builds a {@link Node} tree from a Minecraft {@link Component} for format expansion
 * in another language table.
 */
public final class ComponentNodes {
    private ComponentNodes() {
    }

    public static Node from(Component component, ClientLanguage otherLanguage) {
        if (component == null) {
            return new Literal("");
        }
        return read(component, otherLanguage, new HashSet<>());
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
            List<Node> args = new ArrayList<>();
            for (Object argument : translatable.getArgs()) {
                if (argument instanceof Component component) {
                    args.add(read(component, table, expanding));
                } else {
                    args.add(new Literal(argument == null ? "null" : argument.toString()));
                }
            }
            // Keep args so "%s Spawner" / "%s刷怪笼" can be filled in the other language.
            if (!args.isEmpty()) {
                return new Translatable(key, List.copyOf(args));
            }
            if (table != null) {
                Component replacement = table.getComponent(key);
                if (replacement != null && expanding.add(key)) {
                    Node nested = read(replacement, table, expanding);
                    expanding.remove(key);
                    if (containsKey(nested)) {
                        return nested;
                    }
                    return new Resolved(flatten(nested));
                }
            }
            return new Translatable(key, List.of());
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

package pnnh.mcbilingual.freeze;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pnnh.mcbilingual.language.LanguageCodes;

/**
 * Turns a sentence that still has translation keys into the other language.
 * Finished strings are not keys. This class does not load Minecraft.
 */
public final class SentenceResolver {
    private static final Pattern FORMAT = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    private SentenceResolver() {
    }

    @FunctionalInterface
    public interface Patterns {
        Optional<String> pattern(String key);
    }

    public sealed interface Node permits Literal, Translatable, Sequence, Resolved {
    }

    public record Literal(String text) implements Node {
    }

    public record Translatable(String key, List<Node> args) implements Node {
    }

    public record Sequence(List<Node> parts) implements Node {
    }

    /**
     * Text already taken from the other language's component entry.
     * It still counts as a translation, and it is not looked up again.
     */
    public record Resolved(String text) implements Node {
    }

    public static String counterpartLanguage(String currentLanguage) {
        if (LanguageCodes.isEnglish(currentLanguage)) {
            return LanguageCodes.ZH_CN;
        }
        if (LanguageCodes.isChinese(currentLanguage)) {
            return LanguageCodes.EN_US;
        }
        return null;
    }

    public static Optional<String> resolve(String currentLanguage, Patterns patterns, Node node, String visible) {
        if (counterpartLanguage(currentLanguage) == null || node == null || !containsKey(node)) {
            return Optional.empty();
        }
        Optional<String> expanded = tryExpand(node, patterns);
        if (expanded.isEmpty()) {
            return Optional.empty();
        }
        String trimmed = expanded.get();
        String current = visible == null ? "" : visible.strip();
        if (trimmed.equals(current)) {
            return Optional.empty();
        }
        return Optional.of(trimmed);
    }

    /**
     * Expand a node tree with {@code %s} / {@code %1$s} patterns from {@code patterns}.
     * Used by freeze counterparts and bilingual secondary names (e.g. Jade spawner).
     */
    public static Optional<String> tryExpand(Node node, Patterns patterns) {
        if (node == null || patterns == null || !containsKey(node)) {
            return Optional.empty();
        }
        String expanded = expand(node, patterns);
        if (expanded == null) {
            return Optional.empty();
        }
        String trimmed = expanded.strip();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(trimmed);
    }

    private static boolean containsKey(Node node) {
        if (node instanceof Translatable || node instanceof Resolved) {
            return true;
        }
        if (node instanceof Sequence sequence) {
            for (Node part : sequence.parts) {
                if (containsKey(part)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String expand(Node node, Patterns patterns) {
        if (node instanceof Literal literal) {
            return literal.text == null ? "" : literal.text;
        }
        if (node instanceof Resolved resolved) {
            return resolved.text == null ? "" : resolved.text;
        }
        if (node instanceof Sequence sequence) {
            StringBuilder builder = new StringBuilder();
            for (Node part : sequence.parts) {
                String text = expand(part, patterns);
                if (text == null) {
                    return null;
                }
                builder.append(text);
            }
            return builder.toString();
        }
        Translatable translatable = (Translatable) node;
        Optional<String> pattern = patterns.pattern(translatable.key);
        if (pattern.isEmpty() || pattern.get().isBlank() || pattern.get().equals(translatable.key)) {
            return null;
        }
        return apply(pattern.get(), translatable.args, patterns);
    }

    private static String apply(String template, List<Node> args, Patterns patterns) {
        Matcher matcher = FORMAT.matcher(template);
        StringBuilder builder = new StringBuilder();
        int sequential = 0;
        int cursor = 0;
        while (matcher.find(cursor)) {
            int start = matcher.start();
            int end = matcher.end();
            if (start > cursor) {
                String between = template.substring(cursor, start);
                if (between.indexOf('%') >= 0) {
                    return null;
                }
                builder.append(between);
            }
            String conversion = matcher.group(2);
            String token = template.substring(start, end);
            if ("%".equals(conversion) && "%%".equals(token)) {
                builder.append('%');
            } else if (!"s".equals(conversion)) {
                return null;
            } else {
                String indexText = matcher.group(1);
                int index = indexText != null ? Integer.parseInt(indexText) - 1 : sequential++;
                if (index < 0 || index >= args.size()) {
                    return null;
                }
                String argument = expand(args.get(index), patterns);
                if (argument == null) {
                    return null;
                }
                builder.append(argument);
            }
            cursor = end;
        }
        if (cursor < template.length()) {
            String tail = template.substring(cursor);
            if (tail.indexOf('%') >= 0) {
                return null;
            }
            builder.append(tail);
        }
        return builder.toString();
    }
}

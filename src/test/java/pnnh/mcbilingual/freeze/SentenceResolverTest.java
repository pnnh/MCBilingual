package pnnh.mcbilingual.freeze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import pnnh.mcbilingual.freeze.SentenceResolver.Literal;
import pnnh.mcbilingual.freeze.SentenceResolver.Node;
import pnnh.mcbilingual.freeze.SentenceResolver.Patterns;
import pnnh.mcbilingual.freeze.SentenceResolver.Translatable;
import pnnh.mcbilingual.language.LanguageCodes;

class SentenceResolverTest {
    @Test
    void englishPrimaryUsesChineseSentence() {
        Optional<String> text = SentenceResolver.resolve(
                LanguageCodes.EN_US,
                key -> optional("item.minecraft.apple", key, "苹果"),
                new Translatable("item.minecraft.apple", List.of()),
                "Apple");

        assertEquals(Optional.of("苹果"), text);
    }

    @Test
    void chinesePrimaryUsesEnglishSentence() {
        Optional<String> text = SentenceResolver.resolve(
                LanguageCodes.ZH_CN,
                key -> optional("item.minecraft.apple", key, "Apple"),
                new Translatable("item.minecraft.apple", List.of()),
                "苹果");

        assertEquals(Optional.of("Apple"), text);
    }

    @Test
    void otherPrimaryShowsNothing() {
        Optional<String> text = SentenceResolver.resolve(
                "ja_jp",
                key -> Optional.of("苹果"),
                new Translatable("item.minecraft.apple", List.of()),
                "リンゴ");

        assertTrue(text.isEmpty());
    }

    @Test
    void missingOrRawKeyShowsNothing() {
        Node node = new Translatable("item.minecraft.apple", List.of());

        assertTrue(SentenceResolver.resolve(LanguageCodes.EN_US, key -> Optional.empty(), node, "Apple").isEmpty());
        assertTrue(SentenceResolver.resolve(
                LanguageCodes.EN_US,
                key -> optional("item.minecraft.apple", key, "item.minecraft.apple"),
                node,
                "Apple").isEmpty());
    }

    @Test
    void sameVisibleTextShowsNothing() {
        Optional<String> text = SentenceResolver.resolve(
                LanguageCodes.EN_US,
                key -> optional("block.minecraft.tnt", key, "TNT"),
                new Translatable("block.minecraft.tnt", List.of()),
                "TNT");

        assertTrue(text.isEmpty());
    }

    @Test
    void literalArgumentStaysInTheTemplate() {
        Patterns patterns = key -> {
            if ("Hello, Steve".equals(key)) {
                return Optional.of("should-not-use");
            }
            return optional("greeting", key, "你好，%s");
        };
        Node node = new Translatable("greeting", List.of(new Literal("Steve")));

        assertEquals(
                Optional.of("你好，Steve"),
                SentenceResolver.resolve(LanguageCodes.EN_US, patterns, node, "Hello, Steve"));
        assertTrue(SentenceResolver.resolve(LanguageCodes.EN_US, patterns, new Literal("Hello, Steve"), "Hello, Steve")
                .isEmpty());
    }

    @Test
    void numberedPlaceholderAndPercent() {
        Node numbered = new Translatable("order", List.of(new Literal("A"), new Literal("B")));
        Node percent = new Translatable("percent", List.of());

        assertEquals(
                Optional.of("B A"),
                SentenceResolver.resolve(
                        LanguageCodes.EN_US,
                        key -> optional("order", key, "%2$s %1$s"),
                        numbered,
                        "A B"));
        assertEquals(
                Optional.of("100%"),
                SentenceResolver.resolve(
                        LanguageCodes.EN_US,
                        key -> optional("percent", key, "100%%"),
                        percent,
                        "100 percent"));
    }

    @Test
    void literalTreeShowsNothing() {
        assertTrue(SentenceResolver.resolve(
                LanguageCodes.EN_US,
                key -> Optional.of("苹果"),
                new Literal("Apple"),
                "Apple").isEmpty());
    }

    @Test
    void nestedTranslatableUsesTheSameCounterpart() {
        Node node = new Translatable("outer", List.of(new Translatable("inner", List.of())));
        Patterns patterns = key -> {
            if ("outer".equals(key)) {
                return Optional.of("%s");
            }
            if ("inner".equals(key)) {
                return Optional.of("世界");
            }
            return Optional.empty();
        };

        assertEquals(Optional.of("世界"), SentenceResolver.resolve(LanguageCodes.EN_US, patterns, node, "World"));
    }

    private static Optional<String> optional(String expectedKey, String key, String pattern) {
        return expectedKey.equals(key) ? Optional.of(pattern) : Optional.empty();
    }
}

package pnnh.mcbilingual.freeze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import pnnh.mcbilingual.freeze.SentenceResolver.Literal;
import pnnh.mcbilingual.freeze.SentenceResolver.Translatable;

class SentenceResolverFormatTest {
    @Test
    void expandsPercentSWithTranslatedArgument() {
        Translatable node = new Translatable(
                "jade.spawner",
                List.of(new Translatable("entity.minecraft.cave_spider", List.of())));
        Optional<String> text = SentenceResolver.tryExpand(node, key -> switch (key) {
            case "jade.spawner" -> Optional.of("%s刷怪笼");
            case "entity.minecraft.cave_spider" -> Optional.of("洞穴蜘蛛");
            default -> Optional.empty();
        });
        assertTrue(text.isPresent());
        assertEquals("洞穴蜘蛛刷怪笼", text.get());
    }
}

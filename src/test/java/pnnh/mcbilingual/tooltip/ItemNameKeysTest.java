package pnnh.mcbilingual.tooltip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.Component;

class ItemNameKeysTest {
    @Test
    void titleComponentKeysComeBeforeFallbacks() {
        Component title = Component.translatable("item.example.sapphire")
                .append(Component.translatable("item.example.suffix"));

        Set<String> keys = new LinkedHashSet<>();
        ItemNameKeys.collect(title, keys);

        assertEquals(List.of("item.example.sapphire", "item.example.suffix"), List.copyOf(keys));
    }

    @Test
    void nestedArgumentsAreCollected() {
        Component title = Component.translatable(
                "item.example.named",
                Component.translatable("item.example.sapphire"));

        Set<String> keys = new LinkedHashSet<>();
        ItemNameKeys.collect(title, keys);

        assertEquals(List.of("item.example.named", "item.example.sapphire"), List.copyOf(keys));
    }

    @Test
    void literalTitlesContributeNoKeys() {
        Set<String> keys = new LinkedHashSet<>();
        ItemNameKeys.collect(Component.literal("Sapphire"), keys);
        assertTrue(keys.isEmpty());
    }
}

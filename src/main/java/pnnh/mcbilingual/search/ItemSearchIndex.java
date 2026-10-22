package pnnh.mcbilingual.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import pnnh.mcbilingual.MCBilingual;
import pnnh.mcbilingual.language.LanguageCodes;
import pnnh.mcbilingual.language.LanguageTexts;

/**
 * Search strings keyed by item description id. Rebuilt only when language tables reload.
 */
public final class ItemSearchIndex {
    private static final String APPLE_KEY = "item.minecraft.apple";

    private static volatile Map<String, List<String>> byDescriptionId = Map.of();

    private ItemSearchIndex() {
    }

    public static void rebuild() {
        LanguageTexts texts = LanguageTexts.get();
        if (!texts.ready()) {
            byDescriptionId = Map.of();
            return;
        }
        Map<String, List<String>> next = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            String key = item.getDescriptionId();
            if (key == null || next.containsKey(key)) {
                continue;
            }
            List<String> queries = ItemSearchTerms.queries(
                    texts.translation(LanguageCodes.ZH_CN, key).orElse(null),
                    texts.translation(LanguageCodes.EN_US, key).orElse(null));
            if (!queries.isEmpty()) {
                next.put(key, queries);
            }
        }
        byDescriptionId = Map.copyOf(next);
        MCBilingual.LOGGER.info(
                "Item search terms ready: {} items, apple={}",
                byDescriptionId.size(),
                byDescriptionId.getOrDefault(APPLE_KEY, List.of()));
    }

    public static List<String> terms(String descriptionId) {
        if (descriptionId == null) {
            return List.of();
        }
        return byDescriptionId.getOrDefault(descriptionId, List.of());
    }

    public static int size() {
        return byDescriptionId.size();
    }
}

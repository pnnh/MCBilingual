package pnnh.mcbilingual.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import pnnh.mcbilingual.MCBilingual;
import pnnh.mcbilingual.language.LanguageCodes;
import pnnh.mcbilingual.language.LanguageTexts;

/**
 * Search strings keyed by the translation key a fluid hover name uses.
 * Rebuilt only when language tables reload.
 */
public final class FluidSearchIndex {
    private static final String WATER_KEY = "block.minecraft.water";

    private static volatile Map<String, List<String>> byDescriptionId = Map.of();

    private FluidSearchIndex() {
    }

    public static void rebuild() {
        LanguageTexts texts = LanguageTexts.get();
        if (!texts.ready()) {
            byDescriptionId = Map.of();
            return;
        }
        Map<String, List<String>> next = new HashMap<>();
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid == null || fluid == Fluids.EMPTY) {
                continue;
            }
            FluidStack stack = new FluidStack(fluid, 1);
            if (stack.isEmpty()) {
                continue;
            }
            String key = stack.getDescriptionId();
            if (key == null || key.isBlank() || next.containsKey(key)) {
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
                "Fluid search terms ready: {} fluids, water={}",
                byDescriptionId.size(),
                byDescriptionId.getOrDefault(WATER_KEY, List.of()));
    }

    public static boolean matches(String descriptionId, String query) {
        if (descriptionId == null) {
            return false;
        }
        return ItemSearchIndex.anyContains(byDescriptionId.getOrDefault(descriptionId, List.of()), query);
    }
}

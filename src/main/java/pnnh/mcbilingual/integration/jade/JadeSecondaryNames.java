package pnnh.mcbilingual.integration.jade;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.ITooltip;

import pnnh.mcbilingual.language.LanguageTexts;
import pnnh.mcbilingual.tooltip.ItemTooltipNames;
import pnnh.mcbilingual.tooltip.SecondaryNameLine;

/**
 * Inserts the same secondary lines used by item tooltips into a Jade overlay.
 */
final class JadeSecondaryNames {
    private JadeSecondaryNames() {
    }

    static void add(ITooltip tooltip, ResourceLocation tag, Component title, List<String> keys) {
        if (!tooltip.get(tag).isEmpty()) {
            return;
        }
        List<SecondaryNameLine> lines = LanguageTexts.get().secondaryLines(title, keys);
        if (lines.isEmpty()) {
            return;
        }
        int insertAt = tooltip.isEmpty() ? 0 : 1;
        for (int i = lines.size() - 1; i >= 0; i--) {
            tooltip.add(insertAt, ItemTooltipNames.grayLine(lines.get(i).text()), tag);
        }
    }
}

package pnnh.mcbilingual.integration.jade;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.ITooltip;

import pnnh.mcbilingual.compat.CompatGate;
import pnnh.mcbilingual.compat.IntegrationGuard;
import pnnh.mcbilingual.language.LanguageTexts;
import pnnh.mcbilingual.tooltip.ItemTooltipNames;
import pnnh.mcbilingual.tooltip.SecondaryNameLine;

/**
 * Inserts the same secondary lines used by item tooltips into a Jade overlay.
 */
final class JadeSecondaryNames {
    private JadeSecondaryNames() {
    }

    static void add(ITooltip tooltip, ResourceLocation tag, String title, String key) {
        if (!CompatGate.dedicatedEnabled("jade")) {
            return;
        }
        try {
            if (!tooltip.get(tag).isEmpty()) {
                return;
            }
            List<SecondaryNameLine> lines = LanguageTexts.get().secondaryLines(title, key);
            if (lines.isEmpty()) {
                return;
            }
            int insertAt = tooltip.isEmpty() ? 0 : 1;
            for (int i = lines.size() - 1; i >= 0; i--) {
                tooltip.add(insertAt, line(lines.get(i)), tag);
            }
        } catch (Exception | LinkageError exception) {
            IntegrationGuard.fail("jade", exception);
        }
    }

    private static Component line(SecondaryNameLine name) {
        return ItemTooltipNames.grayLine(name.text());
    }
}

package pnnh.mcbilingual.tooltip;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Either;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import pnnh.mcbilingual.MCBilingual;
import pnnh.mcbilingual.compat.RivalMods;
import pnnh.mcbilingual.language.LanguageTexts;

/**
 * Adds one gray name line under the item title. The game language is left unchanged.
 */
@EventBusSubscriber(modid = MCBilingual.MOD_ID, value = Dist.CLIENT)
public final class ItemTooltipNames {
    private static final TextColor GRAY = TextColor.fromLegacyFormat(ChatFormatting.GRAY);

    private ItemTooltipNames() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (RivalMods.isBlocked()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        if (tooltip.isEmpty()) {
            return;
        }
        List<SecondaryNameLine> lines = LanguageTexts.get()
                .secondaryLines(tooltip.getFirst().getString(), stack.getItem().getDescriptionId());
        replaceLines(tooltip, lines);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        if (RivalMods.isBlocked()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        if (elements.isEmpty()) {
            return;
        }
        String title = plainText(elements.getFirst().left().orElse(null));
        List<SecondaryNameLine> lines = LanguageTexts.get()
                .secondaryLines(title, stack.getItem().getDescriptionId());
        if (lines.isEmpty()) {
            return;
        }
        List<Either<FormattedText, TooltipComponent>> moved = new ArrayList<>();
        for (int i = elements.size() - 1; i >= 0; i--) {
            Either<FormattedText, TooltipComponent> element = elements.get(i);
            if (matchesLine(element, lines)) {
                moved.add(elements.remove(i));
            }
        }
        int insertAt = Math.min(1, elements.size());
        for (int i = moved.size() - 1; i >= 0; i--) {
            elements.add(insertAt, moved.get(i));
        }
        int cursor = Math.min(insertAt + moved.size(), elements.size());
        for (SecondaryNameLine line : lines) {
            if (containsLine(elements, line)) {
                continue;
            }
            elements.add(cursor, Either.left(grayLine(line.text())));
            cursor++;
        }
    }

    public static Component grayLine(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GRAY);
    }

    private static void replaceLines(List<Component> tooltip, List<SecondaryNameLine> lines) {
        for (int i = tooltip.size() - 1; i >= 1; i--) {
            if (isGrayNamed(tooltip.get(i), lines)) {
                tooltip.remove(i);
            }
        }
        int insertAt = Math.min(1, tooltip.size());
        for (SecondaryNameLine line : lines) {
            tooltip.add(insertAt, grayLine(line.text()));
            insertAt++;
        }
    }

    private static boolean isGrayNamed(Component line, List<SecondaryNameLine> lines) {
        if (GRAY == null || !GRAY.equals(line.getStyle().getColor())) {
            return false;
        }
        String text = line.getString();
        for (SecondaryNameLine candidate : lines) {
            if (candidate.text().equals(text)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsLine(
            List<Either<FormattedText, TooltipComponent>> elements,
            SecondaryNameLine line) {
        for (Either<FormattedText, TooltipComponent> element : elements) {
            if (matchesLine(element, List.of(line))) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesLine(Either<FormattedText, TooltipComponent> element, List<SecondaryNameLine> lines) {
        FormattedText text = element.left().orElse(null);
        if (!(text instanceof Component component) || !isGrayNamed(component, lines)) {
            return false;
        }
        return true;
    }

    private static String plainText(FormattedText text) {
        if (text == null) {
            return "";
        }
        return StringDecomposer.getPlainText(text);
    }
}

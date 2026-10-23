package pnnh.mcbilingual.freeze.mixin;

import java.util.List;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.ClientHooks;

import pnnh.mcbilingual.freeze.FreezeCapture;

/**
 * Tooltip lines are split before they are drawn, and the draw call only has the sequence.
 */
@Mixin(value = ClientHooks.class, remap = false)
public class TooltipOwnerMixin {
    // NeoForge 21.1.251 calls getVisualOrderText from this lambda, not from gatherTooltipComponentsFromElements.
    @Redirect(
            method = "lambda$gatherTooltipComponentsFromElements$10(Lnet/minecraft/network/chat/FormattedText;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;getVisualOrderText()Lnet/minecraft/util/FormattedCharSequence;"),
            remap = false)
    private static FormattedCharSequence mcbilingual$rememberVisual(Component component) {
        FormattedCharSequence sequence = component.getVisualOrderText();
        FreezeCapture.rememberFrame(sequence, component);
        return sequence;
    }

    @Inject(
            method = "splitLine(Lnet/minecraft/network/chat/FormattedText;Lnet/minecraft/client/gui/Font;I)Ljava/util/stream/Stream;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private static void mcbilingual$rememberSplit(
            FormattedText text, Font font, int maxWidth, CallbackInfoReturnable<Stream<ClientTooltipComponent>> callback) {
        if (text instanceof Component component && component.getString().isEmpty()) {
            FormattedCharSequence sequence = component.getVisualOrderText();
            FreezeCapture.rememberFrame(sequence, component);
            callback.setReturnValue(Stream.of(sequence).map(ClientTooltipComponent::create));
            return;
        }
        List<FormattedCharSequence> lines = font.split(text, maxWidth);
        if (text instanceof Component component) {
            for (FormattedCharSequence line : lines) {
                FreezeCapture.rememberFrame(line, component);
            }
        }
        callback.setReturnValue(lines.stream().map(ClientTooltipComponent::create));
    }
}

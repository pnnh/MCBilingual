package pnnh.mcbilingual.freeze.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import pnnh.mcbilingual.freeze.FreezeCapture;

/**
 * Every wrapped fragment points at the message component from before the split.
 * A player message is a literal, so it still produces no sentence.
 */
@Mixin(ChatComponent.class)
public class ChatOwnerMixin {
    @Redirect(
            method = "addMessageToDisplayQueue",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ComponentRenderUtils;wrapComponents(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/client/gui/Font;)Ljava/util/List;"))
    private List<FormattedCharSequence> mcbilingual$rememberChat(FormattedText text, int width, Font font) {
        List<FormattedCharSequence> lines = ComponentRenderUtils.wrapComponents(text, width, font);
        if (text instanceof Component component) {
            for (FormattedCharSequence line : lines) {
                FreezeCapture.remember(line, component);
            }
        }
        return lines;
    }
}

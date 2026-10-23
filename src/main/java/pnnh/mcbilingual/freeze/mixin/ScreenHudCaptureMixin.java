package pnnh.mcbilingual.freeze.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import pnnh.mcbilingual.freeze.FreezeCapture;

/**
 * Container screens copy {@code Screen.render} instead of calling it, and the
 * item tooltip is drawn after that render returns. The bracket has to be the
 * method that does both.
 */
@Mixin(Screen.class)
public class ScreenHudCaptureMixin {
    @Inject(method = "renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("HEAD"))
    private void mcbilingual$beginScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        FreezeCapture.beginDraw();
    }

    @Inject(method = "renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("RETURN"))
    private void mcbilingual$endScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo callback) {
        FreezeCapture.endDraw();
    }
}

package pnnh.mcbilingual.freeze.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

import pnnh.mcbilingual.freeze.FreezeCapture;

@Mixin(Gui.class)
public class GuiHudCaptureMixin {
    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"))
    private void mcbilingual$beginHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo callback) {
        FreezeCapture.beginDraw();
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("RETURN"))
    private void mcbilingual$endHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo callback) {
        FreezeCapture.endDraw();
    }
}

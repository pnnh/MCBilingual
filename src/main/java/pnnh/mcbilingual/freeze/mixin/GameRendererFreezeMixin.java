package pnnh.mcbilingual.freeze.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.neoforge.client.ClientHooks;

import pnnh.mcbilingual.freeze.FreezeFrames;
import pnnh.mcbilingual.freeze.FreezeSession;

/**
 * The whole render method still runs so the projection and the buffer flush stay intact.
 * Only the live world, HUD, and screen are skipped while the saved frame is shown.
 */
@Mixin(GameRenderer.class)
public class GameRendererFreezeMixin {
    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("HEAD"))
    private void mcbilingual$beginFreeze(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo callback) {
        FreezeFrames.beginRender();
    }

    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("RETURN"))
    private void mcbilingual$publishFreeze(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo callback) {
        FreezeFrames.publish();
    }

    @Redirect(
            method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V"))
    private void mcbilingual$skipWorld(GameRenderer renderer, DeltaTracker deltaTracker) {
        if (!FreezeSession.get().active()) {
            renderer.renderLevel(deltaTracker);
        }
    }

    @Redirect(
            method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void mcbilingual$skipHud(Gui gui, GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!FreezeSession.get().active()) {
            gui.render(graphics, deltaTracker);
        }
    }

    @Redirect(
            method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/ClientHooks;drawScreen(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
                    remap = false))
    private void mcbilingual$skipScreen(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!FreezeSession.get().active()) {
            ClientHooks.drawScreen(screen, graphics, mouseX, mouseY, partialTick);
        }
    }

    @Redirect(
            method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;flush()V"))
    private void mcbilingual$paintFreeze(GuiGraphics graphics) {
        if (FreezeSession.get().active()) {
            FreezeFrames.paint(graphics);
        }
        graphics.flush();
    }
}

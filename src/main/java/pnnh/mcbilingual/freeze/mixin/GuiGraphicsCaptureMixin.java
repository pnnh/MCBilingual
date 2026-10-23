package pnnh.mcbilingual.freeze.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import pnnh.mcbilingual.freeze.FreezeCapture;

/**
 * String and sequence overloads do not name a component, so they do not push an owner.
 * Buttons draw a centered component, which never calls the component {@code drawString}.
 */
@Mixin(GuiGraphics.class)
public class GuiGraphicsCaptureMixin {
    @Inject(
            method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",
            at = @At("HEAD"))
    private void mcbilingual$pushString(
            Font font, Component text, int x, int y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> callback) {
        FreezeCapture.pushOwner(text);
    }

    @Inject(
            method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",
            at = @At("RETURN"))
    private void mcbilingual$popString(
            Font font, Component text, int x, int y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> callback) {
        FreezeCapture.popOwner(text);
    }

    @Inject(
            method = "drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V",
            at = @At("HEAD"))
    private void mcbilingual$pushCentered(Font font, Component text, int x, int y, int color, CallbackInfo callback) {
        FreezeCapture.pushOwner(text);
    }

    @Inject(
            method = "drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V",
            at = @At("RETURN"))
    private void mcbilingual$popCentered(Font font, Component text, int x, int y, int color, CallbackInfo callback) {
        FreezeCapture.popOwner(text);
    }

    @Inject(
            method = "drawWordWrap(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/FormattedText;IIII)V",
            at = @At("HEAD"))
    private void mcbilingual$pushWrap(
            Font font, FormattedText text, int x, int y, int lineWidth, int color, CallbackInfo callback) {
        if (text instanceof Component component) {
            FreezeCapture.pushOwner(component);
        }
    }

    @Inject(
            method = "drawWordWrap(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/FormattedText;IIII)V",
            at = @At("RETURN"))
    private void mcbilingual$popWrap(
            Font font, FormattedText text, int x, int y, int lineWidth, int color, CallbackInfo callback) {
        if (text instanceof Component component) {
            FreezeCapture.popOwner(component);
        }
    }

    @Inject(method = "enableScissor(IIII)V", at = @At("HEAD"))
    private void mcbilingual$pushScissor(int minX, int minY, int maxX, int maxY, CallbackInfo callback) {
        FreezeCapture.pushScissor(minX, minY, maxX, maxY);
    }

    @Inject(method = "disableScissor()V", at = @At("HEAD"))
    private void mcbilingual$popScissor(CallbackInfo callback) {
        FreezeCapture.popScissor();
    }
}

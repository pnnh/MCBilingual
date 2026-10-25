package pnnh.mcbilingual.freeze.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

import pnnh.mcbilingual.freeze.FreezeKeyHandler;
import pnnh.mcbilingual.freeze.FreezeSession;
import pnnh.mcbilingual.freeze.LookupModeHud;

@Mixin(MouseHandler.class)
public class MouseLookFreezeMixin {
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;
    @Shadow
    private double accumulatedScrollX;
    @Shadow
    private double accumulatedScrollY;
    @Shadow
    private int activeButton;

    @Inject(method = "turnPlayer(D)V", at = @At("HEAD"), cancellable = true)
    private void mcbilingual$stopLook(double movementTime, CallbackInfo callback) {
        if (!FreezeSession.get().suppressInput()) {
            return;
        }
        this.accumulatedDX = 0.0;
        this.accumulatedDY = 0.0;
        callback.cancel();
    }

    @Inject(method = "handleAccumulatedMovement()V", at = @At("HEAD"), cancellable = true)
    private void mcbilingual$stopDrag(CallbackInfo callback) {
        if (!FreezeSession.get().suppressInput()) {
            return;
        }
        this.accumulatedDX = 0.0;
        this.accumulatedDY = 0.0;
        this.activeButton = -1;
        callback.cancel();
    }

    @Inject(method = "onPress(JIII)V", at = @At("HEAD"), cancellable = true)
    private void mcbilingual$stopButtons(long windowPointer, int button, int action, int modifiers, CallbackInfo callback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || windowPointer != minecraft.getWindow().getWindow()) {
            return;
        }
        if (FreezeKeyHandler.acceptsMouse(button, action)) {
            FreezeKeyHandler.handleMouse(button, action);
            this.activeButton = -1;
            callback.cancel();
            return;
        }
        if (!FreezeSession.get().suppressInput()) {
            return;
        }
        // NeoForge MouseButton.Pre may never run if we cancel onPress at HEAD.
        // Handle the Word/Unit HUD here so clicks are not swallowed.
        if (action == GLFW.GLFW_PRESS && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && tryLookupModeHudClick(minecraft)) {
            this.activeButton = -1;
            callback.cancel();
            return;
        }
        if (minecraft.screen == null && minecraft.getOverlay() == null) {
            KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(button), action != GLFW.GLFW_RELEASE);
        }
        this.activeButton = -1;
        callback.cancel();
    }

    private static boolean tryLookupModeHudClick(Minecraft minecraft) {
        if (minecraft.getWindow().getScreenWidth() <= 0 || minecraft.getWindow().getScreenHeight() <= 0) {
            return false;
        }
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int mouseX = (int) (minecraft.mouseHandler.xpos() * screenWidth / (double) minecraft.getWindow().getScreenWidth());
        int mouseY = (int) (minecraft.mouseHandler.ypos() * screenHeight / (double) minecraft.getWindow().getScreenHeight());
        return LookupModeHud.click(mouseX, mouseY);
    }

    @Inject(method = "onScroll(JDD)V", at = @At("HEAD"), cancellable = true)
    private void mcbilingual$stopScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo callback) {
        if (!FreezeSession.get().suppressInput()) {
            return;
        }
        this.accumulatedScrollX = 0.0;
        this.accumulatedScrollY = 0.0;
        callback.cancel();
    }
}

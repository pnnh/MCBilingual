package pnnh.mcbilingual.freeze.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;

import pnnh.mcbilingual.freeze.FreezeKeyHandler;
import pnnh.mcbilingual.freeze.FreezeSession;

/**
 * The first press is not frozen yet, so it still reaches the key event.
 * Later presses are handled here because that event returns while frozen.
 */
@Mixin(KeyboardHandler.class)
public class KeyboardHandlerFreezeMixin {
    @Inject(method = "keyPress(JIIII)V", at = @At("HEAD"), cancellable = true)
    private void mcbilingual$freezeKeys(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo callback) {
        if (!FreezeSession.get().active()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || windowPointer != minecraft.getWindow().getWindow()) {
            return;
        }
        if (FreezeKeyHandler.acceptsKey(key, scanCode, action)) {
            FreezeKeyHandler.claimDebugKey();
            FreezeKeyHandler.handleKey(key, scanCode, action);
            callback.cancel();
            return;
        }
        KeyMapping.set(InputConstants.getKey(key, scanCode), action != GLFW.GLFW_RELEASE);
        callback.cancel();
    }

    @Inject(method = "charTyped(JII)V", at = @At("HEAD"), cancellable = true)
    private void mcbilingual$freezeChars(long windowPointer, int codePoint, int modifiers, CallbackInfo callback) {
        if (!FreezeSession.get().suppressInput()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && windowPointer == minecraft.getWindow().getWindow()) {
            callback.cancel();
        }
    }
}

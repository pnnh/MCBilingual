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
import pnnh.mcbilingual.freeze.LookupModeKeyHandler;

/**
 * Consumes freeze / mode keys before ChatScreen can type them.
 * While frozen, all other keys are swallowed.
 */
@Mixin(KeyboardHandler.class)
public class KeyboardHandlerFreezeMixin {
    @Inject(method = "keyPress(JIIII)V", at = @At("HEAD"), cancellable = true)
    private void mcbilingual$freezeKeys(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo callback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || windowPointer != minecraft.getWindow().getWindow()) {
            return;
        }
        if (FreezeKeyHandler.acceptsEscape(key, action)) {
            FreezeKeyHandler.handleKey(key, scanCode, action);
            callback.cancel();
            return;
        }
        // Must run even when ChatScreen is open so F3+K is not typed as "k"/"K".
        if (FreezeKeyHandler.acceptsKey(key, scanCode, action)) {
            FreezeKeyHandler.claimDebugKey();
            FreezeKeyHandler.handleKey(key, scanCode, action);
            callback.cancel();
            return;
        }
        if (!FreezeSession.get().active()) {
            return;
        }
        if (LookupModeKeyHandler.acceptsKey(key, scanCode, action)) {
            LookupModeKeyHandler.handleKey(key, scanCode, action);
            callback.cancel();
            return;
        }
        KeyMapping.set(InputConstants.getKey(key, scanCode), action != GLFW.GLFW_RELEASE);
        callback.cancel();
    }

    @Inject(method = "charTyped(JII)V", at = @At("HEAD"), cancellable = true)
    private void mcbilingual$freezeChars(long windowPointer, int codePoint, int modifiers, CallbackInfo callback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || windowPointer != minecraft.getWindow().getWindow()) {
            return;
        }
        // F3+letter still emits a char into chat; drop K while the freeze chord is held.
        if (FreezeKeyHandler.shouldSuppressChatChar(codePoint)) {
            callback.cancel();
            return;
        }
        if (!FreezeSession.get().suppressInput()) {
            return;
        }
        callback.cancel();
    }
}

package pnnh.mcbilingual.freeze.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;

import pnnh.mcbilingual.freeze.FreezeSession;

@Mixin(KeyboardInput.class)
public class KeyboardInputFreezeMixin {
    @Inject(method = "tick(ZF)V", at = @At("HEAD"), cancellable = true)
    private void mcbilingual$stopMovement(boolean isSneaking, float sneakingSpeedMultiplier, CallbackInfo callback) {
        if (!FreezeSession.get().suppressInput()) {
            return;
        }
        Input input = (Input) (Object) this;
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
        callback.cancel();
    }
}

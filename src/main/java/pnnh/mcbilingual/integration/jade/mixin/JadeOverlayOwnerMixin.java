package pnnh.mcbilingual.integration.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import pnnh.mcbilingual.compat.UiOwner;

/**
 * Marks Jade's HUD overlay. {@code renderOverlay478757} is the entry that calls
 * {@code renderOverlay} in Jade 15.10.5. Both are wrapped so either path owns the draw.
 */
@Mixin(targets = "snownee.jade.overlay.OverlayRenderer", remap = false)
public class JadeOverlayOwnerMixin {
    @Inject(method = "renderOverlay478757", at = @At("HEAD"), remap = false)
    private static void mcbilingual$pushHud(CallbackInfo callback) {
        UiOwner.push("jade");
    }

    @Inject(method = "renderOverlay478757", at = @At("RETURN"), remap = false)
    private static void mcbilingual$popHud(CallbackInfo callback) {
        UiOwner.pop("jade");
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), remap = false)
    private static void mcbilingual$pushOverlay(CallbackInfo callback) {
        UiOwner.push("jade");
    }

    @Inject(method = "renderOverlay", at = @At("RETURN"), remap = false)
    private static void mcbilingual$popOverlay(CallbackInfo callback) {
        UiOwner.pop("jade");
    }
}

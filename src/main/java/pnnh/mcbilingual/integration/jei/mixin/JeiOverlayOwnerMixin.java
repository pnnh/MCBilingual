package pnnh.mcbilingual.integration.jei.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import pnnh.mcbilingual.compat.UiOwner;

/**
 * Marks JEI's item list while it draws over a vanilla screen.
 * The screen class stays vanilla, so inventory slots are not treated as JEI.
 */
@Mixin(targets = "mezz.jei.gui.overlay.IngredientListOverlay", remap = false)
public class JeiOverlayOwnerMixin {
    @Inject(method = "drawScreen", at = @At("HEAD"), remap = false)
    private void mcbilingual$pushScreen(CallbackInfo callback) {
        UiOwner.push("jei");
    }

    @Inject(method = "drawScreen", at = @At("RETURN"), remap = false)
    private void mcbilingual$popScreen(CallbackInfo callback) {
        UiOwner.pop("jei");
    }

    @Inject(method = "drawForeground", at = @At("HEAD"), remap = false)
    private void mcbilingual$pushForeground(CallbackInfo callback) {
        UiOwner.push("jei");
    }

    @Inject(method = "drawForeground", at = @At("RETURN"), remap = false)
    private void mcbilingual$popForeground(CallbackInfo callback) {
        UiOwner.pop("jei");
    }

    @Inject(method = "drawTooltips", at = @At("HEAD"), remap = false)
    private void mcbilingual$pushTooltips(CallbackInfo callback) {
        UiOwner.push("jei");
    }

    @Inject(method = "drawTooltips", at = @At("RETURN"), remap = false)
    private void mcbilingual$popTooltips(CallbackInfo callback) {
        UiOwner.pop("jei");
    }
}

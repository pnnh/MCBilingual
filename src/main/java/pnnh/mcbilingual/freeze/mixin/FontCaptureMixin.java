package pnnh.mcbilingual.freeze.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;

import pnnh.mcbilingual.freeze.FreezeCapture;

/**
 * A string draw and a literal component have no translation key, so they produce no hit.
 * Finished text is not indexed back to a key.
 */
@Mixin(Font.class)
public class FontCaptureMixin {
    @Inject(
            method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I",
            at = @At("HEAD"))
    private void mcbilingual$record(
            FormattedCharSequence text,
            float x,
            float y,
            int color,
            boolean dropShadow,
            Matrix4f matrix,
            MultiBufferSource buffer,
            Font.DisplayMode displayMode,
            int backgroundColor,
            int packedLightCoords,
            CallbackInfoReturnable<Integer> callback) {
        FreezeCapture.glyph((Font) (Object) this, text, x, y, matrix);
    }
}

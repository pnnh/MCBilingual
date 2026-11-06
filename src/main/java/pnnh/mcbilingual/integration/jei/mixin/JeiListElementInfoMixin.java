package pnnh.mcbilingual.integration.jei.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.ListElementInfo;

import pnnh.mcbilingual.integration.jei.JeiSearchAppender;
import pnnh.mcbilingual.integration.jei.JeiVersions;

/**
 * JEI 19.56.0 and newer. {@code ListElementInfo} takes {@code IIngredientFilterConfig}.
 * Tag {@code v19.56.0} added that argument in commit {@code 0f2120c687}.
 * {@link pnnh.mcbilingual.integration.jei.JeiLineChoice} selects this class by its simple name.
 */
@Mixin(value = ListElementInfo.class, remap = false)
public class JeiListElementInfoMixin {
    @Shadow
    @Final
    private IListElement<?> element;

    // Final through JEI 19.60. 19.70 drops final. @Mutable lets this method write the field on both.
    @Shadow
    @Mutable
    private List<String> names;

    @Inject(method = JeiVersions.FILTER_CONFIG_INIT, at = @At("RETURN"), remap = false, require = 1)
    private void mcbilingual$appendSearchTerms(CallbackInfo callback) {
        names = JeiSearchAppender.append(element, names);
    }
}

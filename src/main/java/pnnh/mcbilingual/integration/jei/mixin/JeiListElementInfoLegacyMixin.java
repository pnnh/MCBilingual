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
 * JEI before 19.56.0, including Better MC v53's 19.27.0.343.
 * {@code ListElementInfo} is {@code (IListElement, IIngredientManager, IModIdHelper)}.
 * {@link pnnh.mcbilingual.integration.jei.JeiLineChoice} selects this class by its simple name.
 */
@Mixin(value = ListElementInfo.class, remap = false)
public class JeiListElementInfoLegacyMixin {
    @Shadow
    @Final
    private IListElement<?> element;

    // This field is final on the pre-19.56 line. The handler writes it from another method.
    @Shadow
    @Mutable
    private List<String> names;

    @Inject(method = JeiVersions.LEGACY_INIT, at = @At("RETURN"), remap = false, require = 1)
    private void mcbilingual$appendSearchTerms(CallbackInfo callback) {
        names = JeiSearchAppender.append(element, names);
    }
}

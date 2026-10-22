package pnnh.mcbilingual.integration.jei.mixin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.gui.ingredients.ListElementInfo;
import net.minecraft.world.item.ItemStack;

import pnnh.mcbilingual.search.ItemSearchIndex;

/**
 * Adds cached search strings to the list both JEI indexes read.
 * Ingredient aliases are not used: JEI prints those in the item tooltip.
 */
@Mixin(value = ListElementInfo.class, remap = false)
public class JeiListElementInfoMixin {
    @Shadow
    @Final
    @Mutable
    private List<String> names;

    @Inject(
            method = "<init>(Lmezz/jei/gui/ingredients/IListElement;Lmezz/jei/api/runtime/IIngredientManager;Lmezz/jei/common/config/IIngredientFilterConfig;Lmezz/jei/api/helpers/IModIdHelper;)V",
            at = @At("RETURN"),
            remap = false)
    private void mcbilingual$appendSearchTerms(CallbackInfo callback, @Local ITypedIngredient<?> typed) {
        if (typed == null || !(typed.getIngredient() instanceof ItemStack stack) || stack.isEmpty()) {
            return;
        }
        List<String> extra = ItemSearchIndex.terms(stack.getItem().getDescriptionId());
        if (extra.isEmpty()) {
            return;
        }
        Set<String> combined = new LinkedHashSet<>();
        if (names != null) {
            combined.addAll(names);
        }
        boolean changed = false;
        for (String term : extra) {
            if (combined.add(term)) {
                changed = true;
            }
        }
        if (changed) {
            names = new ArrayList<>(combined);
        }
    }
}

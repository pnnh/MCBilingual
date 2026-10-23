package pnnh.mcbilingual.integration.integratedterminals.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.ItemStack;

import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerItemStack;
import org.cyclops.integratedterminals.core.terminalstorage.query.SearchMode;

import pnnh.mcbilingual.search.ItemSearchIndex;

/**
 * Keeps the terminal's current hover-name test and also accepts item search strings.
 * Mod, tooltip, and tag queries stay on the original predicate.
 */
@Mixin(value = IngredientComponentTerminalStorageHandlerItemStack.class, remap = false)
public class TerminalItemFilterMixin {
    @Inject(
            method = "getInstanceFilterPredicate",
            at = @At("RETURN"),
            cancellable = true,
            remap = false)
    private void mcbilingual$widenDefaultName(
            SearchMode searchMode,
            String query,
            CallbackInfoReturnable<Predicate<ItemStack>> callback) {
        if (searchMode != SearchMode.DEFAULT) {
            return;
        }
        Predicate<ItemStack> original = callback.getReturnValue();
        if (original == null) {
            return;
        }
        Predicate<ItemStack> widened = stack ->
                original.test(stack)
                        || ItemSearchIndex.matches(stack.getItem().getDescriptionId(), query);
        callback.setReturnValue(widened);
    }
}

package pnnh.mcbilingual.integration.integratedterminals.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.neoforged.neoforge.fluids.FluidStack;

import org.cyclops.integratedterminals.capability.ingredient.IngredientComponentTerminalStorageHandlerFluidStack;
import org.cyclops.integratedterminals.core.terminalstorage.query.SearchMode;

import pnnh.mcbilingual.search.FluidSearchIndex;

/**
 * Keeps the terminal's current fluid hover-name test and also accepts fluid search strings.
 * Mod and tag queries stay on the original predicate. Energy is not handled here.
 */
@Mixin(value = IngredientComponentTerminalStorageHandlerFluidStack.class, remap = false)
public class TerminalFluidFilterMixin {
    @Inject(
            method = "getInstanceFilterPredicate",
            at = @At("RETURN"),
            cancellable = true,
            remap = false)
    private void mcbilingual$widenDefaultName(
            SearchMode searchMode,
            String query,
            CallbackInfoReturnable<Predicate<FluidStack>> callback) {
        if (searchMode != SearchMode.DEFAULT) {
            return;
        }
        Predicate<FluidStack> original = callback.getReturnValue();
        if (original == null) {
            return;
        }
        Predicate<FluidStack> widened = stack ->
                original.test(stack)
                        || (stack != null
                                && !stack.isEmpty()
                                && FluidSearchIndex.matches(stack.getDescriptionId(), query));
        callback.setReturnValue(widened);
    }
}

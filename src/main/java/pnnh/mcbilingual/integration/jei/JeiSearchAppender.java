package pnnh.mcbilingual.integration.jei;

import java.util.List;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.gui.ingredients.IListElement;
import net.minecraft.world.item.ItemStack;

import pnnh.mcbilingual.diagnostics.LoadReport;
import pnnh.mcbilingual.search.ItemSearchIndex;

/**
 * Writes cached item search strings into the name list JEI indexes.
 * Both constructor mixins call this. The write is the same; only the constructor differs.
 */
public final class JeiSearchAppender {
    private JeiSearchAppender() {
    }

    public static List<String> append(IListElement<?> element, List<String> names) {
        try {
            LoadReport.jeiHookRan(ItemSearchIndex.size());
            if (element == null) {
                return names;
            }
            ITypedIngredient<?> typed = element.getTypedIngredient();
            if (typed == null || !(typed.getIngredient() instanceof ItemStack stack) || stack.isEmpty()) {
                return names;
            }
            return JeiSearchTerms.merge(names, ItemSearchIndex.terms(stack.getItem().getDescriptionId()));
        } catch (Exception | LinkageError exception) {
            LoadReport.jeiSearchFailed(exception);
            return names;
        }
    }
}

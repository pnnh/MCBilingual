package pnnh.mcbilingual.integration.ae2.mixin;

import java.util.ArrayList;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.menu.me.common.GridInventoryEntry;

import pnnh.mcbilingual.integration.ae2.Ae2ItemQuery;

/**
 * Keeps AE2's current-name test and also accepts this mod's item search strings.
 * Prefixed queries are left untouched.
 */
@Mixin(targets = "appeng.client.gui.me.search.RepoSearch", remap = false)
public class Ae2RepoSearchMixin {
    @Redirect(
            method = "getPredicates",
            at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z"),
            remap = false)
    @SuppressWarnings("unchecked")
    private boolean mcbilingual$wrapNameSearch(ArrayList<Object> list, Object predicate) {
        if (predicate instanceof Ae2NameSearchAccessor named) {
            String term = named.mcbilingual$term();
            Predicate<GridInventoryEntry> original = (Predicate<GridInventoryEntry>) predicate;
            Predicate<GridInventoryEntry> wrapped = entry ->
                    original.test(entry) || Ae2ItemQuery.matches(entry, term);
            return list.add(wrapped);
        }
        return list.add(predicate);
    }
}

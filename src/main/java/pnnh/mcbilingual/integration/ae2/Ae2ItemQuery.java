package pnnh.mcbilingual.integration.ae2;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.menu.me.common.GridInventoryEntry;

import pnnh.mcbilingual.search.ItemSearchIndex;

/**
 * Extra name match for one AE2 search token. Fluids stay on AE2's own predicate.
 */
public final class Ae2ItemQuery {
    private Ae2ItemQuery() {
    }

    public static boolean matches(GridInventoryEntry entry, String term) {
        if (entry == null || term == null || term.isBlank()) {
            return false;
        }
        AEKey key = entry.getWhat();
        if (!(key instanceof AEItemKey itemKey)) {
            return false;
        }
        return ItemSearchIndex.matches(itemKey.getItem().getDescriptionId(), term);
    }
}

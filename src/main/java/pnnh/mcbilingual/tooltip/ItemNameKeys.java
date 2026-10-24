package pnnh.mcbilingual.tooltip;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;

/**
 * Translation keys that may name an item. Hover text and {@link ItemStack#getDescriptionId()}
 * are not always the same key; merchants and subtype items often expose the real name only in
 * the title component.
 */
public final class ItemNameKeys {
    private ItemNameKeys() {
    }

    /**
     * Ordered candidates: title component keys first, then the stack-aware id, then the bare item id.
     */
    public static List<String> candidates(Component title, ItemStack stack) {
        Set<String> keys = new LinkedHashSet<>();
        collect(title, keys);
        if (stack != null && !stack.isEmpty()) {
            add(keys, stack.getDescriptionId());
            add(keys, stack.getItem().getDescriptionId());
        }
        return List.copyOf(keys);
    }

    static void collect(Component component, Set<String> keys) {
        if (component == null) {
            return;
        }
        collectContents(component.getContents(), keys);
        for (Component sibling : component.getSiblings()) {
            collect(sibling, keys);
        }
    }

    private static void collectContents(ComponentContents contents, Set<String> keys) {
        if (!(contents instanceof TranslatableContents translatable)) {
            return;
        }
        add(keys, translatable.getKey());
        for (Object argument : translatable.getArgs()) {
            if (argument instanceof Component component) {
                collect(component, keys);
            }
        }
    }

    private static void add(Set<String> keys, String key) {
        if (key != null && !key.isBlank()) {
            keys.add(key);
        }
    }
}

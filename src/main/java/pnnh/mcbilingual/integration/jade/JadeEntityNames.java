package pnnh.mcbilingual.integration.jade;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;

import pnnh.mcbilingual.MCBilingual;
import pnnh.mcbilingual.tooltip.ItemNameKeys;

public enum JadeEntityNames implements IEntityComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.parse(MCBilingual.MOD_ID + ":jade_entity_name");
    private static final ResourceLocation TAG = ResourceLocation.parse(MCBilingual.MOD_ID + ":jade_entity_secondary_name");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        Query query = resolve(accessor.getEntity());
        JadeSecondaryNames.add(tooltip, TAG, query.title(), query.keys());
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public int getDefaultPriority() {
        return TooltipPosition.HEAD + 50;
    }

    private static Query resolve(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (!stack.isEmpty()) {
                return new Query(stack.getHoverName().getString(), ItemNameKeys.candidates(stack.getHoverName(), stack));
            }
        }
        return new Query(entity.getDisplayName().getString(), List.of(entity.getType().getDescriptionId()));
    }

    private record Query(String title, List<String> keys) {
    }
}

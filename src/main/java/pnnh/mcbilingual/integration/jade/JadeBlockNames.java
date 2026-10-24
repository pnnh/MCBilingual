package pnnh.mcbilingual.integration.jade;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;

import pnnh.mcbilingual.MCBilingual;
import pnnh.mcbilingual.tooltip.ItemNameKeys;

public enum JadeBlockNames implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.parse(MCBilingual.MOD_ID + ":jade_block_name");
    private static final ResourceLocation TAG = ResourceLocation.parse(MCBilingual.MOD_ID + ":jade_secondary_name");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        Query query = resolve(accessor);
        JadeSecondaryNames.add(tooltip, TAG, query.title(), query.keys());
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public int getDefaultPriority() {
        // Just after Jade's own head line, so the block name is already present.
        return TooltipPosition.HEAD + 50;
    }

    private static Query resolve(BlockAccessor accessor) {
        Block block = accessor.getBlock();
        if (block instanceof LiquidBlock) {
            FluidState fluidState = accessor.getBlockState().getFluidState();
            if (!fluidState.isEmpty()) {
                FluidStack fluidStack = new FluidStack(fluidState.getType(), 1);
                Component description = fluidStack.getFluidType().getDescription(fluidStack);
                return new Query(description.getString(), List.of(fluidStack.getFluidType().getDescriptionId()));
            }
        }

        ItemStack picked = accessor.getPickedResult();
        if (!picked.isEmpty()) {
            return new Query(picked.getHoverName().getString(), ItemNameKeys.candidates(picked.getHoverName(), picked));
        }
        return new Query(block.getName().getString(), List.of(block.getDescriptionId()));
    }

    private record Query(String title, List<String> keys) {
    }
}

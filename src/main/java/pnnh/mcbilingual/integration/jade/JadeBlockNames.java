package pnnh.mcbilingual.integration.jade;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
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
    private static final String JADE_SPAWNER_KEY = "jade.spawner";

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
        Optional<Query> spawner = spawnerQuery(accessor);
        if (spawner.isPresent()) {
            return spawner.get();
        }

        Block block = accessor.getBlock();
        if (block instanceof LiquidBlock) {
            FluidState fluidState = accessor.getBlockState().getFluidState();
            if (!fluidState.isEmpty()) {
                FluidStack fluidStack = new FluidStack(fluidState.getType(), 1);
                Component description = fluidStack.getFluidType().getDescription(fluidStack);
                return new Query(description, List.of(fluidStack.getFluidType().getDescriptionId()));
            }
        }

        ItemStack picked = accessor.getPickedResult();
        if (!picked.isEmpty()) {
            Component hover = picked.getHoverName();
            return new Query(hover, ItemNameKeys.candidates(hover, picked));
        }
        Component name = block.getName();
        return new Query(name, List.of(block.getDescriptionId()));
    }

    /**
     * Jade titles spawners as {@code jade.spawner} + entity name. Rebuild that component so
     * secondary-language expansion can fill {@code %s}.
     */
    private static Optional<Query> spawnerQuery(BlockAccessor accessor) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (!(blockEntity instanceof SpawnerBlockEntity spawner)) {
            return Optional.empty();
        }
        Level level = accessor.getLevel();
        BlockPos pos = accessor.getPosition();
        if (level == null || pos == null) {
            return Optional.empty();
        }
        Entity display;
        try {
            display = spawner.getSpawner().getOrCreateDisplayEntity(level, pos);
        } catch (RuntimeException | LinkageError ignored) {
            return Optional.empty();
        }
        if (display == null) {
            return Optional.empty();
        }
        Component title = Component.translatable(JADE_SPAWNER_KEY, display.getType().getDescription());
        return Optional.of(new Query(title, ItemNameKeys.candidates(title, ItemStack.EMPTY)));
    }

    private record Query(Component title, List<String> keys) {
    }
}

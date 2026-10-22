package pnnh.mcbilingual.switchlang.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;

@Mixin(AdvancementsScreen.class)
public interface AdvancementsScreenAccessor {
    @Accessor("tabs")
    Map<AdvancementHolder, AdvancementTab> mcbilingual$tabs();

    @Accessor("selectedTab")
    AdvancementTab mcbilingual$selectedTab();

    @Accessor("selectedTab")
    void mcbilingual$setSelectedTab(AdvancementTab tab);

    @Accessor("advancements")
    ClientAdvancements mcbilingual$advancements();
}

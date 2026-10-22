package pnnh.mcbilingual.switchlang.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screens.Screen;

@Mixin(Screen.class)
public interface ScreenRebuild {
    @Invoker("rebuildWidgets")
    void mcbilingual$rebuildWidgets();
}

package pnnh.mcbilingual.switchlang.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.Display;

@Mixin(Display.TextDisplay.class)
public interface TextDisplayAccessor {
    @Accessor("clientDisplayCache")
    void mcbilingual$setClientDisplayCache(Display.TextDisplay.CachedInfo cache);
}

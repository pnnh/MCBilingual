package pnnh.mcbilingual.switchlang.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.KeyboardHandler;

/**
 * F3's release toggles the debug overlay unless a chord already claimed it.
 */
@Mixin(KeyboardHandler.class)
public interface KeyboardHandlerAccessor {
    @Accessor("handledDebugKey")
    void mcbilingual$setHandledDebugKey(boolean handled);
}

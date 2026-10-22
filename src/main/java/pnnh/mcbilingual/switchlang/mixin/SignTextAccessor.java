package pnnh.mcbilingual.switchlang.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignText;

@Mixin(SignText.class)
public interface SignTextAccessor {
    @Accessor("renderMessages")
    void mcbilingual$setRenderMessages(FormattedCharSequence[] messages);
}

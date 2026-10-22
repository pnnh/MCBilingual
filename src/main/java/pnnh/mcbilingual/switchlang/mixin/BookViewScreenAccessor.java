package pnnh.mcbilingual.switchlang.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.BookViewScreen;

@Mixin(BookViewScreen.class)
public interface BookViewScreenAccessor {
    @Accessor("cachedPage")
    void mcbilingual$setCachedPage(int page);
}

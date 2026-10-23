package pnnh.mcbilingual.integration.ae2.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "appeng.client.gui.me.search.NameSearchPredicate", remap = false)
public interface Ae2NameSearchAccessor {
    @Accessor("term")
    String mcbilingual$term();
}

package pnnh.mcbilingual.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

import pnnh.mcbilingual.MCBilingual;
import pnnh.mcbilingual.search.ItemSearchIndex;

/**
 * Marks the JEI integration. Search strings are added by the list mixin, not as visible aliases.
 */
@JeiPlugin
public class JeiSearchPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(MCBilingual.MOD_ID, "search");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        MCBilingual.LOGGER.info("JEI search integration active, indexed items={}", ItemSearchIndex.size());
    }
}

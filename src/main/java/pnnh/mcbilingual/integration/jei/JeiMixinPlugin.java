package pnnh.mcbilingual.integration.jei;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.neoforged.fml.loading.LoadingModList;

import pnnh.mcbilingual.compat.RivalMods;

/**
 * Skips JEI mixins when the mod is not installed, so the client still reaches a world.
 */
public final class JeiMixinPlugin implements IMixinConfigPlugin {
    private boolean jeiLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        LoadingModList mods = LoadingModList.get();
        jeiLoaded = mods != null && mods.getModFileById("jei") != null && !RivalMods.isBlocked();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return jeiLoaded;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}

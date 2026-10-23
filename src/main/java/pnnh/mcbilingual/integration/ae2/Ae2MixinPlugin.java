package pnnh.mcbilingual.integration.ae2;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.neoforged.fml.loading.LoadingModList;

import pnnh.mcbilingual.compat.RivalMods;

/**
 * Applies the terminal search mixin only when AE2 is installed and this mod is active.
 */
public final class Ae2MixinPlugin implements IMixinConfigPlugin {
    private boolean apply;

    @Override
    public void onLoad(String mixinPackage) {
        LoadingModList mods = LoadingModList.get();
        boolean ae2Loaded = mods != null && mods.getModFileById("ae2") != null;
        apply = ae2Loaded && !RivalMods.isBlocked();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return apply;
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

package pnnh.mcbilingual.integration.jade;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.neoforged.fml.loading.LoadingModList;

import pnnh.mcbilingual.compat.IntegrationGuard;
import pnnh.mcbilingual.compat.RivalMods;

/**
 * Applies the Jade overlay owner mixin only when Jade is installed and this mod is active.
 * Full, Generic, and Off are read while the overlay draws.
 */
public final class JadeMixinPlugin implements IMixinConfigPlugin {
    private boolean apply;

    @Override
    public void onLoad(String mixinPackage) {
        apply = false;
        try {
            if (RivalMods.isBlocked()) {
                return;
            }
            LoadingModList mods = LoadingModList.get();
            apply = mods != null && mods.getModFileById("jade") != null;
        } catch (Exception | LinkageError exception) {
            apply = false;
            IntegrationGuard.fail("jade", exception);
        }
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

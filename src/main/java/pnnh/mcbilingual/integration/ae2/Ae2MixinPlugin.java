package pnnh.mcbilingual.integration.ae2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;

import pnnh.mcbilingual.compat.IntegrationGuard;
import pnnh.mcbilingual.compat.RivalMods;

/**
 * Applies the terminal search mixin only when AE2 is installed, this mod is active,
 * and {@code RepoSearch.getPredicates} is still present. The config mode is read later.
 */
public final class Ae2MixinPlugin implements IMixinConfigPlugin {
    private boolean apply;

    @Override
    public void onLoad(String mixinPackage) {
        apply = false;
        try {
            if (RivalMods.isBlocked()) {
                return;
            }
            LoadingModList mods = LoadingModList.get();
            if (mods == null) {
                return;
            }
            ModFileInfo ae2 = mods.getModFileById("ae2");
            if (ae2 == null) {
                return;
            }
            if (!hasMethod(ae2, "getPredicates", "appeng", "client", "gui", "me", "search", "RepoSearch.class")) {
                IntegrationGuard.fail("ae2", "找不到 RepoSearch.getPredicates。 RepoSearch.getPredicates is missing.");
                return;
            }
            apply = true;
        } catch (Exception | LinkageError exception) {
            apply = false;
            IntegrationGuard.fail("ae2", exception);
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

    private static boolean hasMethod(ModFileInfo mod, String methodName, String... resource) throws IOException {
        Path classFile = mod.getFile().findResource(resource);
        if (classFile == null || !Files.isRegularFile(classFile)) {
            return false;
        }
        ClassNode node = new ClassNode();
        try (InputStream input = Files.newInputStream(classFile)) {
            new ClassReader(input).accept(
                    node,
                    ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        if (node.methods == null) {
            return false;
        }
        for (MethodNode method : node.methods) {
            if (methodName.equals(method.name)) {
                return true;
            }
        }
        return false;
    }
}

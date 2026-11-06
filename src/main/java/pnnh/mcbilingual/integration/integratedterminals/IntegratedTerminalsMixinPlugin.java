package pnnh.mcbilingual.integration.integratedterminals;

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
 * Applies each storage-terminal mixin only when that handler still has
 * {@code getInstanceFilterPredicate}. The config mode is read later.
 */
public final class IntegratedTerminalsMixinPlugin implements IMixinConfigPlugin {
    private static final String[] ITEM_HANDLER = {
            "org", "cyclops", "integratedterminals", "capability", "ingredient",
            "IngredientComponentTerminalStorageHandlerItemStack.class"
    };
    private static final String[] FLUID_HANDLER = {
            "org", "cyclops", "integratedterminals", "capability", "ingredient",
            "IngredientComponentTerminalStorageHandlerFluidStack.class"
    };

    private boolean itemApply;
    private boolean fluidApply;

    @Override
    public void onLoad(String mixinPackage) {
        itemApply = false;
        fluidApply = false;
        try {
            if (RivalMods.isBlocked()) {
                return;
            }
            LoadingModList mods = LoadingModList.get();
            if (mods == null) {
                return;
            }
            ModFileInfo terminals = mods.getModFileById("integratedterminals");
            if (terminals == null) {
                return;
            }
            itemApply = hasMethod(terminals, "getInstanceFilterPredicate", ITEM_HANDLER);
            fluidApply = hasMethod(terminals, "getInstanceFilterPredicate", FLUID_HANDLER);
            if (!itemApply || !fluidApply) {
                IntegrationGuard.fail(
                        "integratedterminals",
                        "找不到 getInstanceFilterPredicate。 getInstanceFilterPredicate is missing.");
            }
        } catch (Exception | LinkageError exception) {
            itemApply = false;
            fluidApply = false;
            IntegrationGuard.fail("integratedterminals", exception);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String simple = simpleName(mixinClassName);
        if ("TerminalItemFilterMixin".equals(simple)) {
            return itemApply;
        }
        if ("TerminalFluidFilterMixin".equals(simple)) {
            return fluidApply;
        }
        return false;
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

    private static String simpleName(String mixinClassName) {
        if (mixinClassName == null || mixinClassName.isEmpty()) {
            return "";
        }
        String name = mixinClassName.replace('/', '.');
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(dot + 1);
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

package pnnh.mcbilingual.integration.jei;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;

import pnnh.mcbilingual.compat.IntegrationGuard;
import pnnh.mcbilingual.compat.RivalMods;
import pnnh.mcbilingual.diagnostics.LoadReport;

/**
 * Applies one JEI search mixin for the installed version.
 * Below 19.56.0 the three-argument constructor is used. From 19.56.0 the four-argument
 * constructor is used. The other mixin stays unapplied, so its missing descriptor cannot crash startup.
 */
public final class JeiMixinPlugin implements IMixinConfigPlugin {
    private static volatile boolean enabled;

    private JeiVersions.Line line = JeiVersions.Line.UNKNOWN;
    private boolean overlay;

    public static boolean enabled() {
        return enabled;
    }

    @Override
    public void onLoad(String mixinPackage) {
        line = JeiVersions.Line.UNKNOWN;
        enabled = false;
        overlay = false;
        try {
            loadJei();
        } catch (Exception | LinkageError exception) {
            line = JeiVersions.Line.UNKNOWN;
            enabled = false;
            overlay = false;
            IntegrationGuard.fail("jei", exception);
        }
    }

    private void loadJei() {
        if (RivalMods.isBlocked()) {
            return;
        }
        LoadingModList mods = LoadingModList.get();
        if (mods == null) {
            LoadReport.jeiModListMissing();
            return;
        }
        ModFileInfo jei = mods.getModFileById("jei");
        if (jei == null) {
            LoadReport.jeiAbsent();
            return;
        }
        overlay = true;
        String version = versionOf(jei);
        JeiVersions.Line parsed = JeiVersions.classify(version);
        JeiLineChoice.Choice choice;
        Throwable unread = null;
        try {
            choice = JeiLineChoice.choose(parsed, inspect(jei));
        } catch (RuntimeException | IOException | LinkageError exception) {
            choice = JeiLineChoice.choose(parsed, null);
            unread = exception;
        }
        line = choice.line();
        enabled = choice.applies();
        LoadReport.jeiSearchSelected(
                JeiLineChoice.message(version, choice),
                choice.applies(),
                choice.source() == JeiLineChoice.Source.VERSION,
                unread);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (isOverlayMixin(mixinClassName)) {
            return overlay;
        }
        return JeiLineChoice.applies(line, mixinClassName);
    }

    private static boolean isOverlayMixin(String mixinClassName) {
        if (mixinClassName == null || mixinClassName.isEmpty()) {
            return false;
        }
        String name = mixinClassName.replace('/', '.');
        int dot = name.lastIndexOf('.');
        String simple = dot < 0 ? name : name.substring(dot + 1);
        return "JeiOverlayOwnerMixin".equals(simple);
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
        LoadReport.jeiMixinApplied(targetClassName);
    }

    private static JeiListElementShape.Shape inspect(ModFileInfo jei) throws IOException {
        Path classFile = jei.getFile().findResource("mezz", "jei", "gui", "ingredients", "ListElementInfo.class");
        if (classFile == null || !Files.isRegularFile(classFile)) {
            return null;
        }
        ClassNode node = new ClassNode();
        try (InputStream input = Files.newInputStream(classFile)) {
            new ClassReader(input).accept(
                    node,
                    ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        List<JeiListElementShape.Member> fields = new ArrayList<>();
        if (node.fields != null) {
            for (FieldNode field : node.fields) {
                fields.add(new JeiListElementShape.Member(field.name, field.desc));
            }
        }
        List<JeiListElementShape.Member> methods = new ArrayList<>();
        if (node.methods != null) {
            for (MethodNode method : node.methods) {
                methods.add(new JeiListElementShape.Member(method.name, method.desc));
            }
        }
        return JeiListElementShape.scan(fields, methods);
    }

    private static String versionOf(ModFileInfo jei) {
        try {
            String version = jei.versionString();
            if (version == null || version.isBlank()) {
                return "unknown";
            }
            return version;
        } catch (RuntimeException | LinkageError exception) {
            return "unknown";
        }
    }
}

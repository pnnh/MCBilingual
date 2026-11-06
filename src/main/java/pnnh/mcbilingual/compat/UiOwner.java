package pnnh.mcbilingual.compat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;

/**
 * Which mod is drawing the current screen or overlay.
 * The draw stack is cleared on the outermost frame so a thrown overlay cannot stick.
 */
public final class UiOwner {
    private static final ArrayDeque<String> DRAW_STACK = new ArrayDeque<>();
    private static final ConcurrentHashMap<Class<?>, Optional<String>> SCREEN_IDS = new ConcurrentHashMap<>();

    private UiOwner() {
    }

    public static void push(String modId) {
        String id = CompatList.id(modId);
        if (!id.isEmpty()) {
            DRAW_STACK.push(id);
        }
    }

    public static void pop(String modId) {
        String id = CompatList.id(modId);
        if (id.isEmpty() || DRAW_STACK.isEmpty()) {
            return;
        }
        if (id.equals(DRAW_STACK.peek())) {
            DRAW_STACK.pop();
        }
    }

    /**
     * Clears only the draw stack. Call this when a frame's depth goes from 0 to 1.
     */
    public static void beginFrame() {
        DRAW_STACK.clear();
    }

    public static List<String> owners(Class<?> screenClass) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        String screen = screenOwner(screenClass);
        if (screen != null) {
            ids.add(screen);
        }
        ids.addAll(DRAW_STACK);
        return List.copyOf(ids);
    }

    public static String screenOwner(Class<?> type) {
        if (type == null) {
            return null;
        }
        Optional<String> cached = SCREEN_IDS.get(type);
        if (cached != null) {
            return cached.orElse(null);
        }
        try {
            String found = lookup(type);
            SCREEN_IDS.put(type, Optional.ofNullable(found));
            return found;
        } catch (Exception | LinkageError exception) {
            return null;
        }
    }

    private static String lookup(Class<?> type) {
        String className = type.getName();
        if (className.startsWith("net.minecraft.")) {
            return null;
        }
        String known = knownPrefix(className);
        if (known != null) {
            return known;
        }
        ModList mods = ModList.get();
        if (mods == null) {
            return null;
        }
        String[] parts = resourceParts(className);
        for (IModFileInfo info : mods.getModFiles()) {
            if (info == null) {
                continue;
            }
            IModFile file = info.getFile();
            if (file == null) {
                continue;
            }
            Path path = file.findResource(parts);
            if (path == null || !Files.exists(path) || Files.isDirectory(path)) {
                continue;
            }
            for (IModInfo mod : info.getMods()) {
                if (mod == null) {
                    continue;
                }
                String id = CompatList.id(mod.getModId());
                if (!id.isEmpty()) {
                    return id;
                }
            }
        }
        return null;
    }

    private static String knownPrefix(String className) {
        if (className.startsWith("mezz.jei.")) {
            return "jei";
        }
        if (className.startsWith("snownee.jade.")) {
            return "jade";
        }
        if (className.startsWith("appeng.")) {
            return "ae2";
        }
        if (className.startsWith("org.cyclops.integratedterminals.")) {
            return "integratedterminals";
        }
        return null;
    }

    private static String[] resourceParts(String className) {
        String[] split = className.replace('.', '/').split("/");
        split[split.length - 1] = split[split.length - 1] + ".class";
        return split;
    }
}

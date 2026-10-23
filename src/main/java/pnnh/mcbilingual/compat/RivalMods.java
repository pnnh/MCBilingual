package pnnh.mcbilingual.compat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

/**
 * Language mods that also rewrite tooltips, search, or the client language.
 * Translation packs are not rivals. When one is present this mod stands down.
 */
public final class RivalMods {
    static final List<String> RIVALS = List.of("polyglottooltip", "languagereload");

    private static volatile List<String> present;

    private RivalMods() {
    }

    public static List<String> findPresent(Collection<String> loadedModIds) {
        if (loadedModIds == null || loadedModIds.isEmpty()) {
            return List.of();
        }
        List<String> found = new ArrayList<>();
        for (String rival : RIVALS) {
            if (loadedModIds.contains(rival)) {
                found.add(rival);
            }
        }
        return List.copyOf(found);
    }

    public static boolean isBlocked() {
        return !presentIds().isEmpty();
    }

    public static List<String> presentIds() {
        List<String> cached = present;
        if (cached != null) {
            return cached;
        }
        List<String> loaded = new ArrayList<>();
        LoadingModList mods = LoadingModList.get();
        if (mods != null) {
            for (ModInfo info : mods.getMods()) {
                loaded.add(info.getModId());
            }
        }
        cached = findPresent(loaded);
        present = cached;
        return cached;
    }

    public static String message() {
        String mods = String.join(", ", presentIds());
        return "MCBilingual 已停用，因为同时安装了 " + mods
                + "。为避免冲突，本模组不再修改提示、搜索或语言。"
                + " MCBilingual is disabled because " + mods
                + " is also installed. It will not change tooltips, search, or language.";
    }
}

package pnnh.mcbilingual.switchlang;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.locale.Language;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import pnnh.mcbilingual.language.LanguageTexts;
import pnnh.mcbilingual.switchlang.mixin.AdvancementsScreenAccessor;
import pnnh.mcbilingual.switchlang.mixin.BookViewScreenAccessor;
import pnnh.mcbilingual.switchlang.mixin.I18nLanguageSetter;
import pnnh.mcbilingual.switchlang.mixin.ScreenRebuild;
import pnnh.mcbilingual.switchlang.mixin.SignTextAccessor;
import pnnh.mcbilingual.switchlang.mixin.TextDisplayAccessor;

/**
 * Installs a language table that {@link LanguageTexts} already loaded.
 * Does not re-read resource packs.
 */
public final class LanguageSwitch {
    private static final Logger LOGGER = LogUtils.getLogger();

    private LanguageSwitch() {
    }

    public static boolean trySwitch(Minecraft minecraft) {
        if (minecraft == null || minecraft.options == null || !LanguageTexts.get().ready()) {
            return false;
        }
        String current = minecraft.options.languageCode;
        String next = LanguageCycle.next(current);
        if (!LanguageTexts.get().hasUsableText(next)) {
            LOGGER.info("Not switching language from {} to {}: target text is unavailable", current, next);
            return false;
        }
        ClientLanguage language = LanguageTexts.get().clientLanguage(next);
        if (language == null) {
            return false;
        }
        install(minecraft, next, language);
        refresh(minecraft);
        LOGGER.info("Switched client language from {} to {}", current, next);
        return true;
    }

    private static void install(Minecraft minecraft, String code, ClientLanguage language) {
        minecraft.getLanguageManager().setSelected(code);
        I18nLanguageSetter.mcbilingual$setLanguage(language);
        Language.inject(language);
        minecraft.options.languageCode = code;
        minecraft.options.save();
    }

    private static void refresh(Minecraft minecraft) {
        runRefresh("window title", minecraft::updateTitle);
        if (minecraft.gui != null) {
            runRefresh("chat", () -> minecraft.gui.getChat().rescaleChat());
        }
        runRefresh("open screen", () -> refreshOpenScreen(minecraft));
        runRefresh("signs", () -> refreshSigns(minecraft));
        runRefresh("text displays", () -> refreshTextDisplays(minecraft));
        if (minecraft.player != null && minecraft.player.connection != null) {
            runRefresh("search trees", () -> minecraft.player.connection.updateSearchTrees());
        }
    }

    private static void refreshOpenScreen(Minecraft minecraft) {
        Screen screen = minecraft.screen;
        if (screen instanceof AdvancementsScreen advancementsScreen) {
            refreshAdvancements(advancementsScreen);
            return;
        }
        if (screen instanceof BookViewScreen) {
            ((BookViewScreenAccessor) screen).mcbilingual$setCachedPage(-1);
        }
        if (screen instanceof PauseScreen
                || screen instanceof InventoryScreen
                || screen instanceof CreativeModeInventoryScreen
                || screen instanceof BookViewScreen) {
            ((ScreenRebuild) screen).mcbilingual$rebuildWidgets();
        }
    }

    private static void refreshAdvancements(AdvancementsScreen screen) {
        AdvancementsScreenAccessor access = (AdvancementsScreenAccessor) screen;
        AdvancementTab selected = access.mcbilingual$selectedTab();
        AdvancementHolder holder = selected == null ? null : selected.getRootNode().holder();
        access.mcbilingual$tabs().clear();
        access.mcbilingual$setSelectedTab(null);
        ClientAdvancements advancements = access.mcbilingual$advancements();
        advancements.setListener(screen);
        if (holder != null) {
            advancements.setSelectedTab(holder, true);
        } else if (!access.mcbilingual$tabs().isEmpty()) {
            AdvancementTab first = access.mcbilingual$tabs().values().iterator().next();
            advancements.setSelectedTab(first.getRootNode().holder(), true);
        }
    }

    private static void refreshSigns(Minecraft minecraft) {
        if (minecraft.level == null || !(minecraft.level.getChunkSource() instanceof ClientChunkCache cache)) {
            return;
        }
        ClientChunkCache.Storage storage = cache.storage;
        if (storage == null) {
            return;
        }
        AtomicReferenceArray<LevelChunk> chunks = storage.chunks;
        for (int i = 0; i < chunks.length(); i++) {
            LevelChunk chunk = chunks.get(i);
            if (chunk == null) {
                continue;
            }
            for (Map.Entry<?, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                if (entry.getValue() instanceof SignBlockEntity sign) {
                    ((SignTextAccessor) sign.getFrontText()).mcbilingual$setRenderMessages(null);
                    ((SignTextAccessor) sign.getBackText()).mcbilingual$setRenderMessages(null);
                }
            }
        }
    }

    private static void refreshTextDisplays(Minecraft minecraft) {
        if (minecraft.level == null) {
            return;
        }
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof Display.TextDisplay textDisplay) {
                ((TextDisplayAccessor) textDisplay).mcbilingual$setClientDisplayCache(null);
            }
        }
    }

    private static void runRefresh(String surface, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            LOGGER.error("Could not refresh {} after a language switch", surface, exception);
        }
    }
}

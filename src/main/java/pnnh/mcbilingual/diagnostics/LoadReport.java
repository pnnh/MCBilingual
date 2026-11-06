package pnnh.mcbilingual.diagnostics;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client log lines for startup and JEI search. Mixin loading must not touch
 * Minecraft classes, so this type stays on SLF4J only.
 */
public final class LoadReport {
    private static final Logger LOGGER = LoggerFactory.getLogger("pnnh.mcbilingual.MCBilingual");
    private static final AtomicBoolean jeiHookLogged = new AtomicBoolean();
    private static final AtomicInteger jeiSearchFailures = new AtomicInteger();

    private LoadReport() {
    }

    public static void modInitialized() {
        LOGGER.info("MCBilingual client mod loaded");
    }

    public static void modInitFailed(Throwable error) {
        LOGGER.error(
                "MCBilingual 初始化失败，提示、搜索和语言切换不会生效。请把这条日志发给作者排查。"
                        + " MCBilingual failed to initialize. Tooltips, search, and language switching will not work."
                        + " Send this log when asking for help.",
                error);
    }

    public static void jeiAbsent() {
        LOGGER.info("JEI is not installed. MCBilingual will not change JEI search.");
    }

    public static void jeiModListMissing() {
        LOGGER.error(
                "MCBilingual 读不到模组列表，已跳过 JEI 搜索注入，客户端会继续启动。"
                        + " MCBilingual could not read the mod list and skipped JEI search injection.");
    }

    /**
     * @param verifiedMatch true when the parsed version and the class file select the same constructor
     */
    public static void jeiSearchSelected(String message, boolean applying, boolean verifiedMatch, Throwable error) {
        if (!applying) {
            logAtError(message, error);
            return;
        }
        if (verifiedMatch && error == null) {
            LOGGER.info(message);
            return;
        }
        logAtWarn(message, error);
    }

    private static void logAtError(String message, Throwable error) {
        if (error == null) {
            LOGGER.error(message);
        } else {
            LOGGER.error(message, error);
        }
    }

    private static void logAtWarn(String message, Throwable error) {
        if (error == null) {
            LOGGER.warn(message);
        } else {
            LOGGER.warn(message, error);
        }
    }

    public static void jeiMixinApplied(String targetClassName) {
        LOGGER.info("MCBilingual applied the JEI search mixin to {}", targetClassName);
    }

    public static void jeiHookRan(int indexedItems) {
        if (jeiHookLogged.compareAndSet(false, true)) {
            LOGGER.info("MCBilingual JEI search hook ran. Indexed items={}", indexedItems);
        }
    }

    public static void jeiSearchFailed(Throwable error) {
        int count = jeiSearchFailures.incrementAndGet();
        if (count == 1) {
            LOGGER.error(
                    "MCBilingual 给 JEI 物品追加搜索词失败，游戏会继续运行，该物品可能无法用另一种语言或拼音搜到。"
                            + " MCBilingual failed to add JEI search terms. The game will continue."
                            + " That ingredient may not match the other language or pinyin.",
                    error);
            return;
        }
        if (count == 20 || count % 500 == 0) {
            LOGGER.error("MCBilingual JEI search term failures so far: {}", count);
        }
    }

    public static void jeiRuntimeWithoutMixin() {
        LOGGER.warn(
                "JEI 已启动，但 MCBilingual 没有注入搜索词。JEI 里不能用另一种语言或拼音搜索，请向上翻找 ListElementInfo 那条日志。"
                        + " JEI is running, but MCBilingual did not inject search terms.");
    }
}

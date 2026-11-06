package pnnh.mcbilingual.diagnostics;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client log lines for startup and JEI search. Mixin loading must not touch
 * Minecraft classes, so this type stays on SLF4J only.
 */
public final class LoadReport {
    private static final Logger LOGGER = LoggerFactory.getLogger("pnnh.mcbilingual.MCBilingual");
    private static final AtomicBoolean jeiHookLogged = new AtomicBoolean();

    private LoadReport() {
    }

    public static void modInitialized() {
        LOGGER.info("MCBilingual client mod loaded");
    }

    public static void modInitFailed(Throwable error) {
        String message = "MCBilingual 初始化失败，提示、搜索和语言切换不会生效。请把这条日志发给作者排查。"
                + " MCBilingual failed to initialize. Tooltips, search, and language switching will not work."
                + " Send this log when asking for help.";
        logAtError(message, error);
    }

    public static void integrationFailed(String modId, Throwable error) {
        String reason = error == null || error.getMessage() == null || error.getMessage().isBlank()
                ? "unknown"
                : error.getMessage();
        integrationFailed(modId, reason, error);
    }

    public static void integrationFailed(String modId, String reason) {
        integrationFailed(modId, reason, null);
    }

    private static void integrationFailed(String modId, String reason, Throwable error) {
        String why = reason == null || reason.isBlank() ? "unknown" : reason;
        String id = modId == null || modId.isBlank() ? "unknown" : modId;
        logAtError(
                "MCBilingual 跳过 " + id + " 的专用兼容：" + why
                        + "。原版功能和其它模组继续可用。"
                        + " MCBilingual skipped dedicated compatibility for " + id + ": " + why
                        + ". Vanilla features and other mods keep working.",
                error);
    }

    public static void compatInvalid(String modId, String raw) {
        String id = modId == null || modId.isBlank() ? "unknown" : modId;
        String shown = raw == null ? "" : raw;
        LOGGER.warn(
                "MCBilingual 忽略无效的兼容模式，{} 按 Full 运行。原文：{}。"
                        + " MCBilingual ignored an invalid compatibility mode. {} stays Full. Raw entry: {}.",
                id,
                shown,
                id,
                shown);
    }

    public static void compatDuplicate(String modId, String raw) {
        String id = modId == null || modId.isBlank() ? "unknown" : modId;
        String shown = raw == null ? "" : raw;
        LOGGER.warn(
                "MCBilingual 兼容列表里 {} 出现多次，采用后写的一项。原文：{}。"
                        + " MCBilingual saw {} more than once and kept the later entry. Raw entry: {}.",
                id,
                shown,
                id,
                shown);
    }

    public static void compatReloadFailed(Throwable error) {
        logAtError(
                "MCBilingual 没能重新读取兼容列表，仍使用上一份。"
                        + " MCBilingual could not reload the compatibility list and kept the previous one.",
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

    public static void jeiRuntimeWithoutMixin() {
        LOGGER.warn(
                "JEI 已启动，但 MCBilingual 没有注入搜索词。JEI 里不能用另一种语言或拼音搜索，请向上翻找 ListElementInfo 那条日志。"
                        + " JEI is running, but MCBilingual did not inject search terms.");
    }
}

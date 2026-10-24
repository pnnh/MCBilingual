package pnnh.mcbilingual.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-only settings. Extended translation talks to a local Ollama process;
 * the mod loads without that process.
 */
public final class ClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue EXTENDED_TRANSLATION;
    public static final ModConfigSpec.IntValue REQUEST_TIMEOUT_MS;
    public static final ModConfigSpec.IntValue VISION_TIMEOUT_MS;
    public static final ModConfigSpec.ConfigValue<String> OLLAMA_BASE_URL;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("extended_translation");
        EXTENDED_TRANSLATION = builder
                .comment("Allow local Ollama machine translation when the dictionary or language pack has no entry.")
                .translation("mcbilingual.config.extended_translation")
                .define("enabled", true);
        REQUEST_TIMEOUT_MS = builder
                .comment("HTTP timeout for Ollama text requests in milliseconds.")
                .translation("mcbilingual.config.request_timeout_ms")
                .defineInRange("requestTimeoutMs", 12000, 1000, 120000);
        VISION_TIMEOUT_MS = builder
                .comment("HTTP timeout for Ollama vision (qwen2.5vl) requests in milliseconds. Usually longer than text.")
                .translation("mcbilingual.config.vision_timeout_ms")
                .defineInRange("visionTimeoutMs", 60000, 1000, 300000);
        OLLAMA_BASE_URL = builder
                .comment("Local Ollama HTTP base URL. Must stay on the player machine.")
                .translation("mcbilingual.config.ollama_base_url")
                .define("ollamaBaseUrl", "http://127.0.0.1:11434");
        builder.pop();
        SPEC = builder.build();
    }

    private ClientConfig() {
    }

    public static boolean extendedTranslation() {
        try {
            return EXTENDED_TRANSLATION.get();
        } catch (RuntimeException ignored) {
            return true;
        }
    }

    public static int requestTimeoutMs() {
        try {
            return REQUEST_TIMEOUT_MS.get();
        } catch (RuntimeException ignored) {
            return 12000;
        }
    }

    public static int visionTimeoutMs() {
        try {
            return VISION_TIMEOUT_MS.get();
        } catch (RuntimeException ignored) {
            return 60000;
        }
    }

    public static String ollamaBaseUrl() {
        try {
            String url = OLLAMA_BASE_URL.get();
            return url == null || url.isBlank() ? "http://127.0.0.1:11434" : url.strip();
        } catch (RuntimeException ignored) {
            return "http://127.0.0.1:11434";
        }
    }
}

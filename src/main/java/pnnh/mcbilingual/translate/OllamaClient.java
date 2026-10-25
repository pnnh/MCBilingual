package pnnh.mcbilingual.translate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pnnh.mcbilingual.translate.MachineTranslationCache.Kind;
import pnnh.mcbilingual.translate.MachineTranslationCache.Job;

/**
 * Async HTTP client for a local Ollama instance. Fixed model ids.
 * Text models never receive images; vision uses {@link #VISION_MODEL} only.
 */
public final class OllamaClient {
    public static final String WORD_MODEL = "qwen2.5:1.5b";
    public static final String SENTENCE_MODEL = "qwen2.5:3b";
    public static final String VISION_MODEL = "qwen2.5vl:3b";

    private final MachineTranslationCache cache;
    private final Supplier<String> baseUrl;
    private final IntSupplier timeoutMs;
    private final IntSupplier visionTimeoutMs;
    private final BooleanSupplier enabled;
    private final HttpClient http;
    private final ExecutorService executor;

    public OllamaClient(
            MachineTranslationCache cache,
            Supplier<String> baseUrl,
            IntSupplier timeoutMs,
            BooleanSupplier enabled) {
        this(cache, baseUrl, timeoutMs, timeoutMs, enabled);
    }

    public OllamaClient(
            MachineTranslationCache cache,
            Supplier<String> baseUrl,
            IntSupplier timeoutMs,
            IntSupplier visionTimeoutMs,
            BooleanSupplier enabled) {
        this.cache = cache;
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
        this.visionTimeoutMs = visionTimeoutMs == null ? timeoutMs : visionTimeoutMs;
        this.enabled = enabled;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "mcbilingual-ollama");
            thread.setDaemon(true);
            return thread;
        });
    }

    public MachineTranslationCache cache() {
        return cache;
    }

    /**
     * Returns a cached job, or starts a request when extended translation is on.
     * When disabled, returns empty so callers show no MT error.
     * {@link Kind#VISION} is rejected here — use {@link #requestVision}.
     */
    public java.util.Optional<Job> request(Kind kind, String source) {
        if (kind == Kind.VISION) {
            return java.util.Optional.empty();
        }
        if (source == null || source.isBlank()) {
            return java.util.Optional.empty();
        }
        java.util.Optional<Job> existing = cache.get(kind, source);
        if (existing.isPresent()) {
            return existing;
        }
        if (!enabled.getAsBoolean()) {
            return java.util.Optional.empty();
        }
        if (!cache.putIfAbsentPending(kind, source)) {
            return cache.get(kind, source);
        }
        executor.execute(() -> runText(kind, source));
        return cache.get(kind, source);
    }

    /**
     * Vision fallback: {@code cacheKey} from {@link VisionRoi}; PNG/JPEG bytes.
     * Never uses WORD/SENTENCE text models.
     */
    public java.util.Optional<Job> requestVision(String cacheKey, byte[] imageBytes) {
        if (cacheKey == null || cacheKey.isBlank() || imageBytes == null || imageBytes.length == 0) {
            return java.util.Optional.empty();
        }
        java.util.Optional<Job> existing = cache.get(Kind.VISION, cacheKey);
        if (existing.isPresent()) {
            return existing;
        }
        if (!enabled.getAsBoolean()) {
            return java.util.Optional.empty();
        }
        if (!cache.putIfAbsentPending(Kind.VISION, cacheKey)) {
            return cache.get(Kind.VISION, cacheKey);
        }
        byte[] copy = imageBytes.clone();
        executor.execute(() -> runVision(cacheKey, copy));
        return cache.get(Kind.VISION, cacheKey);
    }

    private void runText(Kind kind, String source) {
        int timeout = Math.max(1000, timeoutMs.getAsInt());
        String model = kind == Kind.WORD ? WORD_MODEL : SENTENCE_MODEL;
        String prompt = kind == Kind.WORD
                ? "You are an English-Chinese learner dictionary. For the English word below, reply in Simplified Chinese only "
                        + "(part of speech + short senses). Do not repeat the English word alone. Word: "
                        + source
                : "Translate this English text to Simplified Chinese. Reply with only the translation: " + source;
        try {
            JsonObject body = new JsonObject();
            body.addProperty("model", model);
            body.addProperty("prompt", prompt);
            body.addProperty("stream", false);
            HttpResponse<String> response = postGenerate(body, timeout);
            finishFromResponse(kind, source, response);
        } catch (java.net.http.HttpTimeoutException timeoutException) {
            cache.put(Job.timeout(kind, source));
        } catch (Exception exception) {
            cache.put(Job.failed(kind, source, exception.getClass().getSimpleName()));
        }
    }

    private void runVision(String cacheKey, byte[] imageBytes) {
        int timeout = Math.max(1000, visionTimeoutMs.getAsInt());
        String prompt = "Read the text visible in this Minecraft UI screenshot crop. "
                + "Translate it to Simplified Chinese as one sentence or a short paragraph. "
                + "Reply with only the translation, no quotes or explanation.";
        try {
            JsonObject body = new JsonObject();
            body.addProperty("model", VISION_MODEL);
            body.addProperty("prompt", prompt);
            body.addProperty("stream", false);
            JsonArray images = new JsonArray();
            images.add(Base64.getEncoder().encodeToString(imageBytes));
            body.add("images", images);
            HttpResponse<String> response = postGenerate(body, timeout);
            finishFromResponse(Kind.VISION, cacheKey, response);
        } catch (java.net.http.HttpTimeoutException timeoutException) {
            cache.put(Job.timeout(Kind.VISION, cacheKey));
        } catch (Exception exception) {
            cache.put(Job.failed(Kind.VISION, cacheKey, exception.getClass().getSimpleName()));
        }
    }

    private HttpResponse<String> postGenerate(JsonObject body, int timeoutMs) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(baseUrl.get()) + "/api/generate"))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private void finishFromResponse(Kind kind, String source, HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            cache.put(Job.failed(kind, source, "HTTP " + response.statusCode()));
            return;
        }
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        String out = json.has("response") ? json.get("response").getAsString() : "";
        if (out == null || out.isBlank()) {
            cache.put(Job.failed(kind, source, "empty"));
            return;
        }
        String stripped = out.strip();
        // Small word models often echo the English token; never treat that as a gloss.
        if (kind == Kind.WORD && source != null && stripped.equalsIgnoreCase(source.strip())) {
            cache.put(Job.failed(kind, source, "echo"));
            return;
        }
        cache.put(Job.ok(kind, source, stripped));
    }

    private static String trimSlash(String url) {
        if (url == null || url.isBlank()) {
            return "http://127.0.0.1:11434";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}

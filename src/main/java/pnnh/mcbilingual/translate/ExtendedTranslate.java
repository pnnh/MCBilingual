package pnnh.mcbilingual.translate;

import pnnh.mcbilingual.MCBilingual;
import pnnh.mcbilingual.config.ClientConfig;
import pnnh.mcbilingual.dictionary.LearnerDictionary;

/**
 * Lazily holds the shared dictionary and Ollama client for freeze lookup.
 */
public final class ExtendedTranslate {
    private static volatile LearnerDictionary dictionary;
    private static volatile OllamaClient ollama;

    private ExtendedTranslate() {
    }

    public static LearnerDictionary dictionary() {
        LearnerDictionary local = dictionary;
        if (local == null) {
            synchronized (ExtendedTranslate.class) {
                local = dictionary;
                if (local == null) {
                    try {
                        local = LearnerDictionary.loadClasspath(LearnerDictionary.DEFAULT_RESOURCE);
                        MCBilingual.LOGGER.info("Loaded learner dictionary with {} entries", local.size());
                    } catch (Exception exception) {
                        MCBilingual.LOGGER.warn("Learner dictionary missing; word lookup will use Ollama only", exception);
                        local = new LearnerDictionary(java.util.Map.of());
                    }
                    dictionary = local;
                }
            }
        }
        return local;
    }

    public static OllamaClient ollama() {
        OllamaClient local = ollama;
        if (local == null) {
            synchronized (ExtendedTranslate.class) {
                local = ollama;
                if (local == null) {
                    local = new OllamaClient(
                            new MachineTranslationCache(),
                            ClientConfig::ollamaBaseUrl,
                            ClientConfig::requestTimeoutMs,
                            ClientConfig::visionTimeoutMs,
                            ClientConfig::extendedTranslation);
                    ollama = local;
                }
            }
        }
        return local;
    }
}

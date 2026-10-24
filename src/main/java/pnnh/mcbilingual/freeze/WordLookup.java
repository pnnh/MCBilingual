package pnnh.mcbilingual.freeze;

import java.util.Optional;
import java.util.function.ToIntFunction;

import pnnh.mcbilingual.config.ClientConfig;
import pnnh.mcbilingual.translate.EnglishWords;
import pnnh.mcbilingual.translate.EnglishWords.WordSpan;
import pnnh.mcbilingual.translate.ExtendedTranslate;
import pnnh.mcbilingual.translate.MachineTranslationCache.Kind;
import pnnh.mcbilingual.translate.MachineTranslationCache.Job;

/**
 * Word-mode hint: dictionary first, then optional Ollama. Never returns pack unit text.
 * Never starts vision requests.
 */
public final class WordLookup {
    public enum HintKind {
        GLOSS,
        TEXT_LOADING,
        IMAGE_LOADING,
        TEXT_FAILED,
        IMAGE_FAILED,
        TEXT_TIMEOUT,
        IMAGE_TIMEOUT,
        NONE
    }

    public record Hint(HintKind kind, String text) {
        public static Hint gloss(String text) {
            return new Hint(HintKind.GLOSS, text);
        }

        public static Hint none() {
            return new Hint(HintKind.NONE, "");
        }
    }

    private WordLookup() {
    }

    public static Hint resolve(String visible, int localPixelX, ToIntFunction<String> widthOf) {
        if (visible == null || visible.isBlank()) {
            return Hint.none();
        }
        Optional<WordSpan> word = EnglishWords.atPixel(visible, localPixelX, widthOf);
        if (word.isEmpty()) {
            return Hint.none();
        }
        String token = word.get().word();
        Optional<String> gloss = ExtendedTranslate.dictionary().lookup(token);
        if (gloss.isPresent()) {
            return Hint.gloss(gloss.get());
        }
        if (!ClientConfig.extendedTranslation()) {
            return Hint.none();
        }
        Optional<Job> job = ExtendedTranslate.ollama().request(Kind.WORD, token);
        if (job.isEmpty()) {
            return Hint.none();
        }
        return fromJob(job.get());
    }

    static Hint fromJob(Job job) {
        HintKind kind = LookupStatusKeys.hintKind(job.kind(), job.status());
        return switch (job.status()) {
            case PENDING, FAILED, TIMEOUT -> new Hint(kind, job.error() == null ? "" : job.error());
            case OK -> Hint.gloss(job.result() == null ? "" : job.result());
        };
    }
}

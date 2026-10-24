package pnnh.mcbilingual.freeze;

import java.util.Optional;

import pnnh.mcbilingual.config.ClientConfig;
import pnnh.mcbilingual.translate.ExtendedTranslate;
import pnnh.mcbilingual.translate.MachineTranslationCache.Kind;
import pnnh.mcbilingual.translate.MachineTranslationCache.Job;
import pnnh.mcbilingual.translate.SentenceBounds;
import pnnh.mcbilingual.freeze.WordLookup.Hint;

/**
 * Unit mode: pack counterpart full text, else one punctuated sentence via Ollama.
 */
public final class UnitLookup {
    private UnitLookup() {
    }

    public static Hint resolve(UiSentence sentence, int charOffset) {
        if (sentence == null) {
            return Hint.none();
        }
        String counterpart = sentence.counterpart();
        if (counterpart != null && !counterpart.isBlank()) {
            return Hint.gloss(counterpart);
        }
        String visible = sentence.visible();
        if (visible == null || visible.isBlank()) {
            return Hint.none();
        }
        String piece = SentenceBounds.sentenceAt(visible, charOffset);
        if (piece.isBlank()) {
            return Hint.none();
        }
        if (!ClientConfig.extendedTranslation()) {
            return Hint.none();
        }
        Optional<Job> job = ExtendedTranslate.ollama().request(Kind.SENTENCE, piece);
        if (job.isEmpty()) {
            return Hint.none();
        }
        return WordLookup.fromJob(job.get());
    }
}

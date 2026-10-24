package pnnh.mcbilingual.freeze;

import pnnh.mcbilingual.freeze.WordLookup.HintKind;
import pnnh.mcbilingual.translate.MachineTranslationCache.Kind;
import pnnh.mcbilingual.translate.MachineTranslationCache.Status;

/**
 * Maps MT job status + pipeline to language keys. Pure strings for unit tests.
 */
public final class LookupStatusKeys {
    public static final String TEXT_RECOGNIZING = "mcbilingual.freeze.status.text_recognizing";
    public static final String IMAGE_RECOGNIZING = "mcbilingual.freeze.status.image_recognizing";
    public static final String TEXT_FAILED = "mcbilingual.freeze.status.text_failed";
    public static final String IMAGE_FAILED = "mcbilingual.freeze.status.image_failed";
    public static final String TEXT_TIMEOUT = "mcbilingual.freeze.status.text_timeout";
    public static final String IMAGE_TIMEOUT = "mcbilingual.freeze.status.image_timeout";
    public static final String UNAVAILABLE = "mcbilingual.freeze.unavailable";

    private LookupStatusKeys() {
    }

    public static HintKind hintKind(Kind kind, Status status) {
        boolean vision = kind == Kind.VISION;
        return switch (status) {
            case PENDING -> vision ? HintKind.IMAGE_LOADING : HintKind.TEXT_LOADING;
            case OK -> HintKind.GLOSS;
            case FAILED -> vision ? HintKind.IMAGE_FAILED : HintKind.TEXT_FAILED;
            case TIMEOUT -> vision ? HintKind.IMAGE_TIMEOUT : HintKind.TEXT_TIMEOUT;
        };
    }

    public static String langKey(HintKind kind) {
        return switch (kind) {
            case TEXT_LOADING -> TEXT_RECOGNIZING;
            case IMAGE_LOADING -> IMAGE_RECOGNIZING;
            case TEXT_FAILED -> TEXT_FAILED;
            case IMAGE_FAILED -> IMAGE_FAILED;
            case TEXT_TIMEOUT -> TEXT_TIMEOUT;
            case IMAGE_TIMEOUT -> IMAGE_TIMEOUT;
            case NONE -> UNAVAILABLE;
            case GLOSS -> null;
        };
    }
}

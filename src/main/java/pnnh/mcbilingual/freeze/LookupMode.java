package pnnh.mcbilingual.freeze;

/**
 * Hover behaviour while the UI is frozen.
 */
public enum LookupMode {
    /** English word under the cursor → dictionary / small model. */
    WORD,
    /** Language-pack unit full text, or one punctuated sentence via Ollama. */
    UNIT;

    public LookupMode toggle() {
        return this == WORD ? UNIT : WORD;
    }
}

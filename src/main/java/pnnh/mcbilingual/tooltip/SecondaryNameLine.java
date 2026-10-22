package pnnh.mcbilingual.tooltip;

/**
 * One extra name line. {@code text} is already trimmed and is never the raw translation key.
 */
public record SecondaryNameLine(String language, String text) {
}

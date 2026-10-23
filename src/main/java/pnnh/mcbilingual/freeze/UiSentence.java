package pnnh.mcbilingual.freeze;

/**
 * One game sentence resolved into the other language. The source component
 * stays with the capture code so this type can be tested without Minecraft.
 */
public record UiSentence(String counterpart, String visible) {
}

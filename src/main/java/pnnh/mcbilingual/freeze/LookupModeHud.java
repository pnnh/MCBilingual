package pnnh.mcbilingual.freeze;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Floating mode toggle drawn on the freeze overlay (not captured as text).
 */
public final class LookupModeHud {
    private static final int PAD = 6;
    private static final int MARGIN = 8;
    private static final int BG = 0xC0100010;
    private static final int BG_HOVER = 0xE0302030;

    private static int left;
    private static int top;
    private static int width;
    private static int height;

    private LookupModeHud() {
    }

    public static void paint(GuiGraphics graphics, Font font, int mouseX, int mouseY, int screenWidth) {
        if (font == null) {
            return;
        }
        LookupMode mode = FreezeSession.get().lookupMode();
        String label = Component.translatable(
                mode == LookupMode.WORD
                        ? "mcbilingual.freeze.mode.word"
                        : "mcbilingual.freeze.mode.unit").getString();
        int textW = font.width(label);
        width = textW + PAD * 2;
        height = font.lineHeight + PAD * 2;
        left = screenWidth - width - MARGIN;
        top = MARGIN;
        boolean hover = contains(mouseX, mouseY);
        graphics.fill(left, top, left + width, top + height, hover ? BG_HOVER : BG);
        graphics.drawString(font, label, left + PAD, top + PAD, 0xFFFFFFFF, true);
    }

    public static boolean contains(int mouseX, int mouseY) {
        return mouseX >= left && mouseX < left + width && mouseY >= top && mouseY < top + height;
    }

    public static boolean click(int mouseX, int mouseY) {
        if (!FreezeSession.get().active() || !contains(mouseX, mouseY)) {
            return false;
        }
        FreezeSession.get().toggleLookupMode();
        return true;
    }
}

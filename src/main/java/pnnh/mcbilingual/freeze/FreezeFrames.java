package pnnh.mcbilingual.freeze;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import pnnh.mcbilingual.MCBilingual;
import pnnh.mcbilingual.freeze.WordLookup.Hint;
import pnnh.mcbilingual.freeze.WordLookup.HintKind;
import pnnh.mcbilingual.translate.ExtendedTranslate;

/**
 * Keeps the frame already in the main render target and paints it back while frozen.
 * The hint is a finished literal, so it is not another lookup target.
 */
public final class FreezeFrames {
    private static final ResourceLocation FRAME = ResourceLocation.fromNamespaceAndPath(MCBilingual.MOD_ID, "freeze_frame");
    private static final int HINT_GAP = 12;
    private static final int HINT_PAD = 4;
    private static final int HINT_MARGIN = 4;
    private static final int HINT_COLOR = 0xF0100010;

    private static int imageWidth;
    private static int imageHeight;
    private static NativeImage freezePixels;

    private FreezeFrames() {
    }

    public static void beginRender() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || !FreezeSession.get().pollToggle()) {
            return;
        }
        if (FreezeSession.get().active()) {
            leave(minecraft);
            return;
        }
        enter(minecraft);
    }

    public static void publish() {
        FreezeCapture.publishFrame();
    }

    public static void paint(GuiGraphics graphics) {
        if (!FreezeSession.get().active() || !(FreezeSession.get().picture() instanceof ResourceLocation)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        if (imageWidth > 0 && imageHeight > 0 && screenWidth > 0 && screenHeight > 0) {
            graphics.blit(FRAME, 0, 0, screenWidth, screenHeight, 0.0F, 0.0F, imageWidth, imageHeight, imageWidth, imageHeight);
        }
        if (minecraft.getWindow().getScreenWidth() <= 0 || minecraft.getWindow().getScreenHeight() <= 0) {
            return;
        }
        int mouseX = (int) (minecraft.mouseHandler.xpos() * screenWidth / minecraft.getWindow().getScreenWidth());
        int mouseY = (int) (minecraft.mouseHandler.ypos() * screenHeight / minecraft.getWindow().getScreenHeight());
        LookupModeHud.paint(graphics, minecraft.font, mouseX, mouseY, screenWidth);
        if (LookupModeHud.contains(mouseX, mouseY)) {
            return;
        }
        Optional<TextHit> hit = TextHit.topmost(FreezeSession.get().hits(), mouseX, mouseY);
        Hint hint;
        TextHit anchor = null;
        if (hit.isEmpty()) {
            hint = resolveEmptySpace(mouseX, mouseY, screenWidth, screenHeight);
            if (hint.kind() == HintKind.NONE && FreezeSession.get().lookupMode() == LookupMode.WORD) {
                // WORD + no string: show cannot translate for wait expectation.
                drawHintText(graphics, minecraft.font,
                        Component.translatable(LookupStatusKeys.UNAVAILABLE).getString(),
                        mouseX, mouseY, screenWidth, screenHeight, null);
                return;
            }
        } else {
            anchor = hit.get();
            hint = resolveHint(minecraft.font, anchor, mouseX);
        }
        String text = displayText(hint);
        if (text == null || text.isBlank()) {
            if (hint.kind() == HintKind.NONE) {
                drawHintText(graphics, minecraft.font,
                        Component.translatable(LookupStatusKeys.UNAVAILABLE).getString(),
                        mouseX, mouseY, screenWidth, screenHeight, anchor);
            }
            return;
        }
        drawHintText(graphics, minecraft.font, text, mouseX, mouseY, screenWidth, screenHeight, anchor);
    }

    private static Hint resolveEmptySpace(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (FreezeSession.get().lookupMode() != LookupMode.UNIT) {
            return Hint.none();
        }
        return FreezeVisionCapture.resolve(
                freezePixels,
                FreezeSession.get().frameGeneration(),
                mouseX,
                mouseY,
                screenWidth,
                screenHeight,
                imageWidth,
                imageHeight);
    }

    private static Hint resolveHint(Font font, TextHit hit, int mouseX) {
        UiSentence sentence = hit.sentence();
        if (sentence == null) {
            return Hint.none();
        }
        int localX = Math.max(0, mouseX - hit.x());
        if (FreezeSession.get().lookupMode() == LookupMode.WORD) {
            return WordLookup.resolve(sentence.visible(), localX, font::width);
        }
        int offset = charOffset(font, sentence.visible(), localX);
        return UnitLookup.resolve(sentence, offset);
    }

    private static String displayText(Hint hint) {
        if (hint == null) {
            return null;
        }
        if (hint.kind() == HintKind.GLOSS) {
            return hint.text();
        }
        if (hint.kind() == HintKind.NONE) {
            return null;
        }
        String key = LookupStatusKeys.langKey(hint.kind());
        return key == null ? null : Component.translatable(key).getString();
    }

    private static int charOffset(Font font, String text, int localX) {
        if (text == null || text.isEmpty() || font == null) {
            return 0;
        }
        int acc = 0;
        for (int i = 0; i < text.length(); i++) {
            int w = font.width(text.substring(i, i + 1));
            if (acc + w > localX) {
                return i;
            }
            acc += w;
        }
        return Math.max(0, text.length() - 1);
    }

    private static void enter(Minecraft minecraft) {
        NativeImage image = null;
        boolean registered = false;
        try {
            image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget());
            int width = image.getWidth();
            int height = image.getHeight();
            if (width <= 0 || height <= 0) {
                image.close();
                image = null;
                MCBilingual.LOGGER.warn("Couldn't freeze the frame already on screen");
                return;
            }
            NativeImage keep = copyPixels(image);
            DynamicTexture texture = new DynamicTexture(image);
            image = null;
            minecraft.getTextureManager().register(FRAME, texture);
            registered = true;
            boolean grabbed = minecraft.mouseHandler.isMouseGrabbed();
            if (grabbed) {
                minecraft.mouseHandler.releaseMouse();
            }
            closePixels();
            freezePixels = keep;
            imageWidth = width;
            imageHeight = height;
            FreezeSession.get().activate(FRAME, FreezeCapture.latestHits(), grabbed);
        } catch (RuntimeException exception) {
            if (image != null) {
                image.close();
            }
            if (registered) {
                minecraft.getTextureManager().release(FRAME);
            }
            closePixels();
            imageWidth = 0;
            imageHeight = 0;
            MCBilingual.LOGGER.warn("Couldn't freeze the frame already on screen", exception);
        } finally {
            minecraft.getMainRenderTarget().bindWrite(false);
        }
    }

    private static void leave(Minecraft minecraft) {
        boolean restore = FreezeSession.get().cursorWasGrabbed()
                && minecraft.screen == null
                && minecraft.getOverlay() == null;
        FreezeSession.get().deactivate();
        closePixels();
        imageWidth = 0;
        imageHeight = 0;
        try {
            ExtendedTranslate.ollama().cache().clear();
        } catch (RuntimeException ignored) {
        }
        try {
            minecraft.getTextureManager().release(FRAME);
        } catch (RuntimeException exception) {
            MCBilingual.LOGGER.warn("Couldn't release the frozen frame", exception);
        }
        KeyMapping.setAll();
        if (restore) {
            minecraft.mouseHandler.grabMouse();
        }
    }

    private static void closePixels() {
        if (freezePixels != null) {
            freezePixels.close();
            freezePixels = null;
        }
    }

    private static NativeImage copyPixels(NativeImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        NativeImage copy = new NativeImage(width, height, false);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                copy.setPixelRGBA(x, y, source.getPixelRGBA(x, y));
            }
        }
        return copy;
    }

    private static void drawHintText(
            GuiGraphics graphics,
            Font font,
            String text,
            int mouseX,
            int mouseY,
            int screenWidth,
            int screenHeight,
            TextHit hit) {
        if (text == null || text.isBlank() || font == null) {
            return;
        }
        int maxWidth = Math.max(40, Math.min(280, screenWidth - 8));
        List<FormattedCharSequence> lines = font.split(Component.literal(text), maxWidth);
        if (lines.isEmpty()) {
            return;
        }
        int textWidth = 0;
        for (FormattedCharSequence line : lines) {
            textWidth = Math.max(textWidth, font.width(line));
        }
        int boxWidth = textWidth + HINT_PAD * 2;
        int boxHeight = lines.size() * font.lineHeight + HINT_PAD * 2;
        int x = mouseX + HINT_GAP;
        int y = mouseY + HINT_GAP;
        if (x + boxWidth > screenWidth - HINT_MARGIN) {
            x = mouseX - HINT_GAP - boxWidth;
        }
        if (y + boxHeight > screenHeight - HINT_MARGIN) {
            y = mouseY - HINT_GAP - boxHeight;
        }
        x = place(x, boxWidth, screenWidth);
        y = place(y, boxHeight, screenHeight);
        if (hit != null && overlaps(x, y, boxWidth, boxHeight, hit)) {
            int otherX = place(x >= mouseX ? mouseX - HINT_GAP - boxWidth : mouseX + HINT_GAP, boxWidth, screenWidth);
            int otherY = place(y >= mouseY ? mouseY - HINT_GAP - boxHeight : mouseY + HINT_GAP, boxHeight, screenHeight);
            if (!overlaps(otherX, otherY, boxWidth, boxHeight, hit)) {
                x = otherX;
                y = otherY;
            }
        }
        graphics.fill(x, y, x + boxWidth, y + boxHeight, HINT_COLOR);
        int textY = y + HINT_PAD;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, x + HINT_PAD, textY, 0xFFFFFFFF, true);
            textY += font.lineHeight;
        }
    }

    private static int place(int value, int size, int screen) {
        int max = screen - size - HINT_MARGIN;
        if (max < HINT_MARGIN) {
            return HINT_MARGIN;
        }
        return Mth.clamp(value, HINT_MARGIN, max);
    }

    private static boolean overlaps(int x, int y, int width, int height, TextHit hit) {
        return x < hit.x() + hit.width()
                && x + width > hit.x()
                && y < hit.y() + hit.height()
                && y + height > hit.y();
    }
}

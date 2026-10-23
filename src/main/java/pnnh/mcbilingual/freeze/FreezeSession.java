package pnnh.mcbilingual.freeze;

import java.util.List;

/**
 * The single in-memory freeze. The picture object is owned by the renderer
 * and is not a Minecraft type from this class's point of view.
 */
public final class FreezeSession {
    private static final FreezeSession INSTANCE = new FreezeSession();

    private boolean active;
    private boolean toggleRequested;
    private boolean cursorWasGrabbed;
    private Object picture;
    private List<TextHit> hits = List.of();

    private FreezeSession() {
    }

    public static FreezeSession get() {
        return INSTANCE;
    }

    public void requestToggle() {
        toggleRequested = true;
    }

    public boolean toggleRequested() {
        return toggleRequested;
    }

    public boolean pollToggle() {
        if (!toggleRequested) {
            return false;
        }
        toggleRequested = false;
        return true;
    }

    /**
     * True from the key press through the frozen frames, including the tick
     * that asked to enter or leave. Movement is read from key state, so the
     * tick of the press has to stop as well.
     */
    public boolean suppressInput() {
        return active || toggleRequested;
    }

    public boolean active() {
        return active;
    }

    public void activate(Object picture, List<TextHit> hits, boolean cursorWasGrabbed) {
        this.active = true;
        this.picture = picture;
        this.hits = List.copyOf(hits);
        this.cursorWasGrabbed = cursorWasGrabbed;
    }

    public void deactivate() {
        active = false;
        picture = null;
        hits = List.of();
        cursorWasGrabbed = false;
    }

    public Object picture() {
        return picture;
    }

    public List<TextHit> hits() {
        return hits;
    }

    public boolean cursorWasGrabbed() {
        return cursorWasGrabbed;
    }
}

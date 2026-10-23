package pnnh.mcbilingual.switchlang;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import pnnh.mcbilingual.MCBilingual;
import pnnh.mcbilingual.compat.RivalMods;
import pnnh.mcbilingual.switchlang.mixin.KeyboardHandlerAccessor;

/**
 * The control entry defaults to unbound. Until the player binds one key,
 * holding F3 and pressing J switches language. A bound key replaces that chord.
 * Chat input ignores both.
 */
public final class SwitchKeyHandler {
    public static final String CATEGORY = "key.categories.mcbilingual";
    public static final KeyMapping SWITCH_LANGUAGE = new KeyMapping(
            "key.mcbilingual.switch_language",
            KeyConflictContext.UNIVERSAL,
            InputConstants.UNKNOWN,
            CATEGORY);

    private SwitchKeyHandler() {
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SWITCH_LANGUAGE);
    }

    @EventBusSubscriber(modid = MCBilingual.MOD_ID, value = Dist.CLIENT)
    public static final class Events {
        private Events() {
        }

        @SubscribeEvent
        public static void onKey(InputEvent.Key event) {
            if (RivalMods.isBlocked() || event.getAction() != GLFW.GLFW_PRESS) {
                return;
            }
            if (!acceptingInput()) {
                return;
            }
            if (SWITCH_LANGUAGE.isUnbound()) {
                if (event.getKey() == GLFW.GLFW_KEY_J && f3Held()) {
                    claimDebugChord();
                    LanguageSwitch.trySwitch(Minecraft.getInstance());
                }
                return;
            }
            if (SWITCH_LANGUAGE.matches(event.getKey(), event.getScanCode())) {
                LanguageSwitch.trySwitch(Minecraft.getInstance());
            }
        }

        @SubscribeEvent
        public static void onMouse(InputEvent.MouseButton.Pre event) {
            if (RivalMods.isBlocked() || event.getAction() != GLFW.GLFW_PRESS || SWITCH_LANGUAGE.isUnbound()) {
                return;
            }
            if (!acceptingInput()) {
                return;
            }
            if (SWITCH_LANGUAGE.matchesMouse(event.getButton())) {
                LanguageSwitch.trySwitch(Minecraft.getInstance());
            }
        }
    }

    private static boolean acceptingInput() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getOverlay() != null) {
            return false;
        }
        return !(minecraft.screen instanceof ChatScreen);
    }

    private static boolean f3Held() {
        Minecraft minecraft = Minecraft.getInstance();
        return InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_F3);
    }

    /**
     * Holding F3 and pressing an unbound letter otherwise toggles the debug
     * overlay when F3 is released. This chord is the language switch.
     */
    private static void claimDebugChord() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.keyboardHandler != null) {
            ((KeyboardHandlerAccessor) minecraft.keyboardHandler).mcbilingual$setHandledDebugKey(true);
        }
    }
}

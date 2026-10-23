package pnnh.mcbilingual.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import pnnh.mcbilingual.MCBilingual;

/**
 * One chat line after the player enters a world, only when this mod stood down.
 */
@EventBusSubscriber(modid = MCBilingual.MOD_ID, value = Dist.CLIENT)
public final class RivalNotice {
    private static boolean announced;

    private RivalNotice() {
    }

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!RivalMods.isBlocked() || announced) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null) {
            return;
        }
        announced = true;
        minecraft.gui.getChat().addMessage(Component.literal(RivalMods.message()));
    }
}

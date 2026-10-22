package pnnh.mcbilingual;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-only Hello World scaffold for the bilingual display mod.
 * Loaded only on the physical client; a dedicated server does not require this jar.
 */
@Mod(value = MCBilingual.MOD_ID, dist = Dist.CLIENT)
public class MCBilingual {
    public static final String MOD_ID = "mcbilingual";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MCBilingual(IEventBus modEventBus) {
        LOGGER.info("Hello World from MCBilingual");
        modEventBus.addListener(MCBilingual::onClientSetup);
        NeoForge.EVENT_BUS.addListener(MCBilingual::onLoggingIn);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("Hello World from MCBilingual client setup");
        LOGGER.info("Minecraft user >> {}", Minecraft.getInstance().getUser().getName());
    }

    private static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        event.getPlayer().displayClientMessage(Component.literal("Hello World from MCBilingual"), false);
    }
}

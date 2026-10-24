package pnnh.mcbilingual;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import pnnh.mcbilingual.compat.RivalMods;
import pnnh.mcbilingual.config.ClientConfig;
import pnnh.mcbilingual.freeze.FreezeKeyHandler;
import pnnh.mcbilingual.freeze.LookupModeKeyHandler;
import pnnh.mcbilingual.language.LanguageTexts;
import pnnh.mcbilingual.switchlang.SwitchKeyHandler;

/**
 * Client-only bilingual names. A dedicated server does not load this mod.
 */
@Mod(value = MCBilingual.MOD_ID, dist = Dist.CLIENT)
public class MCBilingual {
    public static final String MOD_ID = "mcbilingual";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MCBilingual(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        if (RivalMods.isBlocked()) {
            LOGGER.warn(RivalMods.message());
            return;
        }
        LOGGER.info("MCBilingual client mod loading");
        modEventBus.addListener(LanguageTexts::onRegisterReloadListeners);
        modEventBus.addListener(SwitchKeyHandler::registerKeys);
        modEventBus.addListener(FreezeKeyHandler::registerKeys);
        modEventBus.addListener(LookupModeKeyHandler::registerKeys);
    }
}

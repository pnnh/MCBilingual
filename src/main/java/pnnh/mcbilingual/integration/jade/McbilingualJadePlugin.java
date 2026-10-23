package pnnh.mcbilingual.integration.jade;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import pnnh.mcbilingual.compat.RivalMods;

/**
 * Loaded by Jade when that mod is present. The main mod class does not reference this type.
 */
@WailaPlugin
public class McbilingualJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        if (RivalMods.isBlocked()) {
            return;
        }
        registration.registerBlockComponent(JadeBlockNames.INSTANCE, Block.class);
        registration.registerEntityComponent(JadeEntityNames.INSTANCE, Entity.class);
    }
}

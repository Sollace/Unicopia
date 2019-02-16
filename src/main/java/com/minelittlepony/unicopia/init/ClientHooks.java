package com.minelittlepony.unicopia.init;

import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
class ClientHooks {

    @SubscribeEvent
    public static void registerItemColours(ColorHandlerEvent.Item event) {
        UItems.registerColors(event.getItemColors());
        UBlocks.registerColors(event.getItemColors(), event.getBlockColors());
    }

}

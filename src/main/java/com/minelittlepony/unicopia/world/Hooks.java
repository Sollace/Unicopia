package com.minelittlepony.unicopia.world;

import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@EventBusSubscriber
public class Hooks {

    @SubscribeEvent
    public static void onBlockHarvested(BlockEvent.HarvestDropsEvent event) {
        UWorld.instance().getBlocks().addAuxiliaryDrops(event.getWorld(), event.getState(), event.getPos(), event.getDrops(), event.getFortuneLevel());
    }

    @SubscribeEvent
    public static void onBlockTilled(UseHoeEvent event) {
        if (UWorld.instance().getBlocks().onBlockTilled(event.getWorld(), event.getPos(), event.getEntityPlayer(), event.getCurrent())) {
            event.setResult(Result.ALLOW);
        }
    }

    @SubscribeEvent
    public static void onStructureGen(PopulateChunkEvent.Populate event) {
        if (event.getType() == EventType.DUNGEON) {
            UWorld.instance().generateStructures(event.getWorld(), event.getChunkX(), event.getChunkZ(), event.getGen());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == Phase.START) {
            UWorld.instance().onUpdate(event.world);
        }
    }
}

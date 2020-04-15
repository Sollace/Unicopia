package com.minelittlepony.unicopia;
/*
package com.minelittlepony.unicopia.world;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.util.ActionResult;

public class Hooks {

    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

        if (event.isCanceled()) {
            return;
        }

        ActionResult result = UWorld.instance().getBlocks().onBlockInteract(
                event.getWorld(), event.getWorld().getBlockState(event.getPos()), event.getPos(), event.getPlayerEntity(), event.getItemStack(), event.getFace(), event.getHand());

        if (result != ActionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    public static void onBlockHarvested(BlockEvent.HarvestDropsEvent event) {
        UWorld.instance().getBlocks().addAuxiliaryDrops(event.getWorld(), event.getState(), event.getPos(), event.getDrops(), event.getFortuneLevel());
    }

    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == Phase.START) {
            UWorld.instance().onUpdate(event.world);
        }
    }
}
*/
package com.minelittlepony.unicopia.world.block.gas;

import com.minelittlepony.unicopia.equine.player.Pony;
import com.minelittlepony.unicopia.util.HoeUtil;
import com.minelittlepony.unicopia.world.block.UBlocks;

import net.minecraft.item.ItemUsageContext;

public class CloudSoilBlock extends CloudBlock implements HoeUtil.Tillable {

    public CloudSoilBlock(GasState variant) {
        super(variant);
        HoeUtil.registerTillingAction(this, UBlocks.CLOUD_FARMLAND.getDefaultState());
    }

    @Override
    public boolean canTill(ItemUsageContext context) {
        return context.getPlayer() == null || Pony.of(context.getPlayer()).getSpecies().canInteractWithClouds();
    }
}

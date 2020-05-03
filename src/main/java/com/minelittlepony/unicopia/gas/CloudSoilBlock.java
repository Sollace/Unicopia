package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.HoeUtil;

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

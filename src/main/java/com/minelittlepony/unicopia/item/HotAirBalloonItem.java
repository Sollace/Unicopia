package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class HotAirBalloonItem extends Item {

    public HotAirBalloonItem(Settings settings) {
        super(settings);
    }

    public static AirBalloonEntity.BalloonDesign getDesign(World world, ItemStack stack) {
        String design;
        if (stack.hasNbt() && !(design = stack.getNbt().getString("design")).isEmpty()) {
            return AirBalloonEntity.BalloonDesign.getType(design);
        }

        int ordinal = 1 + world.getRandom().nextInt(AirBalloonEntity.BalloonDesign.values().length - 1);
        return AirBalloonEntity.BalloonDesign.getType(ordinal);
    }
}

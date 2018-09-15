package com.minelittlepony.unicopia.entity;

import net.minecraft.world.World;

public class EntityConstructionCloud extends EntityCloud {

    public EntityConstructionCloud(World world) {
        super(world);
    }

    @Override
    public boolean getStationary() {
        return true;
    }

    @Override
    public boolean getOpaque() {
        return true;
    }
}

package com.minelittlepony.unicopia;

import java.util.function.Function;

import com.minelittlepony.unicopia.entity.*;

import net.minecraft.world.World;

public enum CloudSize {
    SMALL(EntityRacingCloud::new),
    MEDIUM(EntityConstructionCloud::new),
    LARGE(EntityWildCloud::new);

    private static final CloudSize[] META_LOOKUP = new CloudSize[values().length];
    static {
        CloudSize[] values = values();
        for (CloudSize i : values) {
            META_LOOKUP[i.getMetadata()] = i;
        }
    }

    private final String name;

    private final Function<World, EntityCloud> constructor;

    CloudSize(Function<World, EntityCloud> constructor) {
        this.constructor = constructor;
        this.name = name().toLowerCase();
    }

    public String getName() {
        return name;
    }

    public int getMetadata() {
        return ordinal();
    }

    public EntityCloud createEntity(World w) {
        return constructor.apply(w);
    }

    public static CloudSize byMetadata(int meta) {
        if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
        }
        return META_LOOKUP[meta];
    }
}
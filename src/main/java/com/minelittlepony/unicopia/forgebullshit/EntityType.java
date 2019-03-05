package com.minelittlepony.unicopia.forgebullshit;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public class EntityType {

    private int projectilNetworkId = 0;
    private final String modid;

    public EntityType(String modid) {
        this.modid = modid;
    }


    public static EntityType builder(String modid) {
        return new EntityType(modid);
    }

    public Entry creature(Class<? extends Entity> cls, String name) {
        return new Entry(cls, name);
    }

    public EntityEntry projectile(Class<? extends Entity> cls, String name, int range, int frequency) {
        return projectile(cls, name, range, frequency, true);
    }

    @FUF(reason = "...and it's much shorter than typing out this factory mess every time.")
    public EntityEntry projectile(Class<? extends Entity> cls, String name, int range, int frequency, boolean includeVelocity) {
        return EntityEntryBuilder.create().entity(cls)
            .name(name)
            .id(new ResourceLocation(modid, name), projectilNetworkId++).tracker(range, frequency, includeVelocity)
            .build();
    }

    @FUF(reason = "This makes it easier to register an egg...")
    public class Entry extends EntityEntry {

        public Entry(Class<? extends Entity> cls, String name) {
            super(cls, name);
            setRegistryName(modid, name);
        }

        public Entry withEgg(int a, int b) {
            setEgg(new EntityEggInfo(getRegistryName(), a, b));

            return this;
        }
    }
}

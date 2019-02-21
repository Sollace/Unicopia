package com.minelittlepony.unicopia.init;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class USounds {

    public static final SoundEvent WING_FLAP = new USound(Unicopia.MODID, "wing_flap");
    public static final SoundEvent WIND_RUSH = new USound(Unicopia.MODID, "wind_rush");

    static void init(IForgeRegistry<SoundEvent> registry) {
        registry.registerAll(WING_FLAP, WIND_RUSH);
    }

    static class USound extends SoundEvent {
        USound(String domain, String name) {
            this(new ResourceLocation(domain, name));
        }

        USound(ResourceLocation id) {
            super(id);
            setRegistryName(id);
        }
    }
}

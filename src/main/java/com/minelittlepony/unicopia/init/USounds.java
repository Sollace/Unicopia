package com.minelittlepony.unicopia.init;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class USounds {

    public static final SoundEvent WING_FLAP = new SoundEvent(new ResourceLocation(Unicopia.MODID, "wing_flap"))
            .setRegistryName(Unicopia.MODID, "wing_flap");

    static void init(IForgeRegistry<SoundEvent> registry) {
        registry.registerAll(WING_FLAP);
    }
}

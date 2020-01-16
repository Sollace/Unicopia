package com.minelittlepony.unicopia;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class USounds {

    public static final SoundEvent WING_FLAP = register("wing_flap");
    public static final SoundEvent WIND_RUSH = register("wind_rush");

    public static final SoundEvent INSECT = register("insect");
    public static final SoundEvent CHANGELING_BUZZ = register("changeling_buzz");

    public static final SoundEvent SLIME_ADVANCE = register("slime_advance");
    public static final SoundEvent SLIME_RETRACT = register("slime_retract");

    public static final SoundEvent RECORD_CRUSADE = register("record.crusade");
    public static final SoundEvent RECORD_PET = register("record.pet");
    public static final SoundEvent RECORD_POPULAR = register("record.popular");
    public static final SoundEvent RECORD_FUNK = register("record.funk");

    private static SoundEvent register(String name) {
        Identifier id = new Identifier(Unicopia.MODID, name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    static void bootstrap() {}
}

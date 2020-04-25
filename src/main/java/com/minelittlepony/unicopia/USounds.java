package com.minelittlepony.unicopia;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface USounds {

    SoundEvent WING_FLAP = register("wing_flap");
    SoundEvent WIND_RUSH = register("wind_rush");

    SoundEvent INSECT = register("insect");
    SoundEvent CHANGELING_BUZZ = register("changeling_buzz");

    SoundEvent SLIME_ADVANCE = register("slime_advance");
    SoundEvent SLIME_RETRACT = register("slime_retract");

    SoundEvent RECORD_CRUSADE = register("record.crusade");
    SoundEvent RECORD_PET = register("record.pet");
    SoundEvent RECORD_POPULAR = register("record.popular");
    SoundEvent RECORD_FUNK = register("record.funk");

    static SoundEvent register(String name) {
        Identifier id = new Identifier("unicopia", name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    static void bootstrap() {}
}

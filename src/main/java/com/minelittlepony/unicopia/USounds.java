package com.minelittlepony.unicopia;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface USounds {
    SoundEvent AMBIENT_WIND_GUST = register("ambient.wind.gust");

    SoundEvent ENTITY_PLAYER_BATPONY_SCREECH = register("entity.player.batpony.screech");
    SoundEvent ENTITY_PLAYER_REBOUND = register("entity.player.rebound");
    SoundEvent ENTITY_PLAYER_PEGASUS_WINGSFLAP = register("entity.player.pegasus.wingsflap");
    SoundEvent ENTITY_PLAYER_CHANGELING_BUZZ = register("entity.player.changeling.buzz");

    SoundEvent ENTITY_PLAYER_EARS_RINGING = register("entity.player.ears_ring");

    SoundEvent ITEM_MAGIC_AURA = register("item.magic.aura");

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

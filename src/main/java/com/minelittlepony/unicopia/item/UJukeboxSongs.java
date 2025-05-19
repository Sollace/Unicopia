package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public interface UJukeboxSongs {
    RegistryKey<JukeboxSong> CRUSADE = of("crusade");
    RegistryKey<JukeboxSong> PET = of("pet");
    RegistryKey<JukeboxSong> POPULAR = of("popular");
    RegistryKey<JukeboxSong> FUNK = of("funk");

    private static RegistryKey<JukeboxSong> of(String name) {
        return RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Unicopia.id(name));
    }
}

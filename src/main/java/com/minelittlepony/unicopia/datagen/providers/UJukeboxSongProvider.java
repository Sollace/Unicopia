package com.minelittlepony.unicopia.datagen.providers;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.item.UJukeboxSongs;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class UJukeboxSongProvider extends FabricDynamicRegistryProvider {
    public UJukeboxSongProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "Jukebox Songs";
    }

    @Override
    protected void configure(WrapperLookup registries, Entries entries) {
        register(entries, UJukeboxSongs.CRUSADE, USounds.RECORD_CRUSADE, 181, 1);
        register(entries, UJukeboxSongs.PET, USounds.RECORD_PET, 221, 2);
        register(entries, UJukeboxSongs.POPULAR, USounds.RECORD_POPULAR, 112, 3);
        register(entries, UJukeboxSongs.FUNK, USounds.RECORD_FUNK, 91, 4);
    }

    private void register(
            Entries entries,
            RegistryKey<JukeboxSong> key,
            RegistryEntry.Reference<SoundEvent> soundEvent, int lengthInSeconds, int comparatorOutput) {
        entries.add(key, new JukeboxSong(soundEvent, Text.translatable(Util.createTranslationKey("jukebox_song", key.getValue())), lengthInSeconds, comparatorOutput));
    }

}

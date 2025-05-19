package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class USoundEventTagProvider extends FabricTagProvider<SoundEvent> {
    public USoundEventTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.SOUND_EVENT, registriesFuture);
    }

    @Override
    protected void configure(WrapperLookup lookup) {
        getOrCreateTagBuilder(UTags.Sounds.POISON_JOKE_EVENTS).add(
                SoundEvents.AMBIENT_CAVE.registryKey(),
                SoundEvents.MUSIC_MENU.registryKey(),
                SoundEvents.ENTITY_GENERIC_EXPLODE.registryKey(),
                SoundEvents.AMBIENT_NETHER_WASTES_LOOP.registryKey(),
                SoundEvents.AMBIENT_NETHER_WASTES_MOOD.registryKey(),
                SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD.registryKey(),
                SoundEvents.AMBIENT_BASALT_DELTAS_MOOD.registryKey(),
                SoundEvents.AMBIENT_BASALT_DELTAS_LOOP.registryKey()
        ).add(
                SoundEvents.ENTITY_CREEPER_PRIMED,
                SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON,
                SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF,
                SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON,
                SoundEvents.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF,
                SoundEvents.BLOCK_WOODEN_DOOR_CLOSE,
                SoundEvents.BLOCK_STONE_BREAK,
                SoundEvents.ITEM_SHIELD_BREAK,
                SoundEvents.ENTITY_BLAZE_AMBIENT,
                SoundEvents.ENTITY_ZOMBIE_AMBIENT,
                SoundEvents.ENTITY_DROWNED_AMBIENT,
                SoundEvents.ENTITY_ENDERMITE_AMBIENT,
                SoundEvents.ENTITY_SKELETON_AMBIENT,
                SoundEvents.ENTITY_SKELETON_HORSE_AMBIENT,
                SoundEvents.ENTITY_ZOGLIN_AMBIENT,
                SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_AMBIENT,
                SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR,
                SoundEvents.ENTITY_TNT_PRIMED,
                SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE,
                SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_RARE
        );

    }
}

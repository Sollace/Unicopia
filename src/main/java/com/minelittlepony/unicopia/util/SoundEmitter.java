package com.minelittlepony.unicopia.util;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public interface SoundEmitter {

    World getReferenceWorld();

    Entity getEntity();


    default void playSound(SoundEvent sound, float volume, float pitch) {
        playSoundAt(getEntity(), sound, volume, pitch);
    }

    default void playSound(SoundEvent sound, float volume) {
        playSound(sound, volume, getRandomPitch());
    }

    default float getRandomPitch() {
        return getRandomPitch(getReferenceWorld().getRandom());
    }

    static void playSoundAt(Entity entity, SoundEvent sound, float pitch, float volume) {
        playSoundAt(entity, sound, entity.getSoundCategory(), volume, pitch);
    }

    static void playSoundAt(Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        entity.world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, category, volume, pitch);
    }

    static float getRandomPitch(Random rng) {
        return (float)rng.nextTriangular(0.5F, 0.2F);
    }
}

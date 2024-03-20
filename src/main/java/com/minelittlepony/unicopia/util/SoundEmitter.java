package com.minelittlepony.unicopia.util;

import com.minelittlepony.unicopia.EntityConvertable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

public interface SoundEmitter<E extends Entity> extends EntityConvertable<E> {

    default void playSound(SoundEvent sound, float volume, float pitch) {
        playSoundAt(asEntity(), sound, volume, pitch);
    }

    default void playSound(SoundEvent sound, float volume) {
        playSound(sound, volume, getRandomPitch());
    }

    default float getRandomPitch() {
        return getRandomPitch(asEntity().getWorld().getRandom());
    }

    static void playSoundAt(Entity entity, SoundEvent sound, float volume, float pitch) {
        playSoundAt(entity, sound, entity.getSoundCategory(), volume, pitch);
    }

    static void playSoundAt(Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (entity.getWorld().isClient && entity instanceof PlayerEntity p) {
            entity.getWorld().playSound(p, entity.getX(), entity.getY(), entity.getZ(), sound, category, volume, pitch);
        } else {
            entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, category, volume, pitch);
        }
    }

    static float getRandomPitch(Random rng) {
        return (float)rng.nextTriangular(0.5F, 0.2F);
    }
}

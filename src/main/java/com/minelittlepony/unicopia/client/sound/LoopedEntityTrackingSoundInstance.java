package com.minelittlepony.unicopia.client.sound;

import net.minecraft.client.sound.EntityTrackingSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;

public class LoopedEntityTrackingSoundInstance extends EntityTrackingSoundInstance {
    public LoopedEntityTrackingSoundInstance(SoundEvent soundEvent, float volume, float pitch, Entity entity, long seed) {
        super(soundEvent, entity.getSoundCategory(), volume, pitch, entity);
        this.repeat = true;
        this.repeatDelay = 0;
    }
}

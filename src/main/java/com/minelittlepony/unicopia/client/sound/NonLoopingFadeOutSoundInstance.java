package com.minelittlepony.unicopia.client.sound;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

public class NonLoopingFadeOutSoundInstance extends FadeOutSoundInstance {

    private final long duration;
    private long ticks;

    public NonLoopingFadeOutSoundInstance(SoundEvent sound, SoundCategory category, float volume, Random random, long duration) {
        super(sound, category, volume, random);
        this.duration = duration;
    }

    @Override
    protected boolean shouldKeepPlaying() {
        return ticks++ < duration;
    }

}

package com.minelittlepony.unicopia.client.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;

public abstract class FadeOutSoundInstance extends MovingSoundInstance {

    protected int transitionTicks = 10;

    private float sourceVolume;

    protected float targetVolume;

    private int prevProgress;
    private int progress;

    private boolean fadingOut;

    public FadeOutSoundInstance(SoundEvent sound, SoundCategory category, float volume) {
        super(sound, category);
        this.looping = true;
        this.repeat = true;
        this.volume = volume;
        setTargetVolume(volume);
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }

    @Override
    public final void tick() {
        setFadingOut(!shouldKeepPlaying());

        prevProgress = progress;
        if (progress < transitionTicks) {
            progress++;
        }

        if (fadingOut && getVolume() < 0.001F) {
            setDone();
        }
    }

    private void setFadingOut(boolean fadingOut) {
        if (fadingOut == this.fadingOut) {
            return;
        }

        this.fadingOut = fadingOut;
        setTargetVolume(fadingOut ? 0 : volume);
    }

    protected abstract boolean shouldKeepPlaying();

    protected void setTargetVolume(float target) {
        sourceVolume = getLerpedVolume();
        targetVolume = target;
        progress = 0;
    }

    private float getLerpedVolume() {
        float delta = MinecraftClient.getInstance().getTickDelta();
        float interpolate = MathHelper.clamp(MathHelper.lerp(delta, prevProgress, progress) / transitionTicks, 0, 1);
        return MathHelper.lerp(interpolate, sourceVolume, targetVolume);
    }

    @Override
    public final float getVolume() {
        return getLerpedVolume() * sound.getVolume();
    }
}

package com.minelittlepony.unicopia.client.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public abstract class FadeOutSoundInstance extends MovingSoundInstance {

    protected int transitionTicks = 10;

    private float sourceVolume;

    protected float targetVolume;

    private int prevProgress;
    private int progress;

    private boolean fadingOut;
    private boolean muted;
    private int ticksMuted;

    public FadeOutSoundInstance(SoundEvent sound, SoundCategory category, float volume, Random random) {
        super(sound, category, random);
        this.relative = true;
        this.repeat = true;
        this.volume = volume;
        setTargetVolume(volume);
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        this.repeat = !muted;
        if (!muted) {
            ticksMuted = 0;
        }
    }

    @Override
    public final void tick() {
        boolean fadeOut = !shouldKeepPlaying();

        if (!muted) {
            setFadingOut(fadeOut);
        } else if (ticksMuted++ > 2000) {
            setDone();
            return;
        }

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

    protected void setVolume(float vol){
        sourceVolume = vol;
        targetVolume = vol;
        progress = 0;
    }

    private float getLerpedVolume() {
        float delta = MinecraftClient.getInstance().getTickDelta();
        float interpolate = MathHelper.clamp(MathHelper.lerp(delta, prevProgress, progress) / transitionTicks, 0, 1);
        return MathHelper.lerp(interpolate, sourceVolume, targetVolume);
    }

    @Override
    public final float getVolume() {
        if (muted) {
            return 0.001F;
        }
        return getLerpedVolume() * sound.getVolume().get(random);
    }
}

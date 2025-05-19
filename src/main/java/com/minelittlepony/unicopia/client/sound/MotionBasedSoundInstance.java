package com.minelittlepony.unicopia.client.sound;

import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class MotionBasedSoundInstance<E extends Entity> extends FadeOutSoundInstance {
    // Tune these if you want to change the way this sounds!
    // Currently just hardcoded for flying because nothing else uses this class
    private static final float MAX_VELOCITY = 1.5F;  // Max velocity before we clamp volume (units/tick?)
    private static final float ATTEN_EXPO = 2;       // Exponent for velocity based attenuation
    private static final int FADEIN_TICKS = 20;      // Ticks for fade-in
    private static final float MIN_PITCH = 0.7F;     // Pitch at 0-speed
    private static final float MAX_PITCH = 2.6F;     // Pitch at reference speed MAX_VELOCITY
    private static final float MAX_RATE_TICKS = 20;  // How many ticks it takes to go from 0 to max volume (filter!)

    private final E entity;

    private int tickCount;
    private float currentVal;   // Cache last tick's curve value

    private final float minVolume;
    private final float maxVolume;
    private final float referencePitch;
    private final Predicate<E> playingCondition;

    public MotionBasedSoundInstance(SoundEvent sound, E entity, Predicate<E> playingCondition, float minVolume, float maxVolume, float pitch, Random random) {
        super(sound, entity.getSoundCategory(), 0.1F, random);
        this.entity = entity;
        this.pitch = pitch;
        this.referencePitch = pitch;
        this.minVolume = minVolume;
        this.maxVolume = maxVolume;
        this.playingCondition = playingCondition;
        this.relative = false; // position is ABSOLUTE! We do this so the sound doesn't play to other players

        x = (float)entity.getX();
        y = (float)entity.getY();
        z = (float)entity.getZ();
    }

    @Override
    protected boolean shouldKeepPlaying() {
        ++tickCount;

        if (entity.isRemoved() || !playingCondition.test(entity)) {
            return false;
        }

        // Update effect position
        x = (float)entity.getX();
        y = (float)entity.getY();
        z = (float)entity.getZ();

        // Get velocity
        float f = (float)entity.getVelocity().horizontalLength();

        float lastVal = currentVal;

        // First we normalise the volume to the maximum velocity we're targeting, then we make it a curve.
        // Drag is not linear, and neither is the woosh it produces, so a curve makes it sound more natural.
        currentVal = (float) Math.pow(MathHelper.clamp(f / MAX_VELOCITY, 0, 1), ATTEN_EXPO);

        // Primitive lowpass filter/rate limiter thingy to rule out sudden jolts
        currentVal = lastVal + MathHelper.clamp(currentVal - lastVal, -1 / MAX_RATE_TICKS, 1 / MAX_RATE_TICKS);

        if (f >= 1.0E-7D) {
           // Multiply output volume by reference volume for overall gain control.
           volume = MathHelper.lerp(currentVal, minVolume, maxVolume);
        } else {
           volume = 0.0F;
        }

        // If we only just started playing, fade in!
        if (tickCount < FADEIN_TICKS) {
           volume *= tickCount / (float)FADEIN_TICKS;
        }

        // Control pitch with velocity
        pitch = MathHelper.lerp(currentVal, MIN_PITCH, MAX_PITCH) * referencePitch;

        // Setting target volume every frame breaks interpolation. We set volume directly,
        // so that FadeOutSoundInstance only handles stopping the sound with an actual fade out!
        setVolume(volume);

        return true;
    }
}

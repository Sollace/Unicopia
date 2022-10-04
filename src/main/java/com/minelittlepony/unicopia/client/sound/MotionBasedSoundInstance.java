package com.minelittlepony.unicopia.client.sound;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class MotionBasedSoundInstance extends FadeOutSoundInstance {

    private final PlayerEntity player;

    // Tune these if you want to change the way this sounds!
    // Currently just hardcoded for flying because nothing else uses this class
    private static final float MAX_VELOCITY = 1.5f;    // Max velocity before we clamp volume (units/tick?)
    private static final float VOLUME_AT_MAX = 1.0f;
    private static final float ATTEN_EXPO = 2.0f;       // Exponent for velocity based attenuation
    private static final int FADEIN_TICKS = 20;         // Ticks for fade-in
    private static final float MIN_PITCH = 0.7f;        // Pitch at 0-speed
    private static final float MAX_PITCH = 2.6f;        // Pitch at reference speed MAX_VELOCITY
    private static final float MAX_RATE_TICKS = 20.0f;       // How many ticks it takes to go from 0 to max volume (filter!)

    private int tickCount;
    private float currentVal;   // Cache last tick's curve value

    public MotionBasedSoundInstance(SoundEvent sound, PlayerEntity player, Random random) {
        super(sound, player.getSoundCategory(), 0.1F, random);
        this.player = player;
        currentVal = 0.0f;
    }

    @Override
    protected boolean shouldKeepPlaying() {
        ++tickCount;

        if (player.isRemoved()) {
            return false;
        }

        Pony pony = Pony.of(player);

        if (!pony.getPhysics().isFlying() || !pony.getPhysics().getFlightType().isAvian()) {
            return false;
        }

        // Update effect position
        x = ((float)player.getX());
        y = ((float)player.getY());
        z = ((float)player.getZ());

        // Get velocity
        float f = (float)player.getVelocity().horizontalLength();

        float lastVal = currentVal;

        // First we normalise the volume to the maximum velocity we're targeting, then we make it a curve.
        // Drag is not linear, and neither is the woosh it produces, so a curve makes it sound more natural.
        currentVal = (float) Math.pow(MathHelper.clamp(f / MAX_VELOCITY, 0, 1),ATTEN_EXPO);

        // Primitive lowpass filter/rate limiter thingy to rule out sudden jolts
        currentVal = lastVal + MathHelper.clamp(currentVal - lastVal, -(1/MAX_RATE_TICKS), 1/MAX_RATE_TICKS);

        if (f >= 1.0E-7D) {
           // Multiply output volume by reference volume for overall gain control.
           volume = currentVal * VOLUME_AT_MAX;
        } else {
           volume = 0.0F;
        }

        // If we only just started playing, fade in!
        if (tickCount < FADEIN_TICKS) {
           volume *= ((tickCount) / (float)FADEIN_TICKS);
        }

        // Control pitch with velocity
        pitch = MathHelper.lerp(currentVal,MIN_PITCH,MAX_PITCH);

        // Setting target volume every frame breaks interpolation. We set volume directly,
        // so that FadeOutSoundInstance only handles stopping the sound with an actual fade out!
        setVolume(volume);

        return true;
    }
}

package com.minelittlepony.unicopia.client.sound;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class MotionBasedSoundInstance extends FadeOutSoundInstance {

    private final PlayerEntity player;

    private int tickCount;

    public MotionBasedSoundInstance(SoundEvent sound, PlayerEntity player, Random random) {
        super(sound, player.getSoundCategory(), 0.1F, random);
        this.player = player;
    }

    @Override
    protected boolean shouldKeepPlaying() {
        ++tickCount;

        if (player.isRemoved() || tickCount > 200) {
            return false;
        }

        Pony pony = Pony.of(player);

        if (!pony.getPhysics().isFlying() || !pony.getPhysics().getFlightType().isAvian()) {
            return false;
        }

        x = ((float)player.getX());
        y = ((float)player.getY());
        z = ((float)this.player.getZ());

        float f = (float)player.getVelocity().lengthSquared();
        if (f >= 1.0E-7D) {
           volume = MathHelper.clamp(f / 4F, 0, 1);
        } else {
           volume = 0.0F;
        }

        if (tickCount < 20) {
           volume = 0;
        } else if (tickCount < 40) {
           volume = (float)(volume * ((tickCount - 20) / 20D));
        }

        if (volume > 0.8F) {
           pitch = 1 + (volume - 0.8F);
        } else {
           pitch = 1;
        }

        return true;
    }
}

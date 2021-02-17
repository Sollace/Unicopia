package com.minelittlepony.unicopia.client.sound;

import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.item.enchantment.SimpleEnchantment;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MagicAuraSoundInstance extends MovingSoundInstance {

    private final Living<?> living;

    private float sourceVolume;
    private float targetVolume;
    private float interpolate;

    private boolean fadingOut;

    public MagicAuraSoundInstance(SoundCategory category, Living<?> living) {
        super(USounds.ITEM_MAGIC_AURA, category);
        this.looping = true;
        this.repeat = true;
        this.living = living;
        this.volume = 0;
        setTargetVolume(1);
    }

    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }

    @Override
    public void tick() {
        Optional<SimpleEnchantment.Data> data = living.getEnchants().getOrEmpty(UEnchantments.GEM_FINDER);

        Vec3d pos = living.getOriginVector();
        x = pos.x;
        y = pos.y;
        z = pos.z;

        if (!living.getEntity().removed && data.isPresent()) {
            float level = data.get().level;
            if (level != targetVolume) {
                setTargetVolume(level);
            }
        } else {
            fadingOut = true;
            setTargetVolume(0);
        }

        if (interpolate < 1) {
            interpolate = Math.min(interpolate +  0.4F, 1);
            volume = MathHelper.lerp(interpolate, sourceVolume, targetVolume);
        }

        if (fadingOut && volume < 0.01F) {
            setDone();
        }
    }

    private void setTargetVolume(float target) {
        sourceVolume = volume;
        targetVolume = target;
        interpolate = 0;
    }
}

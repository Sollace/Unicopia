package com.minelittlepony.unicopia.client.sound;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class MagicAuraSoundInstance extends FadeOutSoundInstance {

    private final Living<?> living;

    public MagicAuraSoundInstance(SoundCategory category, Living<?> living, Random random) {
        super(USounds.ITEM_MAGIC_AURA, category, 0.1F, random);
        this.relative = false;
        this.living = living;
        setPosition(living.getOriginVector());
    }

    @Override
    protected boolean shouldKeepPlaying() {
        int level = EnchantmentUtil.getLevel(UEnchantments.GEM_FINDER, living.asEntity());

        setPosition(living.getOriginVector());
        if (level <= 0 || living.asEntity().isRemoved()) {
            return false;
        }

        float volume = computeTargetVolume(level);
        if (volume != targetVolume) {
            setTargetVolume(volume);
        }
        return true;
    }

    private float computeTargetVolume(int level) {
        int radius = 2 + (level * 2);

        BlockPos origin = living.getOrigin();

        double volume = BlockPos.findClosest(origin, radius, radius, p -> living.asWorld().getBlockState(p).isIn(UTags.Blocks.INTERESTING))
            .map(p -> living.getOriginVector().squaredDistanceTo(p.getX(), p.getY(), p.getZ()))
            .map(find -> (1 - (Math.sqrt(find) / radius)))
            .orElse(-1D);

        volume = Math.max(volume, 0.04F);

        return (float)volume * (1.3F + level);
    }
}

package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.particle.MagicParticleEffect;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerBlock;
import net.minecraft.client.util.ParticleUtil;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

class CuringJokeBlock extends FlowerBlock {
    public CuringJokeBlock(StatusEffect suspiciousStewEffect, int effectDuration, Settings settings) {
        super(suspiciousStewEffect, effectDuration, settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        for (int i = 0; i < 3; i++) {
            ParticleUtil.spawnParticle(world, pos, random, new MagicParticleEffect(0x3388EE));
        }
    }
}

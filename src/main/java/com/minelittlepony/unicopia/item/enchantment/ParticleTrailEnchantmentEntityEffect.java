package com.minelittlepony.unicopia.item.enchantment;

import java.util.Optional;

import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.particle.WeakTarget;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record ParticleTrailEnchantmentEntityEffect(
        Optional<ParticleEffect> particle,
        float followSpeed,
        int density,
        int probability
    ) implements EnchantmentEntityEffect {
    public static final MapCodec<ParticleTrailEnchantmentEntityEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ParticleTypes.TYPE_CODEC.optionalFieldOf("particle").forGetter(ParticleTrailEnchantmentEntityEffect::particle),
            Codec.FLOAT.fieldOf("followSpeed").forGetter(ParticleTrailEnchantmentEntityEffect::followSpeed),
            Codec.INT.fieldOf("density").forGetter(ParticleTrailEnchantmentEntityEffect::density),
            Codec.INT.fieldOf("probability").forGetter(ParticleTrailEnchantmentEntityEffect::probability)
    ).apply(instance, ParticleTrailEnchantmentEntityEffect::new));

    @Override
    public MapCodec<ParticleTrailEnchantmentEntityEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity entity, Vec3d pos) {
        if (probability <= 0 || world.random.nextInt(probability) == 0) {
            ParticleUtils.spawnParticles(new FollowingParticleEffect(
                    UParticles.HEALTH_DRAIN,
                    new WeakTarget(entity.getCameraPosVec(1), entity), followSpeed, particle
            ), entity, density);
        }
    }
}

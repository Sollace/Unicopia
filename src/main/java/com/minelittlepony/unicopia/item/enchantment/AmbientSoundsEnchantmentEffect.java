package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.util.RegistryUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public record AmbientSoundsEnchantmentEffect (
        Identifier id,
        TagKey<SoundEvent> sounds
) implements EnchantmentEntityEffect {
    public static final MapCodec<AmbientSoundsEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(AmbientSoundsEnchantmentEffect::id),
            TagKey.codec(RegistryKeys.SOUND_EVENT).fieldOf("sounds").forGetter(AmbientSoundsEnchantmentEffect::sounds)
    ).apply(instance, AmbientSoundsEnchantmentEffect::new));

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity entity, Vec3d pos) {
        Living.getOrEmpty(entity).ifPresent(user -> {
            user.getEnchants().compute(id, (id, data) -> {
                if (data == null) {
                    data = 0F;
                }
                Random rng = world.getRandom();
                data -= rng.nextFloat() * 0.8F;
                int light = world.getLightLevel(entity.getRootVehicle().getBlockPos());
                if (rng.nextInt(Math.max(1, (light * 9) + data.intValue())) == 0) {
                    data = (float)rng.nextInt(5000);

                    RegistryUtils.pickRandom(world, sounds).ifPresent(event -> {
                        user.asWorld().playSoundFromEntity(
                                null,
                                user.asEntity(),
                                event, SoundCategory.HOSTILE,
                                0.5F + rng.nextFloat() * 0.5F,
                                0.5F + rng.nextFloat() * 0.5F
                        );
                    });
                }
                return data;
            });
        });

    }
}

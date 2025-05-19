package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.Living;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.AttributeEnchantmentEffect;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.world.ServerWorld;

public record GroupBasedAttributeEnchantmentEffect (
        AttributeEnchantmentEffect attribute,
        EnchantmentLevelBasedValue range,
        TagKey<Enchantment> validTeamMateEnchantmentKey
    ) implements EnchantmentEntityEffect {
    public static final MapCodec<GroupBasedAttributeEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AttributeEnchantmentEffect.CODEC.fieldOf("attribute").forGetter(GroupBasedAttributeEnchantmentEffect::attribute),
            EnchantmentLevelBasedValue.CODEC.fieldOf("range").forGetter(GroupBasedAttributeEnchantmentEffect::range),
            TagKey.codec(RegistryKeys.ENCHANTMENT).fieldOf("valid_teammate_enchantment_key").forGetter(GroupBasedAttributeEnchantmentEffect::validTeamMateEnchantmentKey)
    ).apply(instance, GroupBasedAttributeEnchantmentEffect::new));

    @Override
    public MapCodec<GroupBasedAttributeEnchantmentEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        Living.getOrEmpty(user).ifPresent(living -> {
            int collectiveLevels = living.findAllEntitiesInRange(range.getValue(level), e -> e instanceof LivingEntity)
                    .mapToInt(e -> EnchantmentUtil.getLevel((LivingEntity)e, validTeamMateEnchantmentKey))
                    .reduce(Integer::sum)
                    .orElse(0);

            living.updateAttributeModifier(attribute.id(), attribute.attribute(), attribute.amount().getValue(collectiveLevels), value -> {
                return new EntityAttributeModifier(attribute.id(), value, attribute.operation());
            }, false);
        });
    }

    @Override
    public void remove(EnchantmentEffectContext context, Entity user, Vec3d pos, int level) {
        Living.getOrEmpty(user).ifPresent(living -> {
            living.updateAttributeModifier(attribute.id(), attribute.attribute(), 0, value -> {
                return new EntityAttributeModifier(attribute.id(), value, attribute.operation());
            }, false);
        });
    }
}

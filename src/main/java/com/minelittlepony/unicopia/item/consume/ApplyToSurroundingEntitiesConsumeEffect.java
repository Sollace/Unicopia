package com.minelittlepony.unicopia.item.consume;

import java.util.List;

import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.util.VecHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.world.World;

public record ApplyToSurroundingEntitiesConsumeEffect(double range, List<ConsumeEffect> effects) implements DeathConsumeEffect {
    public static final MapCodec<ApplyToSurroundingEntitiesConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.DOUBLE.fieldOf("range").forGetter(ApplyToSurroundingEntitiesConsumeEffect::range),
        ConsumeEffect.CODEC.listOf().fieldOf("effect").forGetter(ApplyToSurroundingEntitiesConsumeEffect::effects)
    ).apply(i, ApplyToSurroundingEntitiesConsumeEffect::new));
    public static final PacketCodec<RegistryByteBuf, ApplyToSurroundingEntitiesConsumeEffect> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.DOUBLE,
        ApplyToSurroundingEntitiesConsumeEffect::range,
        ConsumeEffect.PACKET_CODEC.collect(PacketCodecs.toList()),
        ApplyToSurroundingEntitiesConsumeEffect::effects,
        ApplyToSurroundingEntitiesConsumeEffect::new
    );

    @Override
    public Type<? extends ConsumeEffect> getType() {
        return UDataComponentTypes.APPLY_TO_SURROUNDING_ENTITIES;
    }

    @Override
    public boolean onConsume(World world, ItemStack stack, LivingEntity user, DamageSource damage) {
        var targets = VecHelper.findInRange(user, world, user.getPos(), range, e -> e instanceof LivingEntity & !SpellType.SHIELD.isOn(e));
        for (var target : targets) {
            for (var effect : effects) {
                if (effect instanceof ApplyToSurroundingEntitiesConsumeEffect) {
                    continue;
                }
                if (effect instanceof DeathConsumeEffect o) {
                    o.onConsume(world, stack, (LivingEntity)target, damage);
                } else {
                    effect.onConsume(world, stack, (LivingEntity)target);
                }
            }
        }

        return !targets.isEmpty();
    }
}

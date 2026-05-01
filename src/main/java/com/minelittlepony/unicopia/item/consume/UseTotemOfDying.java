package com.minelittlepony.unicopia.item.consume;

import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgEntityStatus;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public record UseTotemOfDying(boolean perpetrator) implements DeathConsumeEffect {
    public static final MapCodec<UseTotemOfDying> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.BOOL.fieldOf("perpetrator").forGetter(UseTotemOfDying::perpetrator)
    ).apply(i, UseTotemOfDying::new));
    public static final PacketCodec<RegistryByteBuf, UseTotemOfDying> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.BOOLEAN,
        UseTotemOfDying::perpetrator,
        UseTotemOfDying::new
    );

    @Override
    public Type<? extends ConsumeEffect> getType() {
        return UDataComponentTypes.USE_TOTEM_OF_DYING;
    }

    @Override
    public boolean onConsume(World world, ItemStack stack, LivingEntity user, DamageSource damage) {
        Channel.ENTITY_STATUS.sendToSurroundingPlayers(new MsgEntityStatus(user.getId(), MsgEntityStatus.USE_TOTEM_OF_DYING), user);
        if (!perpetrator && world instanceof ServerWorld sw) {
            user.damage(sw, damage, Integer.MAX_VALUE);
        }
        return true;
    }
}

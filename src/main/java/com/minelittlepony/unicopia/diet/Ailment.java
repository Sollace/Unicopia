package com.minelittlepony.unicopia.diet;

import com.minelittlepony.unicopia.diet.affliction.Affliction;
import com.minelittlepony.unicopia.diet.affliction.EmptyAffliction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record Ailment(Affliction effects) {
    public static final Ailment EMPTY = new Ailment(EmptyAffliction.INSTANCE);
    public static final Codec<Ailment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Affliction.CODEC.fieldOf("effects").forGetter(Ailment::effects)
    ).apply(instance, Ailment::new));
    public static final PacketCodec<RegistryByteBuf, Ailment> PACKET_CODEC = Affliction.PACKET_CODEC.xmap(Ailment::new, Ailment::effects);
}

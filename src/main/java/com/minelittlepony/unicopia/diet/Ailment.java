package com.minelittlepony.unicopia.diet;

import com.minelittlepony.unicopia.diet.affliction.Affliction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.PacketByteBuf;

public record Ailment(Affliction effects) {
    public static final Ailment EMPTY = new Ailment(Affliction.EMPTY);
    public static final Codec<Ailment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Affliction.CODEC.fieldOf("effects").forGetter(Ailment::effects)
    ).apply(instance, Ailment::new));

    public Ailment(PacketByteBuf buffer) {
        this(Affliction.read(buffer));
    }

    public void toBuffer(PacketByteBuf buffer) {
        Affliction.write(buffer, effects);
    }
}

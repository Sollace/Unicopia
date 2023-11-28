package com.minelittlepony.unicopia.diet;

import com.minelittlepony.unicopia.item.toxin.Toxicity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.PacketByteBuf;

public record Ailment(Toxicity toxicity, Affliction effects) {
    public static final Codec<Ailment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Toxicity.CODEC.fieldOf("toxicity").forGetter(Ailment::toxicity),
            Affliction.CODEC.fieldOf("effects").forGetter(Ailment::effects)
    ).apply(instance, Ailment::new));

    public Ailment(PacketByteBuf buffer) {
        this(Toxicity.byName(buffer.readString()), Affliction.read(buffer));
    }

    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeString(toxicity.name());
        Affliction.write(buffer, effects);
    }
}

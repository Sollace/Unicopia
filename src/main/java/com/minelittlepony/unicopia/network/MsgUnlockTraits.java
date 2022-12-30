package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.entity.player.PlayerEntity;

public record MsgUnlockTraits (Set<Trait> traits) implements Packet<PlayerEntity> {
    MsgUnlockTraits(PacketByteBuf buffer) {
        this(new HashSet<>());
        int length = buffer.readInt();
        for (int i = 0; i < length; i++) {
            Trait.fromId(buffer.readIdentifier()).ifPresent(traits::add);
        }
    }

    public MsgUnlockTraits(Set<Trait> traits) {
        this.traits = new HashSet<>(traits);
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(traits.size());
        traits.forEach(trait -> buffer.writeIdentifier(trait.getId()));
    }

    @Override
    public void handle(PlayerEntity sender) { }
}

package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.util.network.Packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.entity.player.PlayerEntity;

public class MsgUnlockTraits implements Packet<PlayerEntity> {

    public final Set<Trait> traits = new HashSet<>();

    MsgUnlockTraits(PacketByteBuf buffer) {
        int length = buffer.readInt();
        for (int i = 0; i < length; i++) {
            Trait.fromId(buffer.readIdentifier()).ifPresent(traits::add);
        }
    }

    public MsgUnlockTraits(Set<Trait> traits) {
        this.traits.addAll(traits);
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(traits.size());
        traits.forEach(trait -> buffer.writeIdentifier(trait.getId()));
    }

    @Override
    public void handle(PlayerEntity sender) {
        InteractionManager.instance().getClientNetworkHandler().handleUnlockTraits(this);
    }
}

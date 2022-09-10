package com.minelittlepony.unicopia.network;

import java.util.*;

import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.util.network.Packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MsgServerResources implements Packet<PlayerEntity> {
    private final Map<Identifier, SpellTraits> entries;

    public MsgServerResources() {
        entries = SpellTraits.all();
    }

    public MsgServerResources(PacketByteBuf buffer) {
        entries = buffer.readMap(PacketByteBuf::readIdentifier, SpellTraits::fromPacket);
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeMap(entries, PacketByteBuf::writeIdentifier, (r, v) -> v.write(r));
    }

    @Override
    public void handle(PlayerEntity sender) {
        SpellTraits.load(entries);
    }
}

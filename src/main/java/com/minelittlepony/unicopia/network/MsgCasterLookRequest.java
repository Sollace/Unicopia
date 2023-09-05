package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.minelittlepony.unicopia.ability.data.Rot;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.OrientedSpell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.HandledPacket;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Sent to the client when the server needs to know precisely where the player is looking.
 */
public record MsgCasterLookRequest (UUID spellId) implements Packet<PlayerEntity> {

    public MsgCasterLookRequest(PacketByteBuf buffer) {
        this(buffer.readUuid());
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeUuid(spellId);
    }

    public record Reply (
            UUID spellId,
            Rot rotation
        ) implements HandledPacket<ServerPlayerEntity> {

        Reply(PacketByteBuf buffer) {
            this(buffer.readUuid(), Rot.SERIALIZER.read().apply(buffer));
        }

        public Reply(OrientedSpell spell, Caster<?> caster) {
            this(spell.getUuid(), Rot.of(caster));
        }

        @Override
        public void toBuffer(PacketByteBuf buffer) {
            buffer.writeUuid(spellId);
            Rot.SERIALIZER.write().accept(buffer, rotation);
        }

        @Override
        public void handle(ServerPlayerEntity sender) {
            Pony.of(sender).getSpellSlot()
                .get(SpellPredicate.IS_ORIENTED.withId(spellId), false)
                .ifPresent(spell -> {
                   spell.setOrientation(rotation);
                });
        }
    }
}

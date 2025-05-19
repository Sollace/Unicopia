package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.minelittlepony.unicopia.ability.data.Rot;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.OrientedSpell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Handled;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;

/**
 * Sent to the client when the server needs to know precisely where the player is looking.
 */
public record MsgCasterLookRequest (UUID spellId) {
    public static final PacketCodec<ByteBuf, MsgCasterLookRequest> PACKET_CODEC = Uuids.PACKET_CODEC.xmap(MsgCasterLookRequest::new, MsgCasterLookRequest::spellId);

    public record Reply (
            UUID spellId,
            Rot rotation
        ) implements Handled<ServerPlayerEntity> {
        public static final PacketCodec<PacketByteBuf, Reply> PACKET_CODEC = PacketCodec.tuple(
                Uuids.PACKET_CODEC, Reply::spellId,
                Rot.CODEC, Reply::rotation,
                Reply::new
        );

        public Reply(OrientedSpell spell, Caster<?> caster) {
            this(spell.getUuid(), Rot.of(caster));
        }

        @Override
        public void handle(ServerPlayerEntity sender) {
            Pony pony = Pony.of(sender);
            pony.getSpellSlot()
                .get(SpellPredicate.IS_ORIENTED.withId(spellId))
                .ifPresent(spell -> spell.setOrientation(pony, rotation));
        }
    }
}

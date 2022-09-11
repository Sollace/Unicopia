package com.minelittlepony.unicopia.network;

import java.util.*;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.client.gui.spellbook.ClientChapters;
import com.minelittlepony.unicopia.container.SpellbookChapterLoader;
import com.minelittlepony.unicopia.util.network.Packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MsgServerResources implements Packet<PlayerEntity> {
    public final Map<Identifier, SpellTraits> traits;
    public final Map<Identifier, ?> chapters;

    public MsgServerResources() {
        traits = SpellTraits.all();
        chapters = SpellbookChapterLoader.INSTANCE.getChapters();
    }

    @Environment(EnvType.CLIENT)
    public MsgServerResources(PacketByteBuf buffer) {
        traits = buffer.readMap(PacketByteBuf::readIdentifier, SpellTraits::fromPacket);
        chapters = buffer.readMap(PacketByteBuf::readIdentifier, ClientChapters::loadChapter);
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeMap(traits, PacketByteBuf::writeIdentifier, (r, v) -> v.write(r));
        buffer.writeMap(chapters, PacketByteBuf::writeIdentifier, (r, v) -> ((SpellbookChapterLoader.Chapter)v).write(r));
    }

    @Override
    public void handle(PlayerEntity sender) {
        InteractionManager.instance().getClientNetworkHandler().handleServerResources(this);
    }
}

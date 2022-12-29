package com.minelittlepony.unicopia.network;

import java.util.*;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.data.tree.TreeTypeLoader;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.SpellbookChapterLoader;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MsgServerResources implements Packet<PlayerEntity> {
    public final Map<Identifier, SpellTraits> traits;
    public final Map<Identifier, ?> chapters;
    public final Map<Identifier, TreeTypeLoader.TreeTypeDef> treeTypes;

    public MsgServerResources() {
        traits = SpellTraits.all();
        chapters = SpellbookChapterLoader.INSTANCE.getChapters();
        treeTypes = TreeTypeLoader.INSTANCE.getEntries();
    }

    public MsgServerResources(PacketByteBuf buffer) {
        traits = buffer.readMap(PacketByteBuf::readIdentifier, SpellTraits::fromPacket);
        chapters = InteractionManager.instance().getClientNetworkHandler().readChapters(buffer);
        treeTypes = buffer.readMap(PacketByteBuf::readIdentifier, TreeTypeLoader.TreeTypeDef::new);
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeMap(traits, PacketByteBuf::writeIdentifier, (r, v) -> v.write(r));
        buffer.writeMap(chapters, PacketByteBuf::writeIdentifier, (r, v) -> ((SpellbookChapterLoader.Chapter)v).write(r));
        buffer.writeMap(treeTypes, PacketByteBuf::writeIdentifier, (r, v) -> v.write(r));
    }

    @Override
    public void handle(PlayerEntity sender) {
        InteractionManager.instance().getClientNetworkHandler().handleServerResources(this);
    }
}

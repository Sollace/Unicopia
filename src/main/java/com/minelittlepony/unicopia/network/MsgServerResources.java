package com.minelittlepony.unicopia.network;

import java.util.*;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.data.tree.TreeTypeLoader;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.SpellbookChapterLoader;
import com.minelittlepony.unicopia.diet.PonyDiets;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record MsgServerResources (
        Map<Identifier, SpellTraits> traits,
        Map<Identifier, ?> chapters,
        Map<Identifier, TreeTypeLoader.TreeTypeDef> treeTypes,
        PonyDiets diets
    ) implements Packet {
    public MsgServerResources() {
        this(
            SpellTraits.all(),
            SpellbookChapterLoader.INSTANCE.getChapters(),
            TreeTypeLoader.INSTANCE.getEntries(),
            PonyDiets.getInstance()
        );
    }

    public MsgServerResources(PacketByteBuf buffer) {
        this(
            buffer.readMap(PacketByteBuf::readIdentifier, SpellTraits::fromPacket),
            InteractionManager.instance().readChapters(buffer),
            buffer.readMap(PacketByteBuf::readIdentifier, TreeTypeLoader.TreeTypeDef::new),
            new PonyDiets(buffer)
        );
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeMap(traits, PacketByteBuf::writeIdentifier, (r, v) -> v.write(r));
        buffer.writeMap(chapters, PacketByteBuf::writeIdentifier, (r, v) -> ((SpellbookChapterLoader.Chapter)v).write(r));
        buffer.writeMap(treeTypes, PacketByteBuf::writeIdentifier, (r, v) -> v.write(r));
        diets.toBuffer(buffer);
    }
}

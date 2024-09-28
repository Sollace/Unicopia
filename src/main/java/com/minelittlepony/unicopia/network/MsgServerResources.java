package com.minelittlepony.unicopia.network;

import java.util.*;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.data.tree.TreeTypeLoader;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.SpellbookChapterLoader;
import com.minelittlepony.unicopia.diet.PonyDiets;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record MsgServerResources (
        Map<Identifier, SpellTraits> traits,
        Map<Identifier, ?> chapters,
        Map<Identifier, TreeTypeLoader.TreeTypeDef> treeTypes,
        PonyDiets diets
    ) {
    public static final PacketCodec<RegistryByteBuf, MsgServerResources> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, SpellTraits.PACKET_CODEC), MsgServerResources::traits,
            PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, null), MsgServerResources::chapters,
            PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, TreeTypeLoader.TreeTypeDef.PACKET_CODEC), MsgServerResources::treeTypes,
            PonyDiets.PACKET_CODEC, MsgServerResources::diets,
            MsgServerResources::new
    );

    public MsgServerResources() {
        this(
            SpellTraits.all(),
            SpellbookChapterLoader.INSTANCE.getChapters(),
            TreeTypeLoader.INSTANCE.getEntries(),
            PonyDiets.getInstance()
        );
    }
}

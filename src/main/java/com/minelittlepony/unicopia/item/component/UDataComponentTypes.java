package com.minelittlepony.unicopia.item.component;

import java.util.function.UnaryOperator;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.mojang.serialization.Codec;

import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface UDataComponentTypes {
    ComponentType<SpellTraits> SPELL_TRAITS = register("spell_traits", builder -> builder.codec(SpellTraits.CODEC).packetCodec(SpellTraits.PACKET_CODEC).cache());
    ComponentType<SpellbookState> SPELLBOOK_STATE = register("spellbook_state", builder -> builder.codec(SpellbookState.CODEC).packetCodec(SpellbookState.PACKET_CODEC).cache());
    ComponentType<Boolean> GLOWING = register("glowing", builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL));
    ComponentType<ButterflyEntity.Variant> BUTTERFLY_VARIANT = register("butterfly_variant", builder -> builder.codec(ButterflyEntity.Variant.CODEC).packetCodec(ButterflyEntity.Variant.PACKET_CODEC));

    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Unicopia.id(name), builderOperator.apply(ComponentType.builder()).build());
    }

    static void bootstrap() { }
}

package com.minelittlepony.unicopia.item.component;

import java.util.function.UnaryOperator;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.minelittlepony.unicopia.diet.DietProfile;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.mojang.serialization.Codec;

import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface UDataComponentTypes {
    ComponentType<SpellType<?>> STORED_SPELL = register("stored_spell", builder -> builder.codec(SpellType.CODEC).packetCodec(SpellType.PACKET_CODEC));
    ComponentType<SpellTraits> SPELL_TRAITS = register("spell_traits", builder -> builder.codec(SpellTraits.CODEC).packetCodec(SpellTraits.PACKET_CODEC).cache());
    ComponentType<SpellbookState> SPELLBOOK_STATE = register("spellbook_state", builder -> builder.codec(SpellbookState.CODEC).packetCodec(SpellbookState.PACKET_CODEC).cache());
    ComponentType<Boolean> GLOWING = register("glowing", builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL));
    ComponentType<BufferflyVariantComponent> BUTTERFLY_VARIANT = register("butterfly_variant", builder -> builder.codec(BufferflyVariantComponent.CODEC).packetCodec(BufferflyVariantComponent.PACKET_CODEC));
    ComponentType<AirBalloonEntity.BalloonDesign> BALLOON_DESIGN = register("balloon_design", builder -> builder.codec(AirBalloonEntity.BalloonDesign.CODEC).packetCodec(AirBalloonEntity.BalloonDesign.PACKET_CODEC));
    ComponentType<Issuer> ISSUER = register("issuer", builder -> builder.codec(Issuer.CODEC).packetCodec(Issuer.PACKET_CODEC).cache());
    ComponentType<Charges> CHARGES = register("charges", builder -> builder.codec(Charges.CODEC).packetCodec(Charges.PACKET_CODEC));
    ComponentType<Appearance> APPEARANCE = register("appearance", builder -> builder.codec(Appearance.CODEC).packetCodec(Appearance.PACKET_CODEC));
    ComponentType<DietProfile> DIET_PROFILE = register("diet_profile", builder -> builder.codec(DietProfile.CODEC).packetCodec(DietProfile.PACKET_CODEC));
    ComponentType<BreaksIntoItemComponent> ITEM_AFTER_BREAKING = register("item_after_breaking", builder -> builder.codec(BreaksIntoItemComponent.CODEC).packetCodec(BreaksIntoItemComponent.PACKET_CODEC));

    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Unicopia.id(name), builderOperator.apply(ComponentType.builder()).build());
    }

    static void bootstrap() { }
}

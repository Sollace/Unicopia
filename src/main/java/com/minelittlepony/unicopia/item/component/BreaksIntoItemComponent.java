package com.minelittlepony.unicopia.item.component;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public record BreaksIntoItemComponent(
        TagKey<DamageType> damageType,
        RegistryKey<Item> itemAfterBreaking,
        RegistryKey<SoundEvent> breakingSound
    ) {
    public static final Codec<BreaksIntoItemComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(RegistryKeys.DAMAGE_TYPE).fieldOf("damage_type").forGetter(BreaksIntoItemComponent::damageType),
            RegistryKey.createCodec(RegistryKeys.ITEM).fieldOf("item_after_breaking").forGetter(BreaksIntoItemComponent::itemAfterBreaking),
            RegistryKey.createCodec(RegistryKeys.SOUND_EVENT).fieldOf("breaking_sound").forGetter(BreaksIntoItemComponent::breakingSound)
    ).apply(instance, BreaksIntoItemComponent::new));
    public static final PacketCodec<RegistryByteBuf, BreaksIntoItemComponent> PACKET_CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC.xmap(id -> TagKey.of(RegistryKeys.DAMAGE_TYPE, id), key -> key.id()), BreaksIntoItemComponent::damageType,
            RegistryKey.createPacketCodec(RegistryKeys.ITEM), BreaksIntoItemComponent::itemAfterBreaking,
            RegistryKey.createPacketCodec(RegistryKeys.SOUND_EVENT), BreaksIntoItemComponent::breakingSound,
            BreaksIntoItemComponent::new
    );

    public Optional<Item> getItemAfterBreaking() {
        return Registries.ITEM.getOptionalValue(itemAfterBreaking());
    }

    public Optional<SoundEvent> getBreakingSound() {
        return Registries.SOUND_EVENT.getOptionalValue(breakingSound);
    }
}

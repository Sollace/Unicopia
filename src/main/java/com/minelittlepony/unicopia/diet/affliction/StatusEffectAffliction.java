package com.minelittlepony.unicopia.diet.affliction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.MathHelper;

public record StatusEffectAffliction(RegistryEntry<StatusEffect> effect, Range seconds, Range amplifier, int chance) implements Affliction {
    public static final MapCodec<StatusEffectAffliction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Registries.STATUS_EFFECT.getEntryCodec().fieldOf("effect").forGetter(StatusEffectAffliction::effect),
            Range.CODEC.fieldOf("seconds").forGetter(StatusEffectAffliction::seconds),
            Range.CODEC.optionalFieldOf("amplifier", Range.of(0, -1)).forGetter(StatusEffectAffliction::amplifier),
            Codec.INT.optionalFieldOf("chance", 0).forGetter(StatusEffectAffliction::chance)
    ).apply(instance, StatusEffectAffliction::new));
    public static final PacketCodec<RegistryByteBuf, StatusEffectAffliction> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.registryEntry(RegistryKeys.STATUS_EFFECT), StatusEffectAffliction::effect,
            Range.PACKET_CODEC, StatusEffectAffliction::seconds,
            Range.PACKET_CODEC, StatusEffectAffliction::amplifier,
            PacketCodecs.INTEGER, StatusEffectAffliction::chance,
            StatusEffectAffliction::new
    );

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.APPLY_STATUS_EFFECT;
    }

    @Override
    public void afflict(PlayerEntity player, ItemStack stack) {
        if (player.getWorld().isClient || (chance > 0 && player.getWorld().random.nextInt(chance) > 0)) {
            return;
        }

        float health = player.getHealth();
        float oldMaxHealth = player.getMaxHealth();
        StatusEffectInstance current = player.getStatusEffect(effect);
        player.addStatusEffect(new StatusEffectInstance(effect,
                seconds.getClamped(current == null ? 0 : current.getDuration(), 20),
                amplifier.getClamped(current == null ? 0 : current.getAmplifier(), 1)
        ));
        // keep original health
        if (player.getMaxHealth() != oldMaxHealth) {
            player.setHealth(MathHelper.clamp(health, 0, player.getMaxHealth()));
        }
    }

    @Override
    public Text getName() {
        MutableText text = effect.value().getName().copy();

        if (amplifier.min() > 0) {
            text = Text.translatable("potion.withAmplifier", text, Text.translatable("potion.potency." + (amplifier.min())));
        }

        float tickRate = MinecraftClient.getInstance().world == null ? 20 : MinecraftClient.getInstance().world.getTickManager().getTickRate();
        text = Text.translatable("potion.withDuration", text, StringHelper.formatTicks(seconds.min() * 20, tickRate));

        if (chance > 0) {
            text = Text.translatable("potion.withChance", chance, text);
        }
        return text;
    }
}
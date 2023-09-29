package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.Optional;
import java.util.function.Predicate;

import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.dynamic.Codecs;

public record TraitIngredient (
        Optional<SpellTraits> min,
        Optional<SpellTraits> max
    ) implements Predicate<SpellTraits> {
    public static final Codec<TraitIngredient> CODEC = Codecs.xor(
            SpellTraits.CODEC.flatXmap(
                    traits -> DataResult.success(new TraitIngredient(Optional.ofNullable(traits), Optional.empty())),
                    ingredient -> ingredient.min().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Cannot serialize an empty trait ingredient"))),
            RecordCodecBuilder.<TraitIngredient>create(instance -> instance.group(
                    SpellTraits.CODEC.optionalFieldOf("min").forGetter(TraitIngredient::min),
                    SpellTraits.CODEC.optionalFieldOf("max").forGetter(TraitIngredient::max)
            ).apply(instance, TraitIngredient::new))
    ).flatXmap(
            either -> either.left().or(either::right).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Invalid traits")),
            ingredient -> DataResult.success(ingredient.max.isPresent() ? Either.left(ingredient) : Either.right(ingredient))
    );

    @Override
    public boolean test(SpellTraits t) {
        boolean minMatch = min.map(m -> t.includes(m)).orElse(true);
        boolean maxMatch = max.map(m -> m.includes(t)).orElse(true);
        return minMatch && maxMatch;
    }

    public void write(PacketByteBuf buf) {
        buf.writeOptional(min, (b, m) -> m.write(b));
        buf.writeOptional(max, (b, m) -> m.write(b));
    }

    public static TraitIngredient fromPacket(PacketByteBuf buf) {
        return new TraitIngredient(SpellTraits.fromPacketOrEmpty(buf), SpellTraits.fromPacketOrEmpty(buf));
    }
}

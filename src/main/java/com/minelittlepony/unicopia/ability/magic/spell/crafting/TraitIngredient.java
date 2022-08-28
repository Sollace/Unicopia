package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.Optional;
import java.util.function.Predicate;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.JsonHelper;

public record TraitIngredient (
        Optional<SpellTraits> min,
        Optional<SpellTraits> max
    ) implements Predicate<SpellTraits> {

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
        return new TraitIngredient(SpellTraits.fromPacket(buf), SpellTraits.fromPacket(buf));
    }

    public static TraitIngredient fromJson(JsonObject json) {
        Optional<SpellTraits> min = Optional.empty();
        Optional<SpellTraits> max = Optional.empty();

        if (json.has("min") || json.has("max")) {
            if (json.has("min")) {
                min = SpellTraits.fromJson(JsonHelper.getObject(json, "min"));
            }
            if (json.has("max")) {
                max = SpellTraits.fromJson(JsonHelper.getObject(json, "max"));
            }
        } else {
            min = SpellTraits.fromJson(json);
        }

        return new TraitIngredient(min, max);
    }
}

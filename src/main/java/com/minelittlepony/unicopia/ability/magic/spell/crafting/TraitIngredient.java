package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.Optional;
import java.util.function.Predicate;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.JsonHelper;

public class TraitIngredient implements Predicate<SpellTraits> {

    private Optional<SpellTraits> min = Optional.empty();
    private Optional<SpellTraits> max = Optional.empty();

    private TraitIngredient() {}

    @Override
    public boolean test(SpellTraits t) {
        boolean minMatch = min.map(m -> t.includes(m)).orElse(true);
        boolean maxMatch = max.map(m -> m.includes(t)).orElse(true);
        return minMatch && maxMatch;
    }

    public void write(PacketByteBuf buf) {
        min.ifPresentOrElse(m -> {
            buf.writeBoolean(true);
            m.write(buf);
        }, () -> buf.writeBoolean(false));
        max.ifPresentOrElse(m -> {
            buf.writeBoolean(true);
            m.write(buf);
        }, () -> buf.writeBoolean(false));
    }

    public static TraitIngredient fromPacket(PacketByteBuf buf) {
        TraitIngredient ingredient = new TraitIngredient();

        if (buf.readBoolean()) {
            ingredient.min = SpellTraits.fromPacket(buf);
        }
        if (buf.readBoolean()) {
            ingredient.max = SpellTraits.fromPacket(buf);
        }

        return ingredient;
    }

    public static TraitIngredient fromJson(JsonObject json) {

        TraitIngredient ingredient = new TraitIngredient();

        if (json.has("min") || json.has("max")) {
            if (json.has("min")) {
                ingredient.min = SpellTraits.fromJson(JsonHelper.getObject(json, "min"));
            }
            if (json.has("max")) {
                ingredient.max = SpellTraits.fromJson(JsonHelper.getObject(json, "max"));
            }
        } else {
            ingredient.min = SpellTraits.fromJson(json);
        }

        return ingredient;
    }
}

package com.minelittlepony.unicopia.advancement;

import java.util.Set;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.serialization.JsonOps;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class TraitDiscoveredCriterion extends AbstractCriterion<TraitDiscoveredCriterion.Conditions> {
    private static final Identifier ID = Unicopia.id("trait_discovered");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject json, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer deserializer) {
        return new Conditions(playerPredicate, Trait.SET_CODEC.decode(JsonOps.INSTANCE, JsonHelper.getArray(json, "traits")).result().get().getFirst());
    }

    public static Conditions create(Set<Trait> traits) {
        return new Conditions(LootContextPredicate.EMPTY, traits);
    }

    public void trigger(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            trigger((ServerPlayerEntity)player, c -> c.test((ServerPlayerEntity)player));
        }
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final Set<Trait> traits;

        public Conditions(LootContextPredicate playerPredicate, Set<Trait> traits) {
            super(ID, playerPredicate);
            this.traits = traits;
        }

        public boolean test(ServerPlayerEntity player) {
            return Pony.of(player).getDiscoveries().isKnown(traits);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.add("traits", Trait.SET_CODEC.encodeStart(JsonOps.INSTANCE, traits).result().get());
            return json;
        }
    }
}

package com.minelittlepony.unicopia.advancement;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class CustomEventCriterion extends AbstractCriterion<CustomEventCriterion.Conditions> {

    private static final Identifier ID = new Identifier("unicopia", "custom");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject json, Extended playerPredicate, AdvancementEntityPredicateDeserializer deserializer) {

        Set<Race> races = new HashSet<>();

        if (json.has("race")) {
            json.get("race").getAsJsonArray().forEach(el -> {
                races.add(Race.fromName(el.getAsString(), Race.EARTH));
            });
        }

        return new Conditions(
                playerPredicate,
                JsonHelper.getString(json, "event"),
                races,
                json.has("flying") ? json.get("flying").getAsBoolean() : null,
                JsonHelper.getInt(json, "repeats", 0)
        );
    }

    public CustomEventCriterion.Trigger createTrigger(String name) {
        return player -> {
            if (player instanceof ServerPlayerEntity p) {
                int counter = Pony.of(player).getAdvancementProgress().compute(name, (key, i) -> i == null ? 1 : i + 1);

                trigger(p, c -> c.test(name, counter, p));
            }
        };
    }

    public interface Trigger {
        void trigger(@Nullable PlayerEntity player);
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final String event;

        private final Set<Race> races;

        private final Boolean flying;

        private final int repeatCount;

        public Conditions(Extended playerPredicate, String event, Set<Race> races, Boolean flying, int repeatCount) {
            super(ID, playerPredicate);
            this.event = event;
            this.races = races;
            this.flying = flying;
            this.repeatCount = repeatCount;
        }

        public boolean test(String event, int count, ServerPlayerEntity player) {
            return this.event.equalsIgnoreCase(event)
                    && (races.isEmpty() || races.contains(Pony.of(player).getSpecies()))
                    && (flying == null || flying == Pony.of(player).getPhysics().isFlying())
                    && (repeatCount <= 0 || (count > 0 && count % repeatCount == 0));
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.addProperty("event", event);
            if (!races.isEmpty()) {
                JsonArray arr = new JsonArray();
                races.forEach(r -> arr.add(Race.REGISTRY.getId(r).toString()));
                json.add("race", arr);
            }
            if (flying != null) {
                json.addProperty("flying", flying);
            }
            if (repeatCount > 0) {
                json.addProperty("repeats", repeatCount);
            }
            return json;
        }
    }
}

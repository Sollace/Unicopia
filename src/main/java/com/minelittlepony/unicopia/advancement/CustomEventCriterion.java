package com.minelittlepony.unicopia.advancement;

import com.google.gson.JsonObject;

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
    protected Conditions conditionsFromJson(JsonObject json, Extended player, AdvancementEntityPredicateDeserializer deserializer) {
        return new Conditions(player, JsonHelper.getString(json, "event"));
    }

    public CustomEventCriterion.Trigger createTrigger(String name) {
        return player -> {
            if (player instanceof ServerPlayerEntity) {
                test((ServerPlayerEntity)player, c -> c.test(name));
            }
        };
    }

    public interface Trigger {
        void trigger(PlayerEntity player);
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final String event;

        public Conditions(Extended playerPredicate, String event) {
            super(ID, playerPredicate);
            this.event = event;
        }

        public boolean test(String event) {
            return this.event.equalsIgnoreCase(event);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer serializer) {
            JsonObject json = super.toJson(serializer);
            json.addProperty("event", event);
            return json;
        }
    }
}

package com.minelittlepony.unicopia.advancement;

import java.util.Optional;
import java.util.function.BiConsumer;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.TypeFilter;

public class SendViaDragonBreathScrollCriterion extends AbstractCriterion<SendViaDragonBreathScrollCriterion.Conditions> {
    @Override
    protected Conditions conditionsFromJson(JsonObject json, Optional<LootContextPredicate> playerPredicate, AdvancementEntityPredicateDeserializer deserializer) {
        return new Conditions(playerPredicate,
                ItemPredicate.fromJson(json.get("item")),
                JsonHelper.getBoolean(json, "is_receiving_end", false),
                json.has("recipient_name") ? Optional.of(JsonHelper.getString(json, "recipient_name")) : Optional.empty(),
                json.has("recipient_present") ? TriState.of(JsonHelper.getBoolean(json, "recipient_present")) : TriState.DEFAULT,
                json.has("counter") ? Optional.of(JsonHelper.getString(json, "counter")) : Optional.empty(),
                RacePredicate.fromJson(json.get("race"))
        );
    }

    public void triggerSent(PlayerEntity player, ItemStack payload, String recipient, BiConsumer<String, Integer> counterCallback) {
        if (player instanceof ServerPlayerEntity spe) {
            trigger(spe, c -> {
                if (c.test(spe, payload, recipient, false)) {
                    c.counter.ifPresent(counter -> {
                        counterCallback.accept(counter, Pony.of(spe).getAdvancementProgress().compute(counter, (key, i) -> i == null ? 1 : i + 1));
                    });
                    return true;
                }
                return false;
            });
        }
    }

    public void triggerReceived(LivingEntity recipient, ItemStack payload) {
        if (recipient instanceof ServerPlayerEntity spe) {
            trigger(spe, c -> c.test(spe, payload, recipient.getDisplayName().getString(), true));
        }
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final Optional<ItemPredicate> item;
        private final boolean isReceivingEnd;
        private final Optional<String> recipientName;
        private final TriState recipientPresent;
        private final Optional<String> counter;
        private final RacePredicate races;

        public Conditions(Optional<LootContextPredicate> playerPredicate, Optional<ItemPredicate> item, boolean isReceivingEnd, Optional<String> recipient, TriState recipientPresent, Optional<String> counter, RacePredicate races) {
            super(playerPredicate);
            this.item = item;
            this.isReceivingEnd = isReceivingEnd;
            this.recipientName = recipient;
            this.recipientPresent = recipientPresent;
            this.counter = counter;
            this.races = races;
        }

        public boolean test(ServerPlayerEntity player, ItemStack payload, String recipient, boolean receiving) {
            return isReceivingEnd == receiving
                    && races.test(player)
                    && (item.isEmpty() || item.get().test(payload))
                    && recipientName.map(expectedRecipientname -> recipient.equalsIgnoreCase(expectedRecipientname)).orElse(true)
                    && (recipientPresent == TriState.DEFAULT || isRecipientAbsent(player.getServerWorld(), recipient) != recipientPresent.get());
        }

        private boolean isRecipientAbsent(ServerWorld world, String recipient) {
            return world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), e -> e.hasCustomName() && e.getCustomName().getString().equalsIgnoreCase(recipient)).isEmpty();
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = super.toJson();
            item.ifPresent(item -> json.add("item", item.toJson()));
            json.add("race", races.toJson());
            recipientName.ifPresent(recipient -> json.addProperty("recipient_name", recipient));
            if (recipientPresent != TriState.DEFAULT) {
                json.addProperty("recipient_present", recipientPresent.getBoxed());
            }
            counter.ifPresent(counter -> json.addProperty("counter", counter));
            return json;
        }
    }
}

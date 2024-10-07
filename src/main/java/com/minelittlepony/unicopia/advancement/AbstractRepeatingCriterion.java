package com.minelittlepony.unicopia.advancement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class AbstractRepeatingCriterion<T extends AbstractRepeatingCriterion.Conditions> implements Criterion<T> {
    private final Map<PlayerAdvancementTracker, Set<Criterion.ConditionsContainer<T>>> progressions = new IdentityHashMap<>();

    @Override
    public final void beginTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<T> conditions) {
        progressions.computeIfAbsent(manager, m -> new HashSet<>()).add(conditions);
    }

    @Override
    public final void endTrackingCondition(PlayerAdvancementTracker manager, Criterion.ConditionsContainer<T> conditions) {
        var set = progressions.get(manager);
        if (set != null) {
            set.remove(conditions);
            if (set.isEmpty()) {
                progressions.remove(manager);
            }
        }
    }

    @Override
    public final void endTracking(PlayerAdvancementTracker tracker) {
        progressions.remove(tracker);
    }

    protected void trigger(ServerPlayerEntity player, BiPredicate<Integer, T> predicate) {
        PlayerAdvancementTracker tracker = player.getAdvancementTracker();
        TriggerCountTracker counter = Pony.of(player).getAdvancementProgress();
        counter.removeGranted(player, tracker);

        var advancements = progressions.get(tracker);
        if (advancements != null && !advancements.isEmpty()) {
            LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, player);
            List<Criterion.ConditionsContainer<T>> matches = null;

            for (var condition : advancements) {
                T conditions = condition.conditions();
                if (predicate.test(counter.update(condition.advancement(), condition.id()), conditions)) {
                    var playerPredicate = conditions.player();
                    if (playerPredicate.isEmpty() || playerPredicate.get().test(lootContext)) {
                        if (matches == null) {
                            matches = new ArrayList<>();
                        }

                        matches.add(condition);
                    }
                }
            }

            if (matches != null) {
                for (var advancement : matches) {
                    advancement.grant(tracker);
                }
                counter.removeGranted(player, tracker);
            }
        }
    }

    public interface Conditions extends AbstractCriterion.Conditions {
    }
}

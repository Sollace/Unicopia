package com.minelittlepony.unicopia.advancement;

import java.util.Optional;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * Advantement trigger for the book of holding. It's an achievement to die so spectacularly! :D
 */
public class BOHDeathCriterion extends AbstractCriterion<BOHDeathCriterion.Entry, BOHDeathCriterion.Conditions> {
    // TODO:  Need to register this
    public static final BOHDeathCriterion INSTANCE = new BOHDeathCriterion();

    private static final Identifier ID = new Identifier("unicopia", "death_by_bag_of_holding");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public Conditions conditionsFromJson(JsonObject json, JsonDeserializationContext context) {
        return new Conditions(AdvancementPredicate.deserialize(json.get("advancement")));
    }

    @Override
    protected Entry createEntry(PlayerAdvancementTracker advancement) {
        return new Entry(advancement);
    }

    public void trigger(ServerPlayerEntity player) {
        PlayerAdvancementTracker key = player.getAdvancementManager();

        Optional.ofNullable(listeners.get(key)).ifPresent(e -> {
            e.trigger((ServerWorld)player.world, key);
        });
    }

    static class Conditions extends AbstractCriterionConditions {

        private final AdvancementPredicate requirement;

        public Conditions(AdvancementPredicate key) {
            super(ID);

            requirement = key;
        }

        public boolean test(ServerWorld world, PlayerAdvancementTracker stracker) {
            return requirement.test(world, stracker);
        }

    }

    static class Entry extends AbstractCriterion.Entry<BOHDeathCriterion.Conditions> {

        Entry(PlayerAdvancementTracker key) {
            super(key);
        }

        public void trigger(ServerWorld world, PlayerAdvancementTracker tracker) {
            listeners.stream()
                .filter(listener -> listener.getConditions().test(world, tracker))
                .forEach(winner -> winner.apply(advancement));
        }
    }
}

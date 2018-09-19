package com.minelittlepony.unicopia.advancements;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

/**
 * Advantement trigger for the book of holding. It's an achievement to die so spectacularly! :D
 */
public class BOHDeathTrigger implements ICriterionTrigger<BOHDeathTrigger.Instance> {

    private static final ResourceLocation ID = new ResourceLocation("unicopia", "death_by_bag_of_holding");

    private final Map<PlayerAdvancements, Entry> listeners = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements key, Listener<Instance> listener) {
        listeners.computeIfAbsent(key, Entry::new).listeners.add(listener);;
    }

    @Override
    public void removeListener(PlayerAdvancements key, Listener<Instance> listener) {
        if (listeners.containsKey(key)) {
            Entry entry = listeners.get(key);

            entry.listeners.remove(listener);
            if (entry.listeners.isEmpty()) {
                listeners.remove(key);
            }
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements key) {
        if (listeners.containsKey(key)) {
            listeners.remove(key);
        }
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(AdvancementPredicate.deserialize(json.get("advancement")));
    }

    public void trigger(EntityPlayerMP player) {
        PlayerAdvancements key = player.getAdvancements();

        Optional.ofNullable(listeners.get(key)).ifPresent(e -> {
            e.trigger((WorldServer)player.world, key);
        });
    }

    static class Instance extends AbstractCriterionInstance {

        AdvancementPredicate requirement;

        public Instance(AdvancementPredicate requirement) {
            super(ID);

            this.requirement = requirement;
        }

        public boolean test(WorldServer world, PlayerAdvancements playerAdvancements) {
            return requirement.test(world, playerAdvancements);
        }

    }

    class Entry {
        private final PlayerAdvancements advancement;

        private final List<Listener<Instance>> listeners = Lists.newArrayList();

        Entry(PlayerAdvancements key) {
            advancement = key;
        }

        public void trigger(WorldServer world, PlayerAdvancements playerAdvancements) {
            listeners.stream()
                .filter(listener -> listener.getCriterionInstance().test(world, playerAdvancements))
                .forEach(winner -> winner.grantCriterion(advancement));;
        }
    }
}

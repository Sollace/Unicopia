package com.minelittlepony.unicopia.advancements;

import java.util.Optional;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

/**
 * Advantement trigger for the book of holding. It's an achievement to die so spectacularly! :D
 */
public class BOHDeathTrigger extends AbstractTrigger<BOHDeathTrigger.Entry, BOHDeathTrigger.Instance> {

    private static final ResourceLocation ID = new ResourceLocation("unicopia", "death_by_bag_of_holding");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(AdvancementPredicate.deserialize(json.get("advancement")));
    }

    @Override
    protected Entry createEntry(PlayerAdvancements advancement) {
        return new Entry(advancement);
    }

    public void trigger(EntityPlayerMP player) {
        PlayerAdvancements key = player.getAdvancements();

        Optional.ofNullable(listeners.get(key)).ifPresent(e -> {
            e.trigger((WorldServer)player.world, key);
        });
    }

    static class Instance extends AbstractCriterionInstance {

        private final AdvancementPredicate requirement;

        public Instance(AdvancementPredicate key) {
            super(ID);

            requirement = key;
        }

        public boolean test(WorldServer world, PlayerAdvancements playerAdvancements) {
            return requirement.test(world, playerAdvancements);
        }

    }

    static class Entry extends AbstractTrigger.Entry<BOHDeathTrigger.Instance> {

        Entry(PlayerAdvancements key) {
            super(key);
        }

        public void trigger(WorldServer world, PlayerAdvancements playerAdvancements) {
            listeners.stream()
                .filter(listener -> listener.getCriterionInstance().test(world, playerAdvancements))
                .forEach(winner -> winner.grantCriterion(advancement));;
        }
    }

}

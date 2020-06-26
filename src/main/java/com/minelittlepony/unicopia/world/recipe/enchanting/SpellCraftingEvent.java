package com.minelittlepony.unicopia.world.recipe.enchanting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.world.item.MagicGemItem;

import net.minecraft.item.ItemStack;

/**
 * A basic event for unlocking a page when a gem is crafted for the given spell
 */
public class SpellCraftingEvent {

    public static void trigger(PageOwner owner, ItemStack stack, @Nullable IPageUnlockListener unlockListener) {
        Pages.instance().triggerUnlockEvent(owner, new Event(stack), unlockListener);
    }

    static class Event implements IUnlockEvent {
        final ItemStack stack;

        Event(ItemStack stack) {
            this.stack = stack;
        }
    }

    static class Condition implements IUnlockCondition<Event> {
        @Nonnull
        Affinity affinity;

        @Expose
        String spell;

        Condition(JsonObject json) {
            require(json, "affinity");
            require(json, "spell");

            affinity = Affinity.of(json.get("affinity").getAsString());
            spell = json.get("spell").getAsString();
        }

        @Override
        public boolean accepts(IUnlockEvent event) {
            return event instanceof Event;
        }

        @Override
        public boolean matches(PageOwner prop, Event event) {
            if (!event.stack.isEmpty() && event.stack.getItem() instanceof MagicGemItem) {
                return ((MagicGemItem)event.stack.getItem()).getAffinity() == affinity
                    && SpellRegistry.getKeyFromStack(event.stack).equals(spell);
            }

            return false;
        }
    }
}

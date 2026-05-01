package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.item.consume.DeathConsumeEffect;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;

public class TotemOfDying {
    public static void tryUseTotem(DamageSource damage, LivingEntity entity) {
        ItemStack totem = getTotem(entity, true);

        if (!totem.isEmpty()) {
            if (entity instanceof ServerPlayerEntity player) {
                player.incrementStat(Stats.USED.getOrCreateStat(totem.getItem()));
                Criteria.USED_TOTEM.trigger(player, totem);
                player.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }

            for (var effect : totem.get(UDataComponentTypes.DEATH_CAUSING).deathEffects()) {
                if (effect instanceof DeathConsumeEffect o) {
                    o.onConsume(entity.getWorld(), totem, entity, damage);
                }
            }
        }
    }

    public static ItemStack getTotem(LivingEntity entity, boolean consume) {
        for (Hand hand : Hand.values()) {
            ItemStack stack = entity.getStackInHand(hand);
            if (stack.contains(UDataComponentTypes.DEATH_CAUSING)) {
                return consume ? stack.split(1) : stack;
            }
        }

        return ItemStack.EMPTY;
    }
}

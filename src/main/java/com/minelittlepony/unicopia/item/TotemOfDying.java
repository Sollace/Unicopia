package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgEntityStatus;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;

public class TotemOfDying {
    public static void tryUseTotem(DamageSource damage, LivingEntity entity) {
        ItemStack totem = getTotem(entity, true);

        if (!totem.isEmpty()) {
            if (entity instanceof ServerPlayerEntity player) {
                player.incrementStat(Stats.USED.getOrCreateStat(Items.TOTEM_OF_UNDYING));
                Criteria.USED_TOTEM.trigger(player, totem);
                player.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
            }

            Channel.ENTITY_STATUS.sendToSurroundingPlayers(new MsgEntityStatus(entity.getId(), MsgEntityStatus.USE_TOTEM_OF_DYING), entity);
            VecHelper.findInRange(entity, entity.getWorld(), entity.getPos(), 10, e -> e instanceof LivingEntity & !SpellType.SHIELD.isOn(e)).forEach(e -> {
                Channel.ENTITY_STATUS.sendToSurroundingPlayers(new MsgEntityStatus(e.getId(), MsgEntityStatus.USE_TOTEM_OF_DYING), e);
                e.damage(damage, Integer.MAX_VALUE);
            });
        }
    }

    public static ItemStack getTotem(LivingEntity entity, boolean consume) {
        for (Hand hand : Hand.values()) {
            ItemStack stack = entity.getStackInHand(hand);
            if (stack.isOf(UItems.TOTEM_OF_DYING)) {
                if (consume) {
                    ItemStack totem = stack.copy();
                    stack.decrement(1);
                    return totem;
                }
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}

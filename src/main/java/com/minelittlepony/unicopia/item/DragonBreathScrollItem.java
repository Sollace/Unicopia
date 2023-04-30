package com.minelittlepony.unicopia.item;

import java.util.UUID;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.server.world.DragonBreathStore;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class DragonBreathScrollItem extends Item {

    public DragonBreathScrollItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        ItemStack payload = player.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);

        if (payload.isEmpty() || !stack.hasCustomName()) {
            return TypedActionResult.fail(stack);
        }

        stack.split(1);
        if (!world.isClient) {
            if (payload.getItem() == UItems.OATS) {
                UCriteria.SEND_OATS.trigger(player);
            }
            DragonBreathStore.get(world).put(stack.getName().getString(), payload.split(1));
        }
        player.playSound(USounds.ITEM_DRAGON_BREATH_SCROLL_USE, 1, 1);
        return TypedActionResult.consume(stack);
    }

    public static ItemStack setRecipient(ItemStack stack, UUID recipient) {
        stack.getOrCreateSubNbt("recipient").putUuid("id", recipient);
        return stack;
    }
}

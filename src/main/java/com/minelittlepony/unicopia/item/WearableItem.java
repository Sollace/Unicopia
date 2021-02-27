package com.minelittlepony.unicopia.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Wearable;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public abstract class WearableItem extends Item implements Wearable {

    public WearableItem(FabricItemSettings settings) {
        super(settings.equipmentSlot(s -> ((WearableItem)s.getItem()).getPreferredSlot(s)));
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        EquipmentSlot slot = MobEntity.getPreferredEquipmentSlot(stack);
        ItemStack currentArmor = player.getEquippedStack(slot);

        if (currentArmor.isEmpty()) {
            ItemStack result = stack.copy();
            result.setCount(1);
            player.equipStack(slot, result);
            stack.decrement(1);
            return TypedActionResult.success(stack, world.isClient());
        }

        return TypedActionResult.fail(stack);
    }

    public EquipmentSlot getPreferredSlot(ItemStack stack) {
        return EquipmentSlot.OFFHAND;
    }
}

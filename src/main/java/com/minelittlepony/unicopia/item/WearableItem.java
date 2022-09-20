package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.trinkets.TrinketsDelegate;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Wearable;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public abstract class WearableItem extends Item implements Wearable {

    public WearableItem(FabricItemSettings settings) {
        super(configureEquipmentSlotSupplier(settings));
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
        TrinketsDelegate.getInstance().registerTrinket(this);
    }

    private static FabricItemSettings configureEquipmentSlotSupplier(FabricItemSettings settings) {
        if (TrinketsDelegate.hasTrinkets()) {
            return settings;
        }
        return settings.equipmentSlot(s -> ((WearableItem)s.getItem()).getPreferredSlot(s));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        return TrinketsDelegate.getInstance().getAvailableTrinketSlots(player, TrinketsDelegate.ALL).stream()
                .findAny()
                .filter(slotId -> TrinketsDelegate.getInstance().equipStack(player, slotId, stack))
                .map(slotId -> TypedActionResult.success(stack, world.isClient()))
                .orElseGet(() -> TypedActionResult.fail(stack));
    }

    @Override
    public SoundEvent getEquipSound() {
        return ArmorMaterials.LEATHER.getEquipSound();
    }

    public EquipmentSlot getPreferredSlot(ItemStack stack) {
        return EquipmentSlot.OFFHAND;
    }
}

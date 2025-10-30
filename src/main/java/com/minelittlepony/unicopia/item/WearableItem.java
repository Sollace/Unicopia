package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

public abstract class WearableItem extends Item {

    public WearableItem(Item.Settings settings, RegistryEntry<SoundEvent> equipSound) {
        super(configureEquipmentSlotSupplier(settings.component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.OFFHAND)
                .equipSound(equipSound)
                .build())));
        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
        TrinketsDelegate.getInstance(null).registerTrinket(this);
    }

    private static Item.Settings configureEquipmentSlotSupplier(Item.Settings settings) {
        if (TrinketsDelegate.hasTrinkets()) {
            return settings;
        }
        return settings.equipmentSlot((e, s) -> ((WearableItem)s.getItem()).getSlotType(s));
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        return TrinketsDelegate.getInstance(player).equipStack(player, player.getStackInHand(hand)) ? ActionResult.SUCCESS : ActionResult.FAIL;
    }

    public EquipmentSlot getSlotType(ItemStack stack) {
        return EquipmentSlot.OFFHAND;
    }

    public static boolean dispenseArmor(BlockPointer pointer, ItemStack armor) {
        return pointer.world().getEntitiesByClass(
                    LivingEntity.class,
                    new Box(pointer.pos().offset(pointer.state().get(DispenserBlock.FACING))),
                    EntityPredicates.EXCEPT_SPECTATOR
                )
                .stream()
                .filter(entity -> TrinketsDelegate.getInstance(entity).equipStack(entity, armor))
                .findFirst()
                .isPresent();
    }

    private static final DispenserBehavior DISPENSER_BEHAVIOR = new ItemDispenserBehavior(){
        @Override
        protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            return dispenseArmor(pointer, stack) ? stack : super.dispenseSilently(pointer, stack);
        }
    };
}

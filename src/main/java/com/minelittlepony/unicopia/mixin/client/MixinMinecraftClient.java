package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minelittlepony.unicopia.ducks.PickedItemSupplier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

@Mixin(MinecraftClient.class)
abstract class MixinMinecraftClient {
    @Redirect(method = "doItemPick()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/SpawnEggItem;forEntity(Lnet/minecraft/entity/EntityType;)Lnet/minecraft/item/SpawnEggItem;")
    )
    private SpawnEggItem redirectSpawnEggForEntity(EntityType<?> type) {

        MinecraftClient self = MinecraftClient.getInstance();

        Entity entity = ((EntityHitResult)self.crosshairTarget).getEntity();

        if (!(entity instanceof PickedItemSupplier)) {
            return SpawnEggItem.forEntity(type);
        }

        ItemStack pickedStack = ((PickedItemSupplier) entity).getPickedStack();

        PlayerInventory inventory = self.player.inventory;

        int i = inventory.getSlotWithStack(pickedStack);

        if (self.player.abilities.creativeMode) {
           inventory.addPickBlock(pickedStack);
           self.interactionManager.clickCreativeStack(self.player.getStackInHand(Hand.MAIN_HAND), 36 + inventory.selectedSlot);
        } else if (i != -1) {
           if (PlayerInventory.isValidHotbarIndex(i)) {
              inventory.selectedSlot = i;
           } else {
              self.interactionManager.pickFromInventory(i);
           }
        }

        return null;
    }
}

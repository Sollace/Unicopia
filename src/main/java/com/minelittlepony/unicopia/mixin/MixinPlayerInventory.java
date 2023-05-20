package com.minelittlepony.unicopia.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;

@Mixin(PlayerInventory.class)
abstract class MixinPlayerInventory implements Inventory, Nameable {
    @Shadow
    public @Final PlayerEntity player;
    @Shadow
    private @Final List<DefaultedList<ItemStack>> combinedInventory;

    @Nullable
    private List<DefaultedList<ItemStack>> storedCombinedInventory;

    @Inject(method = "dropAll()V", at = @At("HEAD"))
    public void beforeDropAll(CallbackInfo info) {
        storedCombinedInventory = combinedInventory.stream().map(l -> DefaultedList.ofSize(l.size(), ItemStack.EMPTY)).toList();
        for (int group = 0; group < combinedInventory.size(); group++) {
            var original = combinedInventory.get(group);
            for (int i = 0; i < original.size(); i++) {
                ItemStack stack = original.get(i);
                if (EnchantmentHelper.getLevel(Enchantments.BINDING_CURSE, stack) == 0
                    && EnchantmentUtil.consumeEnchantment(UEnchantments.HEART_BOUND, 1, stack, player.world.random, EnchantmentUtil.getLuck(3, player))) {
                    original.set(i, ItemStack.EMPTY);
                    UCriteria.USE_SOULMATE.trigger(player);
                    storedCombinedInventory.get(group).set(i, stack);
                }
            }
        }
    }

    @Inject(method = "dropAll()V", at = @At("TAIL"))
    public void afterDropAll(CallbackInfo info) {
        if (storedCombinedInventory != null) {
            for (int group = 0; group < combinedInventory.size(); group++) {
                var original = combinedInventory.get(group);
                for (int i = 0; i < original.size(); i++) {
                    ItemStack stored = storedCombinedInventory.get(group).get(i);
                    if (!stored.isEmpty()) {
                        original.set(i, stored);
                    }
                }
            }
        }
    }
}

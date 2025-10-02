package com.minelittlepony.unicopia.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.UTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

@Mixin(targets = "net/minecraft/recipe/Ingredient$StackEntry")
abstract class MixinIngredient_StackEntry {
    @Shadow
    private @Final ItemStack stack;

    @ModifyReturnValue(method = "getStacks", at = @At("RETURN"))
    private Collection<ItemStack> getImplicitStacks(Collection<ItemStack> stacks) {
        if (stacks.size() == 1 && stacks.iterator().next() == stack) {
            if (stack.isOf(Items.STICK)) {
                return unicopia$getStacksFromTag(ConventionalItemTags.WOODEN_RODS);
            }
            if (stack.isOf(Items.FEATHER)) {
                return unicopia$getStacksFromTag(UTags.Items.MAGIC_FEATHERS);
            }
        }
        return stacks;
    }

    @Unique
    private Collection<ItemStack> unicopia$getStacksFromTag(TagKey<Item> key) {
        List<ItemStack> list = new ArrayList<>();
        list.add(stack);
        for (RegistryEntry<Item> registryEntry : Registries.ITEM.iterateEntries(key)) {
            if (registryEntry.value() != stack.getItem()) {
                list.add(stack.withItem(registryEntry.value()));
            }
        }

        return list;
    }
}

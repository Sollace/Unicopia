package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.UTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;

@Mixin(Ingredient.class)
abstract class MixinIngredient {
    @Shadow @Mutable
    private @Final RegistryEntryList<Item> entries;

    @SuppressWarnings({ "deprecation" })
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(RegistryEntryList<Item> entries, CallbackInfo info) {

        entries.getStorage().ifRight(stacks -> {
            if (stacks.size() == 1) {
                if (stacks.getFirst().matches(Items.STICK.getRegistryEntry())) {
                    this.entries = RegistryEntryList.of(Registries.ITEM, ConventionalItemTags.WOODEN_RODS);
                }
                if (stacks.getFirst().matches(Items.FEATHER.getRegistryEntry())) {
                    this.entries = RegistryEntryList.of(Registries.ITEM, UTags.Items.MAGIC_FEATHERS);
                }
            }
        });
    }
}

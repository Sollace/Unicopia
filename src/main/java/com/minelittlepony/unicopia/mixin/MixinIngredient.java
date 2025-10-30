package com.minelittlepony.unicopia.mixin;

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import com.minelittlepony.unicopia.UTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

@Mixin(Ingredient.class)
abstract class MixinIngredient {
    @Shadow @Mutable
    private @Final RegistryEntryList<Item> entries;

    @SuppressWarnings({ "unchecked", "deprecation" })
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(RegistryEntryList<Item> entries) {
        this.entries = RegistryEntryList.of(entries.stream().map(entry -> {
            if (entry.matches(Items.STICK.getRegistryEntry())) {
                return unicopia$getStacksFromTag(entry, ConventionalItemTags.WOODEN_RODS);
            }
            if (entry.matches(Items.FEATHER.getRegistryEntry())) {
                return unicopia$getStacksFromTag(entry, UTags.Items.MAGIC_FEATHERS);
            }
            return Stream.of(entry);
        }).distinct().toArray(RegistryEntry[]::new));
    }


    private Stream<RegistryEntry<Item>> unicopia$getStacksFromTag(RegistryEntry<Item> include, TagKey<Item> tag) {
        return Stream.concat(Stream.of(include), Registries.ITEM.getOrThrow(tag).stream()).distinct();
    }
}

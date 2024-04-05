package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Map;
import java.util.NoSuchElementException;

import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.recipe.CloudShapingRecipe;
import com.mojang.datafixers.util.Either;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.SingleItemRecipeJsonBuilder;
import net.minecraft.data.server.recipe.VanillaRecipeProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface CraftingMaterialHelper {
    Map<String, TagKey<Item>> MATERIALS = Map.of(
            "wood", ItemTags.PLANKS,
            "stone", ItemTags.STONE_TOOL_MATERIALS,
            "iron", ConventionalItemTags.IRON_INGOTS,
            "gold", ConventionalItemTags.GOLD_INGOTS,
            "copper", ConventionalItemTags.COPPER_INGOTS,
            "netherite", ConventionalItemTags.NETHERITE_INGOTS
    );

    static Either<ItemConvertible, TagKey<Item>> getMaterial(Item output, String toStrip, String suffex) {
        Identifier id = Registries.ITEM.getId(output).withPath(p -> p.replace(toStrip, "") + suffex);
        TagKey<Item> tag = MATERIALS.getOrDefault(id.getPath().replace("en_", "_").split("_")[0], null);
        if (tag != null) {
            return Either.right(tag);
        }
        return Either.left(
            Registries.ITEM.getOrEmpty(id)
                .or(() -> Registries.ITEM.getOrEmpty(new Identifier(Identifier.DEFAULT_NAMESPACE, id.getPath())))
                .or(() -> Registries.ITEM.getOrEmpty(new Identifier(Identifier.DEFAULT_NAMESPACE, id.getPath().replace(suffex, ""))))
                .orElseThrow(() -> new NoSuchElementException("No item with id " + id))
        );
    }

    static Item getItem(Identifier id) {
        return Registries.ITEM.getOrEmpty(id).orElseThrow(() -> new NoSuchElementException("No item with id " + id));
    }

    static ShapedRecipeJsonBuilder input(ShapedRecipeJsonBuilder builder, char key, Either<ItemConvertible, TagKey<Item>> material) {
        material.ifLeft(i -> builder.input(key, i));
        material.ifRight(i -> builder.input(key, i));
        return builder;
    }

    static AdvancementCriterion<?> conditionsFromEither(Either<ItemConvertible, TagKey<Item>> material) {
        return material.map(RecipeProvider::conditionsFromItem, RecipeProvider::conditionsFromTag);
    }

    static String hasEither(Either<ItemConvertible, TagKey<Item>> material) {
        return material.map(VanillaRecipeProvider::hasItem, CraftingMaterialHelper::hasTag);
    }

    static String hasTag(TagKey<Item> tag) {
        return "has_" + tag.id();
    }

    static AdvancementCriterion<?> conditionsFromSpell(ItemConvertible gem, SpellType<?> spell) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("spell", spell.getId().toString());
        return RecipeProvider.conditionsFromItemPredicates(ItemPredicate.Builder.create()
                .items(gem)
                .nbt(nbt)
                .build()
        );
    }

    static String hasSpell(SpellType<?> spell) {
        return "has_" + spell.getId() + "_gemstone";
    }

    static SingleItemRecipeJsonBuilder createCloudShaping(Ingredient input, RecipeCategory category, ItemConvertible output, int count) {
        return new SingleItemRecipeJsonBuilder(category, CloudShapingRecipe::new, input, output, count);
    }
}

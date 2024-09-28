package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.*;
import com.minelittlepony.unicopia.server.world.gen.ULootTableEntryType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.collection.DefaultedList;

public interface URecipes {
    Codec<DefaultedList<Ingredient>> SHAPELESS_RECIPE_INGREDIENTS_CODEC = Ingredient.DISALLOW_EMPTY_CODEC.listOf().flatXmap(ingredients -> {
        Ingredient[] ingredients2 = ingredients.stream().filter(ingredient -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
        if (ingredients2.length == 0) {
            return DataResult.error(() -> "No ingredients for shapeless recipe");
        }
        if (ingredients2.length > 9) {
            return DataResult.error(() -> "Too many ingredients for shapeless recipe");
        }
        return DataResult.success(DefaultedList.copyOf(Ingredient.EMPTY, ingredients2));
    }, DataResult::success);

    RecipeType<SpellbookRecipe> SPELLBOOK = RecipeType.register("unicopia:spellbook");
    RecipeType<StonecuttingRecipe> CLOUD_SHAPING = RecipeType.register("unicopia:cloud_shaping");
    RecipeType<TransformCropsRecipe> GROWING = RecipeType.register("unicopia:growing");

    RecipeSerializer<ZapAppleRecipe> ZAP_APPLE_SERIALIZER = register("crafting_zap_apple", ZapAppleRecipe.CODEC, ZapAppleRecipe.PACKET_CODEC);
    RecipeSerializer<GlowingRecipe> GLOWING_SERIALIZER = register("crafting_glowing", new SpecialRecipeSerializer<>(GlowingRecipe::new));
    RecipeSerializer<JarInsertRecipe> JAR_INSERT_SERIALIZER = register("jar_insert", new SpecialRecipeSerializer<>(JarInsertRecipe::new));
    RecipeSerializer<JarExtractRecipe> JAR_EXTRACT_SERIALIZER = register("jar_extract", new SpecialRecipeSerializer<>(JarExtractRecipe::new));
    RecipeSerializer<SpellShapedCraftingRecipe> CRAFTING_MAGICAL_SERIALIZER = register("crafting_magical", SpellShapedCraftingRecipe.CODEC, SpellShapedCraftingRecipe.PACKET_CODEC);
    RecipeSerializer<SpellCraftingRecipe> TRAIT_REQUIREMENT = register("spellbook/crafting", SpellCraftingRecipe.CODEC, SpellCraftingRecipe.PACKET_CODEC);
    RecipeSerializer<SpellEnhancingRecipe> TRAIT_COMBINING = register("spellbook/combining", SpellEnhancingRecipe.CODEC, SpellEnhancingRecipe.PACKET_CODEC);
    RecipeSerializer<SpellDuplicatingRecipe> SPELL_DUPLICATING = register("spellbook/duplicating", SpellDuplicatingRecipe.CODEC, SpellDuplicatingRecipe.PACKET_CODEC);
    RecipeSerializer<CloudShapingRecipe> CLOUD_SHAPING_SERIALIZER = register("cloud_shaping", new CuttingRecipe.Serializer<>(CloudShapingRecipe::new) {});
    RecipeSerializer<TransformCropsRecipe> TRANSFORM_CROP_SERIALIZER = register("transform_crop", TransformCropsRecipe.CODEC, TransformCropsRecipe.PACKET_CODEC);

    static <T extends Recipe<?>> RecipeSerializer<T> register(String name, MapCodec<T> codec, PacketCodec<RegistryByteBuf, T> packetCodec) {
        return register(name, new RecipeSerializer<>() {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public PacketCodec<RegistryByteBuf, T> packetCodec() {
                return packetCodec;
            }
        });
    }

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String name, S serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Unicopia.id(name), serializer);
    }

    static void bootstrap() {
        ULootTableEntryType.bootstrap();
    }
}
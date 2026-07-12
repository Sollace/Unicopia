package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

public class CuttingBoardRecipeJsonBuilder {
    private final Map<String, AdvancementCriterion<?>> criterions = new LinkedHashMap<>();

    private final ItemConvertible output;
    private Either<Ingredient, Tool> tool;

    private final List<Result> results = new ArrayList<>();
    private final List<Ingredient> ingredients = new ArrayList<>();

    private Identifier sound = Identifier.ofVanilla("item.axe.strip");

    public static CuttingBoardRecipeJsonBuilder create(ItemConvertible output, String action) {
        return new CuttingBoardRecipeJsonBuilder(output).tool(action);
    }

    public static CuttingBoardRecipeJsonBuilder create(ItemConvertible output, Ingredient tool) {
        return new CuttingBoardRecipeJsonBuilder(output).tool(tool);
    }

    protected CuttingBoardRecipeJsonBuilder(ItemConvertible output) {
        this.output = output;
    }

    public CuttingBoardRecipeJsonBuilder tool(Ingredient tool) {
        this.tool = Either.left(tool);
        return this;
    }

    public CuttingBoardRecipeJsonBuilder tool(String action) {
        this.tool = Either.right(new Tool(Identifier.of("farmersdelight:tool_action"), action));
        return this;
    }

    public CuttingBoardRecipeJsonBuilder sound(SoundEvent sound) {
        this.sound = Registries.SOUND_EVENT.getId(sound);
        return this;
    }

    public CuttingBoardRecipeJsonBuilder input(ItemConvertible input) {
        ingredients.add(Ingredient.ofItems(input));
        return this;
    }

    public CuttingBoardRecipeJsonBuilder result(ItemConvertible result, int count) {
        results.add(new Result(Registries.ITEM.getId(result.asItem()), count));
        return this;
    }

    public CuttingBoardRecipeJsonBuilder result(ItemConvertible result) {
        return result(result, 1);
    }

    public CuttingBoardRecipeJsonBuilder result(Identifier result) {
        return result(result, 1);
    }

    public CuttingBoardRecipeJsonBuilder result(Identifier result, int count) {
        results.add(new Result(result, count));
        return this;
    }

    public CuttingBoardRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> condition) {
        criterions.put(name, condition);
        return this;
    }

    public void offerTo(RecipeExporter exporter, Identifier id) {
        id = id.withPrefixedPath("cutting/");
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + id);
        Advancement.Builder advancementBuilder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        criterions.forEach(advancementBuilder::criterion);
        exporter.accept(id,
            new CuttingBoardRecipe(ingredients, tool, sound, results),
            advancementBuilder.build(id.withPrefixedPath("recipes/"))
        );
    }

    public void offerTo(RecipeExporter exporter) {
        offerTo(exporter, Registries.ITEM.getId(output.asItem()));
    }

    public void offerTo(RecipeExporter exporter, String recipePath) {
        Identifier outputItemId = Registries.ITEM.getId(output.asItem());
        Identifier recipeId = outputItemId.withPath(recipePath);
        if (recipeId.equals(outputItemId)) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        offerTo(exporter, recipeId);
    }

    public record Tool(Identifier type, String action) {
        static final Codec<Tool> CODEC = RecordCodecBuilder.create(ii -> ii.group(
                Identifier.CODEC.fieldOf("fabric:type").forGetter(Tool::type),
                Codec.STRING.fieldOf("action").forGetter(Tool::action)
        ).apply(ii, Tool::new));
    }
    public record Result(Identifier item, int count) {
        public static final Codec<Result> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("item").forGetter(Result::item),
            Codecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(Result::count)
        ).apply(i, Result::new));
    }

    public record CuttingBoardRecipe(
            List<Ingredient> ingredients,
            Either<Ingredient, Tool> tool,
            Identifier sound,
            List<Result> result
        ) implements Recipe<CraftingRecipeInput> {
        static final Identifier ID = Identifier.of("farmersdelight", "cutting");
        static final Codec<Either<Ingredient, Tool>> TOOL_CODEC = Codec.xor(Ingredient.DISALLOW_EMPTY_CODEC, Tool.CODEC);
        static final MapCodec<CuttingBoardRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("ingredients").forGetter(CuttingBoardRecipe::ingredients),
                TOOL_CODEC.fieldOf("tool").forGetter(CuttingBoardRecipe::tool),
                Identifier.CODEC.fieldOf("sound").forGetter(CuttingBoardRecipe::sound),
                Result.CODEC.listOf().fieldOf("result").forGetter(CuttingBoardRecipe::result)
        ).apply(i, CuttingBoardRecipe::new));
        static final RecipeType<CuttingBoardRecipe> TYPE = Registry.register(Registries.RECIPE_TYPE, ID, new RecipeType<>() {
            @Override
            public String toString() {
                return "farmersdelight:cutting";
            }
        });
        static final RecipeSerializer<CuttingBoardRecipe> SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, ID, new RecipeSerializer<>() {
            @Override
            public MapCodec<CuttingBoardRecipe> codec() { return CODEC; }
            @Override
            public PacketCodec<RegistryByteBuf, CuttingBoardRecipe> packetCodec() { return null; }
        });
        public static void bootstrap() {}

        @Override
        public boolean matches(CraftingRecipeInput inventory, World world) { return false; }

        @Override
        public ItemStack craft(CraftingRecipeInput inventory, WrapperLookup registryManager) { return ItemStack.EMPTY; }

        @Override
        public boolean fits(int width, int height) { return false; }

        @Override
        public ItemStack getResult(WrapperLookup registryManager) { return ItemStack.EMPTY; }

        @Override
        public RecipeSerializer<?> getSerializer() { return SERIALIZER; }

        @Override
        public RecipeType<?> getType() { return TYPE; }
    }
}

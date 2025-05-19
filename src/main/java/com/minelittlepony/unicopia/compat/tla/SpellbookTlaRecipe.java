package com.minelittlepony.unicopia.compat.tla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellDuplicatingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellEnhancingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookScreen;
import com.minelittlepony.unicopia.container.inventory.HexagonalCraftingGrid;
import com.minelittlepony.unicopia.recipe.URecipes;
import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.gui.TextureConfig;
import io.github.mattidragon.tlaapi.api.gui.TlaBounds;
import io.github.mattidragon.tlaapi.api.plugin.PluginContext;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import io.github.mattidragon.tlaapi.api.recipe.TlaRecipe;
import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

class SpellbookTlaRecipe implements TlaRecipe, SpellbookRecipe.CraftingTreeBuilder {
    private final RecipeEntry<SpellbookRecipe> recipe;

    protected final List<TraitedTlaIngredient> inputs = new ArrayList<>();
    private final List<TlaStack> outputs = new ArrayList<>();

    private final Supplier<List<TlaIngredient>> ingredients;

    static void generate(RecipeCategory category, PluginContext context) {
        context.addGenerator(client -> client.world.getRecipeManager().listAllOfType(URecipes.SPELLBOOK).stream().flatMap(recipe -> {
            if (recipe.value() instanceof SpellDuplicatingRecipe) {
                return Stream.of(new SpellDuplicatingTlaRecipe(recipe));
            }

            if (recipe.value() instanceof SpellEnhancingRecipe) {
                return Trait.all().stream().map(trait -> new SpellEnhancingTlaRecipe(recipe, trait));
            }

            return Stream.of((TlaRecipe)new SpellbookTlaRecipe(recipe));
        }).toList());
    }

    public SpellbookTlaRecipe(RecipeEntry<SpellbookRecipe> recipe) {
        this.recipe = recipe;
        this.ingredients = Suppliers.memoize(() -> inputs.stream().map(TraitedTlaIngredient::ingredient).toList());
        recipe.value().buildCraftingTree(this);
    }

    @Override
    public RecipeCategory getCategory() {
        return RecipeCategory.SPELL_BOOK;
    }

    @Nullable
    @Override
    public Identifier getId() {
        return recipe.id();
    }

    @Override
    public List<TlaIngredient> getInputs() {
        return ingredients.get();
    }

    @Override
    public List<TlaStack> getOutputs() {
        return outputs;
    }

    @Override
    public List<TlaIngredient> getCatalysts() {
        return List.of();
    }

    @Override
    public void buildGui(GuiBuilder builder) {
        TlaBounds bounds = builder.getBounds();
        builder.addTexture(TextureConfig.builder().texture(SpellbookScreen.TEXTURE)
                .size(bounds.width(), bounds.height())
                .uv(50, 50)
                .regionSize(128, 128)
                .textureSize(512, 256)
                .build(), 0, 0);
        builder.addTexture(Main.EMPTY_ARROW, 85, 30);

        List<HexagonalCraftingGrid.Slot> grid = new ArrayList<>();
        List<HexagonalCraftingGrid.Slot> gem = new ArrayList<>();
        HexagonalCraftingGrid.create(-34, -5, 3, grid, gem);

        int currentInput = 1;

        for (int i = 0; i < grid.size(); i++) {
            var slot = grid.get(i);

            if (currentInput < inputs.size() && slot.weight() == 1) {
                inputs.get(currentInput++).buildGui(slot, builder);
            } else if (slot.weight() == 1) {
                inputs.get(0).buildGui(TlaIngredient.EMPTY, slot, builder);
            }
        }

        inputs.get(0).buildGui(gem.get(0), builder);
        builder.addSlot(getOutput(), 120, 25).makeLarge().markOutput();
    }

    protected TlaIngredient getOutput() {
        return TlaIngredient.ofStacks(outputs);
    }

    @Override
    public void input(ItemStack... stacks) {
        inputs.add(TraitedTlaIngredient.of(TlaIngredient.ofStacks(Arrays.stream(stacks).map(TlaStack::of).toList())));
    }

    @Override
    public void input(Trait... traits) {
        inputs.add(TraitedTlaIngredient.of(List.of(traits), 1));
    }

    @Override
    public void input(Trait trait, float value) {
        inputs.add(TraitedTlaIngredient.of(trait, value));
    }

    @Override
    public void result(ItemStack... stack) {
        outputs.addAll(Arrays.stream(stack).map(TlaStack::of).toList());
    }
}
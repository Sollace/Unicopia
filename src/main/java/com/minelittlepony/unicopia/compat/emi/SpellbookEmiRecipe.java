package com.minelittlepony.unicopia.compat.emi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookScreen;
import com.minelittlepony.unicopia.container.inventory.HexagonalCraftingGrid;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

class SpellbookEmiRecipe implements EmiRecipe, SpellbookRecipe.CraftingTreeBuilder {

    private final SpellbookRecipe recipe;

    private final List<EmiIngredient> inputs = new ArrayList<>();
    private final List<EmiStack> outputs = new ArrayList<>();

    public SpellbookEmiRecipe(SpellbookRecipe recipe) {
        this.recipe = recipe;
        recipe.buildCraftingTree(this);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return Main.SPELL_BOOK_CATEGORY;
    }

    @Nullable
    @Override
    public Identifier getId() {
        return recipe.getId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 75;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(SpellbookScreen.TEXTURE, 0, 0, getDisplayWidth(), getDisplayHeight(), 50, 50, 128, 128, 512, 256);
        widgets.addTexture(Main.EMPTY_ARROW, 85, 30);

        List<HexagonalCraftingGrid.Slot> grid = new ArrayList<>();
        List<HexagonalCraftingGrid.Slot> gem = new ArrayList<>();
        HexagonalCraftingGrid.create(-34, -5, 3, grid, gem);

        int currentInput = 1;

        for (int i = 0; i < grid.size(); i++) {
            var slot = grid.get(i);

            if (currentInput < inputs.size() && slot.weight() == 1) {
                widgets.add(new SlotTexture(slot));
                widgets.addSlot(inputs.get(currentInput++), slot.left(), slot.top()).drawBack(false);
            } else if (slot.weight() == 1) {
                widgets.add(new SlotTexture(slot));
                widgets.addSlot(slot.left(), slot.top()).drawBack(false);
            }
        }
        widgets.addSlot(inputs.get(0), gem.get(0).left(), gem.get(0).top()).drawBack(false);
        widgets.addSlot(getOutput(), 120, 25).large(true).recipeContext(this);
    }

    protected EmiIngredient getOutput() {
        return EmiIngredient.of(outputs);
    }

    @Override
    public void input(ItemStack... stacks) {
        inputs.add(EmiIngredient.of(Arrays.stream(stacks).map(EmiStack::of).toList()));
    }

    @Override
    public void input(Trait... traits) {
        inputs.add(EmiIngredient.of(Arrays.stream(traits).map(trait -> new TraitEmiStack(trait, 1)).toList()));
    }

    @Override
    public void input(Trait trait, float value) {
        inputs.add(new TraitEmiStack(trait, value));
    }

    @Override
    public void result(ItemStack... stack) {
        outputs.addAll(Arrays.stream(stack).map(EmiStack::of).toList());
    }

    static class SlotTexture extends TextureWidget {

        public SlotTexture(HexagonalCraftingGrid.Slot slot) {
            super(SpellbookScreen.SLOT, slot.left() - 7, slot.top() - 7, 32, 32, 0, 0, 32, 32, 32, 32);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            RenderSystem.enableBlend();
            context.drawTexture(texture, x, y, 0, u, v, width, height, textureWidth, textureHeight);
        }
    }
}
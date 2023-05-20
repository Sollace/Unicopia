package com.minelittlepony.unicopia.compat.emi;

import java.util.Arrays;
import java.util.List;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class SpellDuplicatingEmiRecipe extends SpellbookEmiRecipe {

    public SpellDuplicatingEmiRecipe(SpellbookRecipe recipe) {
        super(recipe);
    }

    @Override
    public void input(ItemStack... stacks) {
        getInputs().add(new CyclingRecipeIngredient(Arrays.stream(stacks).map(EmiStack::of).toList(), 1));
    }

    @Override
    protected EmiIngredient getOutput() {
        return new CyclingRecipeIngredient(getOutputs(), 2);
    }

    static class CyclingRecipeIngredient extends ListEmiIngredient {

        private final List<? extends EmiIngredient> ingredients;
        private final int maxCount;

        public CyclingRecipeIngredient(List<? extends EmiIngredient> ingredients, long amount) {
            super(ingredients, amount);
            this.ingredients = ingredients;
            this.maxCount = ingredients.size();
        }

        @Override
        public void render(MatrixStack matrices, int x, int y, float delta, int flags) {

            if (maxCount < 2 || MinecraftClient.getInstance().player == null) {
                super.render(matrices, x, y, delta, flags);
            } else {
                int tick = (MinecraftClient.getInstance().player.age / 12) % maxCount;
                if ((flags & RENDER_AMOUNT) != 0) {
                    String count = "";
                    if (getAmount() != 1) {
                        count += getAmount();
                    }
                    EmiRenderHelper.renderAmount(matrices, x, y, EmiPort.literal(count));
                }
                ingredients.get(tick).render(matrices, x, y, delta, flags & ~RENDER_AMOUNT);
            }
        }
    }
}

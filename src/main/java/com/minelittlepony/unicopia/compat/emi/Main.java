package com.minelittlepony.unicopia.compat.emi;

import java.util.Arrays;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellDuplicatingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellEnhancingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellShapedCraftingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.URecipes;
import com.minelittlepony.unicopia.item.group.MultiItem;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class Main implements EmiPlugin {
    static final EmiStack SPELL_BOOK_STATION = EmiStack.of(UItems.SPELLBOOK);
    static final EmiRecipeCategory SPELL_BOOK_CATEGORY = new EmiRecipeCategory(Unicopia.id("spellbook"), SPELL_BOOK_STATION, SPELL_BOOK_STATION);

    static final Identifier WIDGETS = Unicopia.id("textures/gui/widgets.png");
    static final EmiTexture EMPTY_ARROW = new EmiTexture(WIDGETS, 44, 0, 24, 17);

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(SPELL_BOOK_CATEGORY);
        registry.addWorkstation(SPELL_BOOK_CATEGORY, SPELL_BOOK_STATION);
        registry.getRecipeManager().listAllOfType(URecipes.SPELLBOOK).forEach(recipe -> {

            if (recipe instanceof SpellDuplicatingRecipe) {
                registry.addRecipe(new SpellDuplicatingEmiRecipe(recipe));
            } else if (recipe instanceof SpellEnhancingRecipe enhancingRecipe) {
                Trait.all().forEach(trait -> {
                    registry.addRecipe(new SpellDuplicatingEmiRecipe(recipe) {
                        private final Identifier id;

                        {
                            id = recipe.getId().withPath(p -> p + "/" + trait.getId().getPath());
                            input(trait);
                            this.getOutputs().addAll(
                                Arrays.stream(enhancingRecipe.getBaseMaterial().getMatchingStacks())
                                    .map(stack -> EmiStack.of(SpellTraits.of(stack).add(new SpellTraits.Builder().with(trait, 1).build()).applyTo(stack)).comparison(c -> Comparison.DEFAULT_COMPARISON))
                                    .toList()
                            );
                        }

                        @Nullable
                        @Override
                        public Identifier getId() {
                            return id;
                        }
                    });
                });
            } else {
                registry.addRecipe(new SpellbookEmiRecipe(recipe));
            }
        });

        Stream.of(UItems.GEMSTONE, UItems.BOTCHED_GEM, UItems.MAGIC_STAFF, UItems.FILLED_JAR).forEach(item -> {
            registry.setDefaultComparison(item, comparison -> Comparison.compareNbt());
        });

        DynamicRegistryManager registries = DynamicRegistryManager.of(Registries.REGISTRIES);
        registry.getRecipeManager().listAllOfType(RecipeType.CRAFTING).stream()
                .filter(recipe -> recipe instanceof SpellShapedCraftingRecipe)
                .map(SpellShapedCraftingRecipe.class::cast).forEach(recipe -> {
            ItemStack output = recipe.getOutput(registries);
            if (output.getItem() instanceof MultiItem multiItem && output.getItem() instanceof EnchantableItem enchantable) {
                multiItem.getDefaultStacks().forEach(outputVariation -> {
                    var spellEffect = enchantable.getSpellEffect(outputVariation);
                    if (!spellEffect.isEmpty()) {
                        registry.addRecipe(new MagicalShapedEmiRecipe(recipe, spellEffect, outputVariation));
                    }
                });
            }
        });
    }
}

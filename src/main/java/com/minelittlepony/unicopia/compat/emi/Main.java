package com.minelittlepony.unicopia.compat.emi;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellDuplicatingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellEnhancingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellShapedCraftingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.block.state.Schematic;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.TransformCropsRecipe;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.URecipes;
import com.minelittlepony.unicopia.item.group.MultiItem;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiStonecuttingRecipe;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class Main implements EmiPlugin {
    static final EmiStack SPELL_BOOK_STATION = EmiStack.of(UItems.SPELLBOOK);
    static final EmiStack CLOUD_SHAPING_STATION = EmiStack.of(UBlocks.SHAPING_BENCH);
    static final EmiStack GROWING_STATION = EmiStack.of(Blocks.FARMLAND);
    static final EmiStack ALTAR_STATION = EmiStack.of(Blocks.CRYING_OBSIDIAN);
    static final EmiRecipeCategory SPELL_BOOK_CATEGORY = new EmiRecipeCategory(Unicopia.id("spellbook"), SPELL_BOOK_STATION, SPELL_BOOK_STATION);
    static final EmiRecipeCategory CLOUD_SHAPING_CATEGORY = new EmiRecipeCategory(Unicopia.id("cloud_shaping"), CLOUD_SHAPING_STATION, CLOUD_SHAPING_STATION);
    static final EmiRecipeCategory GROWING_CATEGORY = new EmiRecipeCategory(Unicopia.id("growing"), GROWING_STATION, GROWING_STATION);
    static final EmiRecipeCategory ALTAR_CATEGORY = new EmiRecipeCategory(Unicopia.id("altar"), ALTAR_STATION, ALTAR_STATION);

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

        registry.addCategory(CLOUD_SHAPING_CATEGORY);
        registry.addWorkstation(CLOUD_SHAPING_CATEGORY, CLOUD_SHAPING_STATION);
        registry.getRecipeManager().listAllOfType(URecipes.CLOUD_SHAPING).forEach(recipe -> {
            registry.addRecipe(new EmiStonecuttingRecipe(recipe) {
                @Override
                public EmiRecipeCategory getCategory() {
                    return CLOUD_SHAPING_CATEGORY;
                }
            });
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

        registry.addCategory(GROWING_CATEGORY);
        registry.addWorkstation(GROWING_CATEGORY, GROWING_STATION);
        registry.getRecipeManager().listAllOfType(URecipes.GROWING).forEach(recipe -> {
            registry.addRecipe(new StructureInteractionEmiRecipe(
                    GROWING_CATEGORY,
                    recipe.getId(),
                    new Schematic.Builder()
                        .fill(0, 0, 0, 6, 0, 6, recipe.getCatalystState())
                        .set(3, 0, 3, Blocks.FARMLAND.getDefaultState())
                        .set(3, 1, 3, recipe.getTargetState())
                        .build(),
                    List.of(EmiStack.of(recipe.getTarget()), EmiStack.of(recipe.getCatalyst(), TransformCropsRecipe.AREA)),
                    EmiStack.of(recipe.getOutput()),
                    Unicopia.id("textures/gui/ability/grow.png")
            ));
        });

        registry.addCategory(ALTAR_CATEGORY);
        registry.addWorkstation(ALTAR_CATEGORY, ALTAR_STATION);
        registry.addRecipe(new StructureInteractionEmiRecipe(
                ALTAR_CATEGORY,
                Unicopia.id("altar/spectral_clock"),
                Schematic.ALTAR,
                List.of(
                    EmiStack.of(Items.CLOCK),
                    EmiStack.of(UItems.SPELLBOOK),
                    EmiStack.of(Blocks.SOUL_SAND),
                    EmiStack.of(Blocks.LODESTONE),
                    EmiStack.of(Blocks.OBSIDIAN, 8 * 4 + 8)
                ),
                EmiStack.of(UItems.SPECTRAL_CLOCK),
                Unicopia.id("textures/gui/race/alicorn.png")
        ));
    }
}

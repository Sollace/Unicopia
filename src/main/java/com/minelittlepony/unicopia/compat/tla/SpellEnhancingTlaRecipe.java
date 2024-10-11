package com.minelittlepony.unicopia.compat.tla;

import java.util.Arrays;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellEnhancingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;

import io.github.mattidragon.tlaapi.api.recipe.TlaStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

class SpellEnhancingTlaRecipe extends SpellbookTlaRecipe {

    private final Identifier id;

    public SpellEnhancingTlaRecipe(RecipeEntry<SpellbookRecipe> recipe, Trait trait) {
        super(recipe);
        id = recipe.id().withPath(p -> p + "/" + trait.getId().getPath());
        input(trait);
        getOutputs().addAll(
                Arrays.stream(((SpellEnhancingRecipe)recipe.value()).getBaseMaterial().getMatchingStacks())
                .map(stack -> TlaStack.of(SpellTraits.of(stack).add(new SpellTraits.Builder().with(trait, 1).build()).applyTo(stack)))
                .toList()
        );
    }

    @Override
    public Identifier getId() {
        return id;
    }
}

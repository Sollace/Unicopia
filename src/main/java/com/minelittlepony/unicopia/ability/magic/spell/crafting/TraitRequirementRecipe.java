package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler.SpellbookInventory;
import com.minelittlepony.unicopia.item.GemstoneItem;
import com.minelittlepony.unicopia.item.URecipes;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class TraitRequirementRecipe implements SpellbookRecipe {
    private final Identifier id;
    private final IngredientWithSpell requirement;
    private final TraitIngredient requiredTraits;
    private final ItemStack output;

    private TraitRequirementRecipe(Identifier id, IngredientWithSpell requirement, TraitIngredient requiredTraits, ItemStack output) {
        this.id = id;
        this.requirement = requirement;
        this.requiredTraits = requiredTraits;
        this.output = output;
    }

    @Override
    public void buildCraftingTree(CraftingTreeBuilder builder) {
        builder.input(requirement.getMatchingStacks());
        requiredTraits.min().ifPresent(min -> {
            min.forEach(e -> builder.input(e.getKey(), e.getValue()));
        });
        builder.result(output);
    }

    @Override
    public boolean matches(SpellbookInventory inventory, World world) {
        return requirement.test(inventory.getItemToModify())
            && requiredTraits.test(SpellTraits.union(
                    inventory.getTraits(),
                    SpellTraits.of(output)
            ));
    }

    @Override
    public ItemStack craft(SpellbookInventory inventory) {
        return SpellTraits.union(
                SpellTraits.of(inventory.getItemToModify()),
                inventory.getTraits(),
                SpellTraits.of(output)
        ).applyTo(output);
    }

    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 0;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.TRAIT_REQUIREMENT;
    }

    public static ItemStack outputFromJson(JsonObject json) {
        ItemStack stack = ShapedRecipe.outputFromJson(json);
        SpellTraits.fromJson(JsonHelper.getObject(json, "traits", new JsonObject()))
            .map(traits -> traits.applyTo(stack)).orElse(stack);

        SpellType<?> spell = SpellType.getKey(Identifier.tryParse(JsonHelper.getString(json, "spell", "")));
        if (spell != SpellType.EMPTY_KEY) {
            return GemstoneItem.enchant(stack, spell);
        }
        return stack;
    }

    public static class Serializer implements RecipeSerializer<TraitRequirementRecipe> {
        @Override
        public TraitRequirementRecipe read(Identifier id, JsonObject json) {
            return new TraitRequirementRecipe(id,
                    IngredientWithSpell.fromJson(JsonHelper.getObject(json, "material")),
                    TraitIngredient.fromJson(JsonHelper.getObject(json, "traits")),
                    outputFromJson(JsonHelper.getObject(json, "result")));
        }

        @Override
        public TraitRequirementRecipe read(Identifier id, PacketByteBuf buf) {
            return new TraitRequirementRecipe(id,
                    IngredientWithSpell.fromPacket(buf),
                    TraitIngredient.fromPacket(buf),
                    buf.readItemStack()
            );
        }

        @Override
        public void write(PacketByteBuf buf, TraitRequirementRecipe recipe) {
            recipe.requirement.write(buf);
            recipe.requiredTraits.write(buf);
            buf.writeItemStack(recipe.output);
        }
    }
}

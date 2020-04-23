package com.minelittlepony.unicopia.enchanting.recipe;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.container.SpellBookInventory;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public class SpellRecipe extends AbstractSpecialRecipe {

	private final String spellId;

	public SpellRecipe(Identifier id, SpellIngredient spellitem, String spellName, DefaultedList<SpellIngredient> ingredients) {
	    super(id, spellitem, ingredients);
	    this.spellId = spellName;
	}

    @Override
    public ItemStack getRecipeKindIcon() {
       return new ItemStack(UItems.spellbook);
    }

	@Override
	public ItemStack craft(CraftingInventory inv) {
        SpellBookInventory inventory = (SpellBookInventory)inv;

        Inventory craftResult = inventory.getCraftResultMatrix();
        ItemStack stackToEnchant = craftResult.getInvStack(0);

        return SpellRegistry.instance().enchantStack(stackToEnchant, spellId);
	}

	@Override
	public ItemStack getOutput() {
		return SpellRegistry.instance().enchantStack(super.getOutput(), spellId);
	}

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.SPELL_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return URecipes.SPELL_BOOK;
    }

    public static class Serializer implements RecipeSerializer<SpellRecipe> {
        @Override
        public SpellRecipe read(Identifier id, JsonObject json) {
            JsonObject resultJson = json.get("result").getAsJsonObject();

            return new SpellRecipe(id,
                    SpellIngredient.single(resultJson),
                    resultJson.get("spell").getAsString(),
                    SpellIngredient.multiple(json)
            );
        }

        @Override
        public SpellRecipe read(Identifier id, PacketByteBuf buff) {
            SpellIngredient spellItem = SpellIngredient.SERIALIZER.read(buff);
            String spellName = buff.readString();

            int size = buff.readInt();
            DefaultedList<SpellIngredient> ingredients = DefaultedList.ofSize(0, SpellIngredient.EMPTY);

            while (ingredients.size() < size) {
                ingredients.add(SpellIngredient.SERIALIZER.read(buff));
            }

            return new SpellRecipe(id,
                    spellItem,
                    spellName,
                    ingredients);
        }

        @Override
        public void write(PacketByteBuf buff, SpellRecipe recipe) {
            recipe.getSpellItem().write(buff);
            buff.writeString(recipe.spellId);
            DefaultedList<SpellIngredient> ingredients = recipe.getSpellIngredients();
            buff.writeInt(ingredients.size());
            ingredients.forEach(i -> i.write(buff));
        }
    }

}

package com.minelittlepony.unicopia.enchanting;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.init.UItems;
import com.minelittlepony.unicopia.inventory.InventorySpellBook;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

public class SpellRecipe extends Impl<IRecipe> implements IRecipe {

    private final SpellIngredient spellitem;

	private final String spellId;

	private final NonNullList<SpellIngredient> ingredients;

	public static IRecipe deserialize(JsonObject json) {

	    NonNullList<SpellIngredient> ingredients = NonNullList.create();

	    for (JsonElement i : json.get("ingredients").getAsJsonArray()) {
	        SpellIngredient ingredient = SpellIngredient.parse(i);

	        if (ingredient != null) {
	            ingredients.add(ingredient);
	        }
	    }

	    if (ingredients.isEmpty()) {
	        throw new JsonParseException("Recipe cannot have 0 ingredients");
	    }

	    json = json.get("result").getAsJsonObject();

	    String spellId = json.get("spell").getAsString();

	    SpellIngredient result = SpellIngredient.parse(json.get("item"));

	    if (result == null) {
	        throw new JsonParseException("Recipe cannot have no enchantable input");
	    }

	    return new SpellRecipe(result, spellId, ingredients);
	}

	public SpellRecipe(SpellIngredient spellitem, String spellName, NonNullList<SpellIngredient> ingredients) {
	    this.spellitem = spellitem;
	    this.spellId = spellName;
		this.ingredients = ingredients;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		ItemStack enchantedStack = ((InventorySpellBook)inv).getCraftResultMatrix().getStackInSlot(0);

		if (enchantedStack.isEmpty() || enchantedStack.getItem() == null) {
		    return false;
		}

		if (!spellitem.matches(enchantedStack, enchantedStack.getCount())) {
		    return false;
		}

		int materialMult = enchantedStack.getCount();

		ArrayList<SpellIngredient> toMatch = Lists.newArrayList(ingredients);

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);

			if (!stack.isEmpty()) {
    			if (toMatch.isEmpty() || !removeMatch(toMatch, stack, materialMult)) {
    			    return false;
    			}
			}
		}
		return toMatch.isEmpty();
	}

	private boolean removeMatch(List<SpellIngredient> toMatch, ItemStack stack, int materialMult) {
	    return toMatch.stream()
	            .filter(s -> s.matches(stack, materialMult))
	            .findFirst()
	            .filter(toMatch::remove)
	            .isPresent();
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		return getRecipeOutput();
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height < ingredients.size();
	}

	@Override
	public ItemStack getRecipeOutput() {
		return SpellRegistry.instance().enchantStack(new ItemStack(UItems.spell, 1), spellId);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> remainers = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < remainers.size(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack != null && stack.getItem().hasContainerItem(stack)) {
                remainers.set(i, new ItemStack(stack.getItem().getContainerItem()));
            }
        }

        return remainers;
	}


}

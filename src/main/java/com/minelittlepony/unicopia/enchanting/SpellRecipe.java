package com.minelittlepony.unicopia.enchanting;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.inventory.gui.SpellBookInventory;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;

public class SpellRecipe extends AbstractSpecialRecipe {

	private final String spellId;

	public static IRecipe deserialize(JsonObject json) {

	    JsonObject resultJson = json.get("result").getAsJsonObject();

	    return new SpellRecipe(
	            parseSingleIngredient(resultJson),
	            resultJson.get("spell").getAsString(),
	            parseIngrediants(json)
        );
	}

	public SpellRecipe(SpellIngredient spellitem, String spellName, NonNullList<SpellIngredient> ingredients) {
	    super(spellitem, ingredients);
	    this.spellId = spellName;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
        SpellBookInventory inventory = (SpellBookInventory)inv;

        IInventory craftResult = inventory.getCraftResultMatrix();
        ItemStack stackToEnchant = craftResult.getStackInSlot(0);

        return SpellRegistry.getInstance().enchantStack(stackToEnchant, spellId);
	}

	@Override
	public ItemStack getRecipeOutput() {
		return SpellRegistry.getInstance().enchantStack(super.getRecipeOutput(), spellId);
	}
}

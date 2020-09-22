package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.item.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class VanillaOverrides {

    public static final Registry<Item> REGISTRY = Registries.createSimple(new Identifier("unicopia", "overrides"));

    private static <T extends Item> T register(String name, T newItem) {
        return Registry.register(REGISTRY, new Identifier(name), newItem);
    }

    static {
        register("apple", new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)));
    }
}
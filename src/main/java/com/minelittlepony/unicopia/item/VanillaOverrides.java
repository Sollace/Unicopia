package com.minelittlepony.unicopia.item;

import net.minecraft.item.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

public final class VanillaOverrides {

    public static final Registry<Item> REGISTRY = new SimpleRegistry<>();

    private static <T extends Item> T register(String name, T newItem) {
        return Registry.register(REGISTRY, new Identifier(name), newItem);
    }

    static {
        register("stick", new StickItem(new Item.Settings().group(ItemGroup.MATERIALS)));
        register("shears", new ExtendedShearsItem(new Item.Settings().maxDamage(238).group(ItemGroup.TOOLS)));
        register("apple", new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)));
    }
}
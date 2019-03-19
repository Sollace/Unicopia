package com.minelittlepony.unicopia.forgebullshit;

import com.minelittlepony.unicopia.UClient;

import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public class ItemRegistrar {
    /**
     * Registers items and all the necessary chaff that comes with.
     */
    public static void registerAll(IForgeRegistry<Item> registry, Item...items) {
        registry.registerAll(items);

        if (UClient.isClientSide()) {
            ItemModels.registerAll(items);
        }
    }
}

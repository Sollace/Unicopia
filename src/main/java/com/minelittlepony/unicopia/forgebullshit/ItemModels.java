package com.minelittlepony.unicopia.forgebullshit;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;

@Deprecated
public class ItemModels {

    /**
     * Registers models and item variants for all of the provided textures.
     */
    public static void registerAll(Item...items) {
        for (Item i : items) {
            registerAll(i, 1, new ModelIdentifier(i.getRegistryName().toString()));
        }
    }

    private static void registerAll(@Nonnull Item item, int maxMeta, ModelIdentifier resource) {
        for (int i = 0; i < maxMeta; i++) {
            ModelLoader.setCustomModelIdentifier(item, i, resource);
        }
    }

    /**
     * Registers a model for the given item and all associated variants.
     */
    public static void registerAllVariants(@Nonnull Item item, String... variants) {
        registerAllVariants(item, item.getRegistryName().getNamespace(), variants);
    }

    public static void registerAllVariants(@Nonnull Item item, String domain, String... variants) {
        for (int i = 0; i < variants.length; i++) {
            ModelLoader.setCustomModelIdentifier(item, i, new ModelIdentifier(domain + ":" + variants[i]));
        }
    }
}

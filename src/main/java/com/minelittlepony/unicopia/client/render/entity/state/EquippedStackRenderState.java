package com.minelittlepony.unicopia.client.render.entity.state;

import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;

public class EquippedStackRenderState {
    public boolean empty;
    @SuppressWarnings("deprecation")
    public RegistryKey<Item> key = Items.AIR.getRegistryEntry().registryKey();

    public void update(TrinketsDelegate.EquippedStack stack) {
        empty = stack.isEmpty();
        key = stack.stack().getRegistryEntry().getKey().orElseThrow();
    }
}

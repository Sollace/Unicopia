package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.inventory.gui.ContainerOfHolding;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.util.Identifier;

public class UContainers {

    public static final Identifier BAG_OF_HOLDING = new Identifier(Unicopia.MODID, "bag_of_holding");

    static void bootstrap() {
        ContainerProviderRegistry.INSTANCE.registerFactory(BAG_OF_HOLDING, ContainerOfHolding::new);
    }
}

package com.minelittlepony.unicopia.redux;

import com.minelittlepony.unicopia.core.UnicopiaCore;
import com.minelittlepony.unicopia.redux.container.BagOfHoldingContainer;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.util.Identifier;

public class UContainers {

    public static final Identifier BAG_OF_HOLDING = new Identifier(UnicopiaCore.MODID, "bag_of_holding");

    public static void bootstrap() {
        ContainerProviderRegistry.INSTANCE.registerFactory(BAG_OF_HOLDING, BagOfHoldingContainer::new);
    }
}

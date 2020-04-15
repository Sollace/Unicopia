package com.minelittlepony.unicopia.redux;

import com.minelittlepony.unicopia.core.UnicopiaCore;
import com.minelittlepony.unicopia.redux.container.BagOfHoldingContainer;
import com.minelittlepony.unicopia.redux.container.SpellBookContainer;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.util.Identifier;

public interface UContainers {

    Identifier BAG_OF_HOLDING = new Identifier(UnicopiaCore.MODID, "bag_of_holding");
    Identifier SPELL_BOOK = new Identifier(UnicopiaCore.MODID, "spell_book");

    static void bootstrap() {
        ContainerProviderRegistry.INSTANCE.registerFactory(BAG_OF_HOLDING, BagOfHoldingContainer::new);
        ContainerProviderRegistry.INSTANCE.registerFactory(SPELL_BOOK, SpellBookContainer::new);
    }
}

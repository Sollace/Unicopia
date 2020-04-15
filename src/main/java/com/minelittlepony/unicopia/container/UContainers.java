package com.minelittlepony.unicopia.container;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.util.Identifier;

public interface UContainers {

    Identifier BAG_OF_HOLDING = new Identifier(Unicopia.MODID, "bag_of_holding");
    Identifier SPELL_BOOK = new Identifier(Unicopia.MODID, "spell_book");

    static void bootstrap() {
        ContainerProviderRegistry.INSTANCE.registerFactory(BAG_OF_HOLDING, BagOfHoldingContainer::new);
        ContainerProviderRegistry.INSTANCE.registerFactory(SPELL_BOOK, SpellBookContainer::new);
    }
}

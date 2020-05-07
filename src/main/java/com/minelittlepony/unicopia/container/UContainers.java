package com.minelittlepony.unicopia.container;

import net.fabricmc.fabric.api.container.ContainerFactory;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.container.Container;
import net.minecraft.util.Identifier;

public interface UContainers {

    Identifier BAG_OF_HOLDING = register("bag_of_holding", BagOfHoldingContainer::new);
    Identifier SPELL_BOOK = register("spell_book", SpellBookContainer::new);

    static Identifier register(String name, ContainerFactory<Container> factory) {
        Identifier id = new Identifier("unicopia", name);
        ContainerProviderRegistry.INSTANCE.registerFactory(id, factory);
        return id;
    }

    static void bootstrap() { }
}

package com.minelittlepony.unicopia.item;

import java.util.stream.Stream;

import com.minelittlepony.unicopia.item.group.ItemGroupRegistry;
import com.minelittlepony.unicopia.item.toxin.Toxic;
import com.minelittlepony.unicopia.item.toxin.ToxicHolder;

import net.minecraft.item.*;
import net.minecraft.registry.Registries;

public interface UItemGroups {
    ItemGroup ALL_ITEMS = ItemGroupRegistry.createDynamic("items", UItems.EMPTY_JAR::getDefaultStack, () -> {
        return Stream.concat(Stream.of(Items.APPLE), UItems.ITEMS.stream().filter(item -> !(item instanceof ChameleonItem) || ((ChameleonItem)item).isFullyDisguised()));
    });
    ItemGroup HORSE_FEED = ItemGroupRegistry.createDynamic("horsefeed", UItems.ZAP_APPLE::getDefaultStack, () -> {
        return Registries.ITEM.stream().filter(item -> ((ToxicHolder)item).getToxic(item.getDefaultStack()) != Toxic.EMPTY);
    });

    ItemGroup EARTH_PONY_ITEMS = ItemGroupRegistry.createGroupFromTag("earth_pony", UItems.APPLE_PIE::getDefaultStack);
    ItemGroup UNICORN_ITEMS = ItemGroupRegistry.createGroupFromTag("unicorn", UItems.SPELLBOOK::getDefaultStack);
    ItemGroup PEGASUS_ITEMS = ItemGroupRegistry.createGroupFromTag("pegasus", UItems.PEGASUS_FEATHER::getDefaultStack);
    ItemGroup BAT_PONY_ITEMS = ItemGroupRegistry.createGroupFromTag("bat_pony", UItems.SUNGLASSES::getDefaultStack);
    ItemGroup CHANGELING_ITEMS = ItemGroupRegistry.createGroupFromTag("changeling", UItems.LOVE_BOTTLE::getDefaultStack);

    static void bootstrap() {
        UItems.bootstrap();
    }
}

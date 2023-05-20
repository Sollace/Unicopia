package com.minelittlepony.unicopia.item.group;

import java.util.stream.Stream;

import com.minelittlepony.unicopia.item.ChameleonItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.toxin.Toxic;
import com.minelittlepony.unicopia.item.toxin.ToxicHolder;

import net.minecraft.item.*;
import net.minecraft.registry.Registries;

public interface UItemGroups {
    ItemGroup ALL_ITEMS = ItemGroupRegistry.createDynamic("items", UItems.EMPTY_JAR::getDefaultStack, () -> {
        return Stream.concat(Stream.of(Items.APPLE), UItems.ITEMS.stream()
                .filter(item -> !(item instanceof ChameleonItem) || ((ChameleonItem)item).isFullyDisguised()));
    });
    ItemGroup HORSE_FEED = ItemGroupRegistry.createDynamic("horsefeed", UItems.ZAP_APPLE::getDefaultStack, () -> {
        return Registries.ITEM.stream()
                .filter(item -> ((ToxicHolder)item).getToxic(item.getDefaultStack()) != Toxic.EMPTY);
    });

    ItemGroup EARTH_PONY_ITEMS = ItemGroupRegistry.createGroupFromTag("earth_pony", UItems.EARTH_BADGE::getDefaultStack);
    ItemGroup UNICORN_ITEMS = ItemGroupRegistry.createGroupFromTag("unicorn", UItems.UNICORN_BADGE::getDefaultStack);
    ItemGroup PEGASUS_ITEMS = ItemGroupRegistry.createGroupFromTag("pegasus", UItems.PEGASUS_BADGE::getDefaultStack);
    ItemGroup BAT_PONY_ITEMS = ItemGroupRegistry.createGroupFromTag("bat_pony", UItems.BAT_BADGE::getDefaultStack);
    ItemGroup CHANGELING_ITEMS = ItemGroupRegistry.createGroupFromTag("changeling", UItems.CHANGELING_BADGE::getDefaultStack);

    static void bootstrap() {
        ItemGroupRegistry.bootstrap();
    }
}

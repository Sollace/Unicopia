package com.minelittlepony.unicopia.item.group;

import java.util.stream.Stream;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.ChameleonItem;
import com.minelittlepony.unicopia.item.UItems;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;

public interface UItemGroups {
    RegistryKey<ItemGroup> ALL_ITEMS = ItemGroupRegistry.createDynamic("items", UItems.CRYSTAL_HEART::getDefaultStack, () -> {
        return Stream.concat(Stream.of(Items.APPLE), ItemGroupRegistry.ITEMS.stream()
                .filter(item -> !(item instanceof ChameleonItem) || ((ChameleonItem)item).isFullyDisguised()));
    });
    RegistryKey<ItemGroup> FORAGING_ITEMS = ItemGroupRegistry.createGroupFromTag("foraging", UTags.Items.GROUP_FORAGING, Items.HAY_BLOCK::getDefaultStack);
    RegistryKey<ItemGroup> EARTH_PONY_ITEMS = ItemGroupRegistry.createGroupFromTag("earth_pony", UTags.Items.GROUP_EARTH_PONY, UItems.EARTH_BADGE::getDefaultStack);
    RegistryKey<ItemGroup> UNICORN_ITEMS = ItemGroupRegistry.createGroupFromTag("unicorn", UTags.Items.GROUP_UNICORN, UItems.UNICORN_BADGE::getDefaultStack);
    RegistryKey<ItemGroup> PEGASUS_ITEMS = ItemGroupRegistry.createGroupFromTag("pegasus", UTags.Items.GROUP_PEGASUS, UItems.PEGASUS_BADGE::getDefaultStack);
    RegistryKey<ItemGroup> BAT_PONY_ITEMS = ItemGroupRegistry.createGroupFromTag("bat_pony", UTags.Items.GROUP_BAT_PONY, UItems.BAT_BADGE::getDefaultStack);
    RegistryKey<ItemGroup> SEA_PON_ITEMS = ItemGroupRegistry.createGroupFromTag("sea_pony", UTags.Items.GROUP_SEA_PONY, UItems.PEARL_NECKLACE::getDefaultStack);
    RegistryKey<ItemGroup> CHANGELING_ITEMS = ItemGroupRegistry.createGroupFromTag("changeling", UTags.Items.GROUP_CHANGELING, UItems.CHANGELING_BADGE::getDefaultStack);

    static void bootstrap() {
        ItemGroupRegistry.bootstrap();
    }
}

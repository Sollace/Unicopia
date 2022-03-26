package com.minelittlepony.unicopia.item;

import java.util.stream.Collectors;

import com.minelittlepony.unicopia.item.toxin.ToxicHolder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

public interface UItemGroups {
    ItemGroup ALL_ITEMS = FabricItemGroupBuilder.create(new Identifier("unicopia", "items")).appendItems(list -> {
        list.add(Items.APPLE.getDefaultStack());

        DefaultedList<ItemStack> defs = DefaultedList.of();
        UItems.ITEMS.stream()
                .filter(item -> !(item instanceof ChameleonItem) || ((ChameleonItem)item).isFullyDisguised())
                .forEach(item -> {
                    item.appendStacks(ItemGroup.SEARCH, defs);
                });
        list.addAll(defs);
    }).icon(UItems.EMPTY_JAR::getDefaultStack).build();
    ItemGroup HORSE_FEED = FabricItemGroupBuilder.create(new Identifier("unicopia", "horsefeed")).appendItems(list -> {
        list.addAll(Registry.ITEM.stream()
                .map(Item::getDefaultStack)
                .filter(item -> item.getItem() instanceof ToxicHolder && ((ToxicHolder)item.getItem()).getToxic(item).isPresent())
                .collect(Collectors.toList()));
    }).icon(UItems.ZAP_APPLE::getDefaultStack).build();

    static void bootstrap() {}
}

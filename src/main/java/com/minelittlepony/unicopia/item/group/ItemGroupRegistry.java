package com.minelittlepony.unicopia.item.group;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.tag.TagKey;

public interface ItemGroupRegistry {

    static List<ItemStack> getVariations(Item item) {
        if (item instanceof MultiItem) {
            return ((MultiItem)item).getDefaultStacks();
        }
        return List.of(item.getDefaultStack());
    }

    static ItemGroup createDynamic(String name, Supplier<ItemStack> icon, Supplier<Stream<Item>> items) {
        boolean[] reloading = new boolean[1];
        return FabricItemGroupBuilder.create(Unicopia.id(name)).appendItems(list -> {
            if (reloading[0]) {
                return;
            }
            reloading[0] = true;
            items.get().forEach(item -> {
                list.addAll(ItemGroupRegistry.getVariations(item));
            });
            reloading[0] = false;
        }).icon(icon).build();
    }

    static ItemGroup createGroupFromTag(String name, Supplier<ItemStack> icon) {
        TagKey<Item> key = UTags.item("groups/" + name);
        return createDynamic(name, icon, () -> {
            return Registry.ITEM.getEntryList(key)
                    .stream()
                    .flatMap(named -> named.stream())
                    .map(entry -> entry.value());
        });
    }

    static void bootstrap() {

    }
}

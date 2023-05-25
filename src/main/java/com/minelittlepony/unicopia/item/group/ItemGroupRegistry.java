package com.minelittlepony.unicopia.item.group;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

public interface ItemGroupRegistry {
    Map<ItemGroup, Set<Item>> REGISTRY = new HashMap<>();

    static List<ItemStack> getVariations(Item item) {
        if (item instanceof MultiItem) {
            return ((MultiItem)item).getDefaultStacks();
        }
        return List.of(item.getDefaultStack());
    }

    static <T extends Item> T register(T item, ItemGroup group) {
        REGISTRY.computeIfAbsent(group, g -> new LinkedHashSet<>()).add(item);
        return item;
    }

    static ItemGroup createDynamic(String name, Supplier<ItemStack> icon, Supplier<Stream<Item>> items) {
        return FabricItemGroup.builder(Unicopia.id(name)).entries((features, list, k) -> {
            items.get().forEach(item -> {
                list.addAll(ItemGroupRegistry.getVariations(item));
            });
        }).icon(icon).build();
    }

    static ItemGroup createGroupFromTag(String name, Supplier<ItemStack> icon) {
        TagKey<Item> key = UTags.item("groups/" + name);
        return createDynamic(name, icon, () -> {
            return Registries.ITEM.getEntryList(key)
                    .stream()
                    .flatMap(named -> named.stream())
                    .map(entry -> entry.value());
        });
    }

    static void bootstrap() {
        REGISTRY.forEach((group, items) -> {
            ItemGroupEvents.modifyEntriesEvent(group).register(event -> {
                event.addAll(items.stream().map(Item::getDefaultStack).toList());
            });
        });
    }
}

package com.minelittlepony.unicopia.item.group;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.FancyBedBlock.SheetPattern;
import com.minelittlepony.unicopia.item.BedsheetsItem;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;

public interface ItemGroupRegistry {
    List<Item> ITEMS = new ArrayList<>();
    Map<RegistryKey<ItemGroup>, Set<Item>> REGISTRY = new HashMap<>();

    static List<ItemStack> getVariations(Item item) {
        if (item instanceof MultiItem m) {
            return m.getDefaultStacks();
        }
        return List.of(item.getDefaultStack());
    }

    static <T extends Item> T register(T item, RegistryKey<ItemGroup> group) {
        REGISTRY.computeIfAbsent(group, g -> new LinkedHashSet<>()).add(item);
        return item;
    }

    static Item register(String name, RegistryKey<JukeboxSong> song) {
        return register(name, s -> new Item(s
                .jukeboxPlayable(song)
                .maxCount(1)
                .rarity(Rarity.RARE)
            ), ItemGroups.TOOLS);
    }

    static Item register(Race race) {
        return register(race.getId().withPath(p -> p + "_badge"), Item::new);
    }

    static Item register(SheetPattern pattern) {
        return register(pattern.asString() + "_bed_sheets", s -> new BedsheetsItem(pattern, s.maxCount(8)), ItemGroups.FUNCTIONAL);
    }

    static <T extends Item> T register(String name, Function<Item.Settings, T> item, RegistryKey<ItemGroup> group) {
        return register(Unicopia.id(name), item, group);
    }

    static <T extends Item> T register(String name, Function<Item.Settings, T> item) {
        return register(Unicopia.id(name), item);
    }

    static <T extends Item> T register(Identifier id, Function<Item.Settings, T> item, RegistryKey<ItemGroup> group) {
        return register(register(id, item), group);
    }

    static <T extends Item> T register(Identifier id, Function<Item.Settings, T> itemFunc) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        T item = itemFunc.apply(new Item.Settings().registryKey(key));
        if (item instanceof BlockItem bi && bi.getBlock() == null) {
            throw new NullPointerException("Registered block item did not have a block " + id);
        }
        ITEMS.add(item);
        return Registry.register(Registries.ITEM, id, item);
    }

    static RegistryKey<ItemGroup> createDynamic(String name, Supplier<ItemStack> icon, Supplier<Stream<Item>> items) {
        RegistryKey<ItemGroup> key = RegistryKey.of(RegistryKeys.ITEM_GROUP, Unicopia.id(name));
        Registry.register(Registries.ITEM_GROUP, key.getValue(), FabricItemGroup.builder().entries((context, entries) -> {
            items.get().forEach(item -> {
                entries.addAll(ItemGroupRegistry.getVariations(item));
            });
        }).icon(icon).displayName(Text.translatable(Util.createTranslationKey("itemGroup", key.getValue()))).build());
        return key;
    }

    static RegistryKey<ItemGroup> createGroupFromTag(String name, TagKey<Item> tag, Supplier<ItemStack> icon) {
        return createDynamic(name, icon, () -> {
            return Registries.ITEM.getOptional(tag)
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

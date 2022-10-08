package com.minelittlepony.unicopia.item;

import java.util.function.Supplier;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.item.toxin.Toxic;
import com.minelittlepony.unicopia.item.toxin.ToxicHolder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.TagKey;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

public interface UItemGroups {
    ItemGroup ALL_ITEMS = create("items", UItems.EMPTY_JAR::getDefaultStack, () -> {
        return Stream.concat(Stream.of(Items.APPLE), UItems.ITEMS.stream().filter(item -> !(item instanceof ChameleonItem) || ((ChameleonItem)item).isFullyDisguised()));
    });
    ItemGroup HORSE_FEED = create("horsefeed", UItems.ZAP_APPLE::getDefaultStack, () -> {
        return Registry.ITEM.stream().filter(item -> ((ToxicHolder)item).getToxic(item.getDefaultStack()) != Toxic.EMPTY);
    });

    ItemGroup EARTH_PONY_ITEMS = forTag("earth_pony", UItems.APPLE_PIE::getDefaultStack);
    ItemGroup UNICORN_ITEMS = forTag("unicorn", UItems.SPELLBOOK::getDefaultStack);
    ItemGroup PEGASUS_ITEMS = forTag("pegasus", UItems.PEGASUS_FEATHER::getDefaultStack);
    ItemGroup BAT_PONY_ITEMS = forTag("bat_pony", UItems.SUNGLASSES::getDefaultStack);
    ItemGroup CHANGELING_ITEMS = forTag("changeling", UItems.LOVE_BOTTLE::getDefaultStack);

    static ItemGroup forTag(String name, Supplier<ItemStack> icon) {
        TagKey<Item> key = UTags.item("groups/" + name);
        return create(name, icon, () -> {
            return Registry.ITEM.getEntryList(key)
                    .stream()
                    .flatMap(named -> named.stream())
                    .map(entry -> entry.value());
        });
    }

    static ItemGroup create(String name, Supplier<ItemStack> icon, Supplier<Stream<Item>> items) {
        return FabricItemGroupBuilder.create(Unicopia.id(name)).appendItems(list -> {
            DefaultedList<ItemStack> defs = DefaultedList.of();
            items.get().forEach(item -> item.appendStacks(ItemGroup.SEARCH, defs));
            list.addAll(defs);
        }).icon(icon).build();
    }

    static void bootstrap() {}
}

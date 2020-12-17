package com.minelittlepony.unicopia.item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.item.toxin.ToxicHolder;
import com.minelittlepony.unicopia.item.toxin.Toxics;
import com.minelittlepony.unicopia.item.toxin.UFoodComponents;

import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemGroup;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

public interface UItems {

    List<Item> ITEMS = new ArrayList<>();

    AppleItem GREEN_APPLE = register("green_apple", new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)));
    AppleItem SWEET_APPLE = register("sweet_apple", new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)));
    AppleItem SOUR_APPLE = register("sour_apple", new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)));

    ZapAppleItem ZAP_APPLE = register("zap_apple", new ZapAppleItem(new Item.Settings().group(ItemGroup.FOOD).food(UFoodComponents.ZAP_APPLE)));

    AppleItem ROTTEN_APPLE = register("rotten_apple", new RottenAppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)));
    AppleItem COOKED_ZAP_APPLE = register("cooked_zap_apple", new AppleItem(new Item.Settings().group(ItemGroup.FOOD).food(FoodComponents.APPLE)));

    Item MUSIC_DISC_CRUSADE = register("music_disc_crusade", USounds.RECORD_CRUSADE);
    Item MUSIC_DISC_PET = register("music_disc_pet", USounds.RECORD_PET);
    Item MUSIC_DISC_POPULAR = register("music_disc_popular", USounds.RECORD_POPULAR);
    Item MUSIC_DISC_FUNK = register("music_disc_funk", USounds.RECORD_FUNK);

    static <T extends Item> T register(String name, T item) {
        ITEMS.add(item);
        if (item instanceof BlockItem) {
            ((BlockItem)item).appendBlocks(Item.BLOCK_ITEMS, item);
        }
        return Registry.register(Registry.ITEM, new Identifier("unicopia", name), item);
    }

    static MusicDiscItem register(String name, SoundEvent sound) {
        return register(name, new MusicDiscItem(1, sound, new Settings()
                .maxCount(1)
                .group(ItemGroup.MISC)
                .rarity(Rarity.RARE)
            ) {});
    }

    static void bootstrap() {
        Toxics.bootstrap();
        URecipes.bootstrap();

        FabricItemGroupBuilder.create(new Identifier("unicopia", "items")).appendItems(list -> {
            list.addAll(VanillaOverrides.REGISTRY.stream().map(Item::getDefaultStack).collect(Collectors.toList()));
            list.addAll(ITEMS.stream().map(Item::getDefaultStack).collect(Collectors.toList()));
        }).icon(ZAP_APPLE::getDefaultStack).build();

        FabricItemGroupBuilder.create(new Identifier("unicopia", "horsefeed")).appendItems(list -> {
            list.addAll(Registry.ITEM.stream()
                    .filter(item -> item instanceof ToxicHolder && ((ToxicHolder)item).getToxic().isPresent())
                    .map(Item::getDefaultStack)
                    .collect(Collectors.toList()));
        }).icon(ZAP_APPLE::getDefaultStack).build();
    }
}

package com.minelittlepony.unicopia.item;

import java.util.ArrayList;
import java.util.List;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.item.toxin.UFoodComponents;

import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
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

    FriendshipBraceletItem FRIENDSHIP_BRACELET = register("friendship_bracelet", new FriendshipBraceletItem(
            new FabricItemSettings()
            .rarity(Rarity.UNCOMMON)
            .group(ItemGroup.TOOLS)
    ));

    Item EMPTY_JAR = register("empty_jar", new JarItem(new Item.Settings().group(ItemGroup.DECORATIONS).maxCount(16).fireproof(), false, false, false));
    FilledJarItem FILLED_JAR = register("filled_jar", new FilledJarItem(new Item.Settings().maxCount(1)));
    Item RAIN_CLOUD_JAR  = register("rain_cloud_jar", new JarItem(new Item.Settings().group(ItemGroup.DECORATIONS).maxCount(1).fireproof(), true, false, false));
    Item STORM_CLOUD_JAR  = register("storm_cloud_jar", new JarItem(new Item.Settings().group(ItemGroup.DECORATIONS).maxCount(1).fireproof(), true, true, false));
    Item LIGHTNING_JAR  = register("lightning_jar", new JarItem(new Item.Settings().group(ItemGroup.DECORATIONS).maxCount(1).fireproof(), false, false, true));
    Item ZAP_APPLE_JAM_JAR = register("zap_apple_jam_jar", new JarItem(new Item.Settings().group(ItemGroup.DECORATIONS).maxCount(1).fireproof(), false, false, true));

    Item CRYSTAL_HEART = register("crystal_heart", new CrystalHeartItem(new Item.Settings().group(ItemGroup.DECORATIONS).maxCount(1)));
    Item CRYSTAL_SHARD = register("crystal_shard", new Item(new Item.Settings().group(ItemGroup.MATERIALS)));

    Item GEMSTONE = register("gemstone", new GemstoneItem(new Item.Settings().group(ItemGroup.MATERIALS)));

    Item PEGASUS_FEATHER = register("pegasus_feather", new Item(new Item.Settings().group(ItemGroup.MATERIALS)));
    Item GRYPHON_FEATHER = register("gryphon_feather", new Item(new Item.Settings().group(ItemGroup.MATERIALS)));

    Item DAFFODIL_DAISY_SANDWICH = register("daffodil_daisy_sandwich", new Item(new Item.Settings().group(ItemGroup.FOOD).food(UFoodComponents.DAFODIL_DAISY_SANDWICH)));
    Item HAY_BURGER = register("hay_burger", new Item(new Item.Settings().group(ItemGroup.FOOD).maxCount(1).food(UFoodComponents.HAY_BURGER)));
    Item HAY_FRIES = register("hay_fries", new Item(new Item.Settings().group(ItemGroup.FOOD).maxCount(16).food(UFoodComponents.HAY_FRIES)));
    Item WHEAT_WORMS = register("wheat_worms", new Item(new Item.Settings().group(ItemGroup.MISC).maxCount(16).food(UFoodComponents.WORMS)));

    Item MUG = register("mug", new Item(new Settings().group(ItemGroup.MATERIALS).maxCount(16)));
    Item CIDER = register("cider", new DrinkableItem(new Item.Settings().group(ItemGroup.FOOD).food(UFoodComponents.CIDER).maxCount(1).recipeRemainder(MUG)));
    Item JUICE = register("juice", new DrinkableItem(new Item.Settings().group(ItemGroup.FOOD).recipeRemainder(Items.GLASS_BOTTLE).maxCount(1).food(UFoodComponents.JUICE)));
    Item BURNED_JUICE = register("burned_juice", new Item(new Item.Settings().group(ItemGroup.FOOD).recipeRemainder(Items.GLASS_BOTTLE).maxCount(16).food(UFoodComponents.BURNED_JUICE)));

    Item GOLDEN_FEATHER = register("golden_feather", new Item(new Item.Settings().rarity(Rarity.UNCOMMON).group(ItemGroup.MATERIALS)));
    Item GOLDEN_WING = register("golden_wing", new Item(new Item.Settings().rarity(Rarity.UNCOMMON).group(ItemGroup.MATERIALS)));

    AmuletItem PEGASUS_AMULET = register("pegasus_amulet", new AmuletItem(new FabricItemSettings()
            .maxDamage(890)
            .rarity(Rarity.UNCOMMON)
            .group(ItemGroup.DECORATIONS), 900));
    /*AmuletItem ALICORN_AMULET = register("alicorn_amulet", new AmuletItem(new AmuletItem.Settings("alicorn_amulet")
            .toughness(900000000)
            .resistance(90000000)
            .group(ItemGroup.DECORATIONS), 0));*/

    static <T extends Item> T register(String name, T item) {
        ITEMS.add(item);
        if (item instanceof BlockItem) {
            ((BlockItem)item).appendBlocks(Item.BLOCK_ITEMS, item);
        }
        Items.MILK_BUCKET.appendStacks(null, null);
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
        UEnchantments.bootstrap();
        URecipes.bootstrap();
        UItemGroups.bootstrap();
    }
}

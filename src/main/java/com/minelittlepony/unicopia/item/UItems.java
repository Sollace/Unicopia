package com.minelittlepony.unicopia.item;

import com.google.common.collect.ImmutableMultimap;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.block.UWoodTypes;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.cloud.CloudBedItem;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.item.group.ItemGroupRegistry;
import com.minelittlepony.unicopia.item.group.UItemGroups;
import com.minelittlepony.unicopia.item.toxin.UFoodComponents;
import com.terraformersmc.terraform.boat.api.TerraformBoatType;
import com.terraformersmc.terraform.boat.api.TerraformBoatTypeRegistry;
import com.terraformersmc.terraform.boat.api.item.TerraformBoatItemHelper;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.*;
import net.minecraft.item.Item.Settings;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Rarity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.Registries;

public interface UItems {
    Item GREEN_APPLE = register("green_apple", AppleItem.registerTickCallback(new Item(new Item.Settings().food(FoodComponents.APPLE))), ItemGroups.FOOD_AND_DRINK);
    Item SWEET_APPLE = register("sweet_apple", AppleItem.registerTickCallback(new Item(new Item.Settings().food(FoodComponents.APPLE))), ItemGroups.FOOD_AND_DRINK);
    Item SOUR_APPLE = register("sour_apple", AppleItem.registerTickCallback(new Item(new Item.Settings().food(FoodComponents.APPLE))), ItemGroups.FOOD_AND_DRINK);

    ZapAppleItem ZAP_APPLE = register("zap_apple", AppleItem.registerTickCallback(new ZapAppleItem(new Item.Settings().food(UFoodComponents.ZAP_APPLE))), ItemGroups.FOOD_AND_DRINK);
    Item ZAP_BULB = register("zap_bulb", new Item(new Item.Settings().food(UFoodComponents.ZAP_BULB)), ItemGroups.FOOD_AND_DRINK);

    Item ROTTEN_APPLE = register("rotten_apple", new RottenAppleItem(new Item.Settings().food(FoodComponents.APPLE)), ItemGroups.FOOD_AND_DRINK);
    Item COOKED_ZAP_APPLE = register("cooked_zap_apple", new Item(new Item.Settings().food(FoodComponents.APPLE)), ItemGroups.FOOD_AND_DRINK);

    Item MUSIC_DISC_CRUSADE = register("music_disc_crusade", USounds.RECORD_CRUSADE, 181);
    Item MUSIC_DISC_PET = register("music_disc_pet", USounds.RECORD_PET, 221);
    Item MUSIC_DISC_POPULAR = register("music_disc_popular", USounds.RECORD_POPULAR, 112);
    Item MUSIC_DISC_FUNK = register("music_disc_funk", USounds.RECORD_FUNK, 91);

    FriendshipBraceletItem FRIENDSHIP_BRACELET = register("friendship_bracelet", new FriendshipBraceletItem(new FabricItemSettings().rarity(Rarity.UNCOMMON)), ItemGroups.TOOLS);

    Item EMPTY_JAR = register("empty_jar", new JarItem(new Item.Settings().maxCount(16).fireproof(), false, false, false), ItemGroups.FUNCTIONAL);
    FilledJarItem FILLED_JAR = register("filled_jar", new FilledJarItem(new Item.Settings().maxCount(1).recipeRemainder(EMPTY_JAR)));
    Item RAIN_CLOUD_JAR  = register("rain_cloud_jar", new JarItem(new Item.Settings().maxCount(1).fireproof().recipeRemainder(EMPTY_JAR), true, false, false), ItemGroups.FUNCTIONAL);
    Item STORM_CLOUD_JAR  = register("storm_cloud_jar", new JarItem(new Item.Settings().maxCount(1).fireproof().recipeRemainder(EMPTY_JAR), true, true, false), ItemGroups.FUNCTIONAL);
    Item LIGHTNING_JAR  = register("lightning_jar", new JarItem(new Item.Settings().maxCount(1).fireproof().recipeRemainder(EMPTY_JAR), false, false, true), ItemGroups.FUNCTIONAL);
    Item ZAP_APPLE_JAM_JAR = register("zap_apple_jam_jar", new JarItem(new Item.Settings().maxCount(1).fireproof().recipeRemainder(EMPTY_JAR), false, false, true), ItemGroups.FUNCTIONAL);

    Item TOAST = register("toast", new Item(new Item.Settings().maxCount(16).food(UFoodComponents.TOAST)), ItemGroups.FOOD_AND_DRINK);
    Item BURNED_TOAST = register("burned_toast", new Item(new Item.Settings().maxCount(16).food(UFoodComponents.BURNED_TOAST)), ItemGroups.FOOD_AND_DRINK);
    Item JAM_TOAST = register("jam_toast", new Item(new Item.Settings().maxCount(16).food(UFoodComponents.JAM_TOAST)), ItemGroups.FOOD_AND_DRINK);

    Item CRYSTAL_HEART = register("crystal_heart", new CrystalHeartItem(new Item.Settings().maxCount(1)), ItemGroups.TOOLS);
    Item CRYSTAL_SHARD = register("crystal_shard", new Item(new Item.Settings()), ItemGroups.NATURAL);

    GemstoneItem GEMSTONE = register("gemstone", new GemstoneItem(new Item.Settings()), ItemGroups.NATURAL);
    Item BOTCHED_GEM = register("botched_gem", new Item(new Item.Settings()), ItemGroups.NATURAL);

    Item PEGASUS_FEATHER = register("pegasus_feather", new Item(new Item.Settings()), ItemGroups.NATURAL);
    Item GRYPHON_FEATHER = register("gryphon_feather", new Item(new Item.Settings()), ItemGroups.NATURAL);

    Item OAT_SEEDS = register("oat_seeds", new AliasedBlockItem(UBlocks.OATS, new Item.Settings()), ItemGroups.NATURAL);
    Item OATS = register("oats", new Item(new Item.Settings().food(UFoodComponents.OATS)), ItemGroups.FOOD_AND_DRINK);
    Item IMPORTED_OATS = register("imported_oats", new Item(new Item.Settings().food(UFoodComponents.IMPORTED_OATS)), ItemGroups.FOOD_AND_DRINK);
    Item OATMEAL = register("oatmeal", new OatmealItem(new Item.Settings().recipeRemainder(Items.BOWL).maxCount(1).food(UFoodComponents.OATMEAL)), ItemGroups.FOOD_AND_DRINK);

    Item DAFFODIL_DAISY_SANDWICH = register("daffodil_daisy_sandwich", new Item(new Item.Settings().food(UFoodComponents.DAFODIL_DAISY_SANDWICH)), ItemGroups.FOOD_AND_DRINK);
    Item HAY_BURGER = register("hay_burger", new Item(new Item.Settings().maxCount(1).food(UFoodComponents.HAY_BURGER)), ItemGroups.FOOD_AND_DRINK);
    Item HAY_FRIES = register("hay_fries", new Item(new Item.Settings().maxCount(16).food(UFoodComponents.HAY_FRIES)), ItemGroups.FOOD_AND_DRINK);
    Item CRISPY_HAY_FRIES = register("crispy_hay_fries", new Item(new Item.Settings().maxCount(16).food(UFoodComponents.CRISPY_HAY_FRIES)), ItemGroups.FOOD_AND_DRINK);
    /**
     * https://mlp.fandom.com/wiki/Food_and_beverage
     */
    Item HORSE_SHOE_FRIES = register("horse_shoe_fries", new Item(new Item.Settings().maxCount(32).food(UFoodComponents.HAY_FRIES)), ItemGroups.FOOD_AND_DRINK);

    Item WHEAT_WORMS = register("wheat_worms", new Item(new Item.Settings().maxCount(16).food(UFoodComponents.WORMS)), ItemGroups.NATURAL);
    Item MUFFIN = register("muffin", new MuffinItem(new Item.Settings().maxCount(32).food(FoodComponents.BREAD), 0), ItemGroups.FOOD_AND_DRINK);
    Item PINECONE = register("pinecone", new Item(new Item.Settings().food(UFoodComponents.PINECONE).maxCount(3)), ItemGroups.FOOD_AND_DRINK);
    Item ACORN = register("acorn", new Item(new Item.Settings().food(UFoodComponents.ACORN).maxCount(16)), ItemGroups.FOOD_AND_DRINK);
    Item MANGO = register("mango", new Item(new Item.Settings().food(UFoodComponents.MANGO)), ItemGroups.FOOD_AND_DRINK);
    Item BANANA = register("banana", new Item(new Item.Settings().food(UFoodComponents.BANANA)), ItemGroups.FOOD_AND_DRINK);
    Item PINEAPPLE = register("pineapple", new PineappleItem(new Item.Settings().food(UFoodComponents.BANANA).maxDamage(3)), ItemGroups.FOOD_AND_DRINK);
    Item PINEAPPLE_CROWN = register("pineapple_crown", new AliasedBlockItem(UBlocks.PINEAPPLE, new Item.Settings()), ItemGroups.NATURAL);

    Item PEBBLES = register("pebbles", new AliasedBlockItem(UBlocks.ROCKS, new Item.Settings()), ItemGroups.NATURAL);
    Item ROCK = register("rock", new HeavyProjectileItem(new Item.Settings(), 3), ItemGroups.NATURAL);
    Item WEIRD_ROCK = register("weird_rock", new BluntWeaponItem(new Item.Settings(), ImmutableMultimap.of(
            EntityAttributes.GENERIC_LUCK, new EntityAttributeModifier(BluntWeaponItem.LUCK_MODIFIER_ID, "Weapon modifier", 9, EntityAttributeModifier.Operation.ADDITION)
    )), ItemGroups.NATURAL);
    Item TOM = register("tom", new BluntWeaponItem(new Item.Settings(), ImmutableMultimap.of(
            EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, new EntityAttributeModifier(BluntWeaponItem.KNOCKBACK_MODIFIER_ID, "Weapon modifier", 0.9, EntityAttributeModifier.Operation.ADDITION)
    )), ItemGroups.NATURAL);
    Item ROCK_STEW = register("rock_stew", new Item(new Item.Settings().food(FoodComponents.MUSHROOM_STEW)), ItemGroups.FOOD_AND_DRINK);
    Item ROCK_CANDY = register("rock_candy", new Item(new Item.Settings().food(UFoodComponents.CANDY).maxCount(16)), ItemGroups.FOOD_AND_DRINK);
    Item SALT_CUBE = register("salt_cube", new Item(new Item.Settings().food(UFoodComponents.SALT_CUBE)), ItemGroups.FOOD_AND_DRINK);

    Item GREEN_APPLE_SEEDS = register("green_apple_seeds", new AliasedBlockItem(UBlocks.GREEN_APPLE_SPROUT, new Item.Settings()), ItemGroups.NATURAL);
    Item SWEET_APPLE_SEEDS = register("sweet_apple_seeds", new AliasedBlockItem(UBlocks.SWEET_APPLE_SPROUT, new Item.Settings()), ItemGroups.NATURAL);
    Item SOUR_APPLE_SEEDS = register("sour_apple_seeds", new AliasedBlockItem(UBlocks.SOUR_APPLE_SPROUT, new Item.Settings()), ItemGroups.NATURAL);

    Item MUG = register("mug", new Item(new Settings().maxCount(16)), ItemGroups.TOOLS);
    Item CIDER = register("cider", new DrinkableItem(new Item.Settings().food(UFoodComponents.CIDER).maxCount(1).recipeRemainder(MUG)), ItemGroups.FOOD_AND_DRINK);
    Item JUICE = register("juice", new DrinkableItem(new Item.Settings().recipeRemainder(Items.GLASS_BOTTLE).maxCount(1).food(UFoodComponents.JUICE)), ItemGroups.FOOD_AND_DRINK);
    Item BURNED_JUICE = register("burned_juice", new DrinkableItem(new Item.Settings().recipeRemainder(Items.GLASS_BOTTLE).maxCount(1).food(UFoodComponents.BURNED_JUICE)), ItemGroups.FOOD_AND_DRINK);
    Item APPLE_PIE = register("apple_pie", new BlockItem(UBlocks.APPLE_PIE, new Item.Settings().maxCount(1)), ItemGroups.FOOD_AND_DRINK);
    Item APPLE_PIE_HOOF = register("apple_pie_hoof", new AliasedBlockItem(UBlocks.APPLE_PIE, new Item.Settings().maxCount(1)), ItemGroups.FOOD_AND_DRINK);
    Item APPLE_PIE_SLICE = register("apple_pie_slice", new Item(new Item.Settings().maxCount(16).food(UFoodComponents.PIE)), ItemGroups.FOOD_AND_DRINK);
    Item CANDIED_APPLE = register("candied_apple", new StagedFoodItem(new Item.Settings().food(UFoodComponents.CANDY).maxDamage(3), () -> Items.STICK), ItemGroups.FOOD_AND_DRINK);

    Item LOVE_BOTTLE = register("love_bottle", new DrinkableItem(new Item.Settings().food(UFoodComponents.LOVE_BOTTLE).maxCount(1).recipeRemainder(Items.GLASS_BOTTLE)), ItemGroups.FOOD_AND_DRINK);
    Item LOVE_BUCKET = register("love_bucket", new DrinkableItem(new Item.Settings().food(UFoodComponents.LOVE_BUCKET).recipeRemainder(Items.BUCKET)), ItemGroups.FOOD_AND_DRINK);
    Item LOVE_MUG = register("love_mug", new DrinkableItem(new Item.Settings().food(UFoodComponents.LOVE_MUG).recipeRemainder(MUG)), ItemGroups.FOOD_AND_DRINK);

    Item GOLDEN_FEATHER = register("golden_feather", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)), ItemGroups.NATURAL);
    Item GOLDEN_WING = register("golden_wing", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)), ItemGroups.NATURAL);

    Item DRAGON_BREATH_SCROLL = register("dragon_breath_scroll", new DragonBreathScrollItem(new Item.Settings().rarity(Rarity.UNCOMMON)), ItemGroups.TOOLS);
    Item GROGARS_BELL = register("grogars_bell", new BellItem(new Item.Settings().rarity(Rarity.RARE).maxCount(1)), ItemGroups.TOOLS);
    Item MEADOWBROOKS_STAFF = register("meadowbrooks_staff", new StaffItem(new Settings().rarity(Rarity.UNCOMMON).maxCount(1).maxDamage(120)), ItemGroups.TOOLS);
    Item MAGIC_STAFF = register("magic_staff", new EnchantedStaffItem(new Settings().rarity(Rarity.UNCOMMON).maxCount(1).maxDamage(120)), ItemGroups.TOOLS);

    Item IRON_HORSE_SHOE = register("iron_horse_shoe", new HorseShoeItem(new Item.Settings().maxDamage(200), 4, 0.6F, 1), ItemGroups.COMBAT);
    Item GOLDEN_HORSE_SHOE = register("golden_horse_shoe", new HorseShoeItem(new Item.Settings().maxDamage(100), 5, 0.1F, 0.5F), ItemGroups.COMBAT);
    Item COPPER_HORSE_SHOE = register("copper_horse_shoe", new HorseShoeItem(new Item.Settings().maxDamage(250), 6, 0.5F, 0.8F), ItemGroups.COMBAT);
    Item NETHERITE_HORSE_SHOE = register("netherite_horse_shoe", new HorseShoeItem(new Item.Settings().maxDamage(800), 3, 0.7F, 1.2F), ItemGroups.COMBAT);

    Item WOODEN_POLEARM = register("wooden_polearm", new PolearmItem(ToolMaterials.WOOD, 2, -3.6F, 2, new Item.Settings()), ItemGroups.COMBAT);
    Item STONE_POLEARM = register("stone_polearm", new PolearmItem(ToolMaterials.STONE, 2, -3.6F, 2, new Item.Settings()), ItemGroups.COMBAT);
    Item IRON_POLEARM = register("iron_polearm", new PolearmItem(ToolMaterials.IRON, 2, -3.6F, 3, new Item.Settings()), ItemGroups.COMBAT);
    Item GOLDEN_POLEARM = register("golden_polearm", new PolearmItem(ToolMaterials.GOLD, 2, -3.6F, 4, new Item.Settings()), ItemGroups.COMBAT);
    Item DIAMOND_POLEARM = register("diamond_polearm", new PolearmItem(ToolMaterials.DIAMOND, 2, -3.6F, 5, new Item.Settings()), ItemGroups.COMBAT);
    Item NETHERITE_POLEARM = register("netherite_polearm", new PolearmItem(ToolMaterials.NETHERITE, 2, -3.6F, 5, new Item.Settings().fireproof()), ItemGroups.COMBAT);

    Item BUTTERFLY_SPAWN_EGG = register("butterfly_spawn_egg", new SpawnEggItem(UEntities.BUTTERFLY, 0x222200, 0xaaeeff, new Item.Settings()), ItemGroups.SPAWN_EGGS);
    Item BUTTERFLY = register("butterfly", new Item(new Item.Settings().food(UFoodComponents.INSECTS)), ItemGroups.FOOD_AND_DRINK);

    Item PALM_BOAT = ItemGroupRegistry.register(TerraformBoatItemHelper.registerBoatItem(Unicopia.id("palm_boat"), UWoodTypes.PALM_BOAT_TYPE, false), ItemGroups.FUNCTIONAL);
    Item PALM_CHEST_BOAT = ItemGroupRegistry.register(TerraformBoatItemHelper.registerBoatItem(Unicopia.id("palm_chest_boat"), UWoodTypes.PALM_BOAT_TYPE, true), ItemGroups.FUNCTIONAL);

    Item SPELLBOOK = register("spellbook", new SpellbookItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)), ItemGroups.TOOLS);

    Item OAK_BASKET = register("oak_basket", new BasketItem(AirBalloonEntity.BasketType.of(BoatEntity.Type.OAK), new Item.Settings().maxCount(1)), ItemGroups.TOOLS);
    Item SPRUCE_BASKET = register("spruce_basket", new BasketItem(AirBalloonEntity.BasketType.of(BoatEntity.Type.SPRUCE), new Item.Settings().maxCount(1)), ItemGroups.TOOLS);
    Item BIRCH_BASKET = register("birch_basket", new BasketItem(AirBalloonEntity.BasketType.of(BoatEntity.Type.BIRCH), new Item.Settings().maxCount(1)), ItemGroups.TOOLS);
    Item JUNGLE_BASKET = register("jungle_basket", new BasketItem(AirBalloonEntity.BasketType.of(BoatEntity.Type.JUNGLE), new Item.Settings().maxCount(1)), ItemGroups.TOOLS);
    Item ACACIA_BASKET = register("acacia_basket", new BasketItem(AirBalloonEntity.BasketType.of(BoatEntity.Type.ACACIA), new Item.Settings().maxCount(1)), ItemGroups.TOOLS);
    Item CHERRY_BASKET = register("cherry_basket", new BasketItem(AirBalloonEntity.BasketType.of(BoatEntity.Type.CHERRY), new Item.Settings().maxCount(1)), ItemGroups.TOOLS);
    Item DARK_OAK_BASKET = register("dark_oak_basket", new BasketItem(AirBalloonEntity.BasketType.of(BoatEntity.Type.DARK_OAK), new Item.Settings().maxCount(1)), ItemGroups.TOOLS);
    Item MANGROVE_BASKET = register("mangrove_basket", new BasketItem(AirBalloonEntity.BasketType.of(BoatEntity.Type.MANGROVE), new Item.Settings().maxCount(1)), ItemGroups.TOOLS);
    Item BAMBOO_BASKET = register("bamboo_basket", new BasketItem(AirBalloonEntity.BasketType.of(BoatEntity.Type.BAMBOO), new Item.Settings().maxCount(1)), ItemGroups.TOOLS);
    Item PALM_BASKET = register("palm_basket", new BasketItem(AirBalloonEntity.BasketType.of(UWoodTypes.PALM_BOAT_TYPE), new Item.Settings().maxCount(1)), ItemGroups.TOOLS);

    Item GIANT_BALLOON = register("giant_balloon", new HotAirBalloonItem(new Item.Settings().maxCount(1)), ItemGroups.TOOLS);

    AmuletItem PEGASUS_AMULET = register("pegasus_amulet", new PegasusAmuletItem(new FabricItemSettings()
            .maxCount(1)
            .maxDamage(890)
            .rarity(Rarity.UNCOMMON), 900), ItemGroups.TOOLS);
    AlicornAmuletItem ALICORN_AMULET = register("alicorn_amulet", new AlicornAmuletItem(new FabricItemSettings()
            .maxCount(1)
            .maxDamage(1000)
            .rarity(Rarity.RARE)), ItemGroups.TOOLS);
    Item BROKEN_ALICORN_AMULET = register("broken_alicorn_amulet", new Item(new Item.Settings()), ItemGroups.TOOLS);
    AmuletItem UNICORN_AMULET = register("unicorn_amulet", new AmuletItem(new FabricItemSettings()
            .maxCount(1)
            .maxDamage(890)
            .rarity(Rarity.UNCOMMON), 0), ItemGroups.TOOLS);

    GlassesItem SUNGLASSES = register("sunglasses", new GlassesItem(new FabricItemSettings().maxCount(1)), ItemGroups.COMBAT);
    GlassesItem BROKEN_SUNGLASSES = register("broken_sunglasses", new GlassesItem(new FabricItemSettings().maxCount(1)), ItemGroups.COMBAT);


    Item CARAPACE = register("carapace", new Item(new Item.Settings()), ItemGroups.INGREDIENTS);
    Item CLOUD_BED = register("cloud_bed", new CloudBedItem(UBlocks.CLOUD_BED, new Item.Settings().maxCount(1)), ItemGroups.FUNCTIONAL);

    Item ALICORN_BADGE = register(Race.ALICORN);
    Item PEGASUS_BADGE = register(Race.PEGASUS);
    Item UNICORN_BADGE = register(Race.UNICORN);
    Item EARTH_BADGE = register(Race.EARTH);
    Item BAT_BADGE = register(Race.BAT);
    Item CHANGELING_BADGE = register(Race.CHANGELING);
    Item KIRIN_BADGE = register(Race.KIRIN);

    private static <T extends Item> T register(String name, T item, RegistryKey<ItemGroup> group) {
        return ItemGroupRegistry.register(Unicopia.id(name), item, group);
    }

    private static <T extends Item> T register(String name, T item) {
        return ItemGroupRegistry.register(Unicopia.id(name), item);
    }

    static MusicDiscItem register(String name, SoundEvent sound, int seconds) {
        return register(name, new MusicDiscItem(1, sound, new Settings()
                .maxCount(1)
                .rarity(Rarity.RARE), seconds
            ) {}, ItemGroups.TOOLS);
    }

    static Item register(Race race) {
        return Registry.register(Registries.ITEM, race.getId().withPath(p -> p + "_badge"), new Item(new Settings()));
    }

    static void bootstrap() {
        AppleItem.registerTickCallback(Items.APPLE);
        FuelRegistry.INSTANCE.add(WOODEN_POLEARM, 200);
        FuelRegistry.INSTANCE.add(MUG, 250);
        FuelRegistry.INSTANCE.add(DRAGON_BREATH_SCROLL, 20000);
        FuelRegistry.INSTANCE.add(BUTTERFLY, 2);
        FuelRegistry.INSTANCE.add(SPELLBOOK, 9000);
        FuelRegistry.INSTANCE.add(MEADOWBROOKS_STAFF, 800);
        FuelRegistry.INSTANCE.add(BURNED_TOAST, 1600);
        FuelRegistry.INSTANCE.add(UTags.BASKETS, 1700);

        CompostingChanceRegistry.INSTANCE.add(GREEN_APPLE, 0.65F);
        CompostingChanceRegistry.INSTANCE.add(SWEET_APPLE, 0.65F);
        CompostingChanceRegistry.INSTANCE.add(SOUR_APPLE, 0.65F);
        CompostingChanceRegistry.INSTANCE.add(ZAP_APPLE, 1F);
        CompostingChanceRegistry.INSTANCE.add(ZAP_BULB, 1F);
        CompostingChanceRegistry.INSTANCE.add(ROTTEN_APPLE, 0.8F);
        CompostingChanceRegistry.INSTANCE.add(OAT_SEEDS, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(OATS, 0.1F);
        CompostingChanceRegistry.INSTANCE.add(IMPORTED_OATS, 0.5F);
        CompostingChanceRegistry.INSTANCE.add(DAFFODIL_DAISY_SANDWICH, 0.5F);
        CompostingChanceRegistry.INSTANCE.add(HAY_BURGER, 0.5F);
        CompostingChanceRegistry.INSTANCE.add(HAY_FRIES, 0.5F);
        CompostingChanceRegistry.INSTANCE.add(WHEAT_WORMS, 0.8F);
        CompostingChanceRegistry.INSTANCE.add(MUFFIN, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(PINECONE, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(ACORN, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(GREEN_APPLE_SEEDS, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(SWEET_APPLE_SEEDS, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(SOUR_APPLE_SEEDS, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(APPLE_PIE, 0.5F);
        CompostingChanceRegistry.INSTANCE.add(APPLE_PIE_HOOF, 0.5F);
        CompostingChanceRegistry.INSTANCE.add(APPLE_PIE_SLICE, 0.1F);
        CompostingChanceRegistry.INSTANCE.add(BUTTERFLY, 0.1F);

        UEnchantments.bootstrap();
        URecipes.bootstrap();
        UItemGroups.bootstrap();

        Registry.register(TerraformBoatTypeRegistry.INSTANCE, Unicopia.id("palm"), new TerraformBoatType.Builder()
                .planks(UBlocks.PALM_PLANKS.asItem())
                .item(PALM_BOAT)
                .build());
    }
}

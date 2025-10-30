package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.block.UWoodTypes;
import com.minelittlepony.unicopia.block.cloud.CloudBedBlock;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.cloud.CloudBedItem;
import com.minelittlepony.unicopia.item.component.BalloonDesignComponent;
import com.minelittlepony.unicopia.item.component.BreaksIntoItemComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.item.group.UItemGroups;
import com.minelittlepony.unicopia.recipe.URecipes;

import net.minecraft.block.Blocks;
import net.minecraft.block.WoodType;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.item.consume.UseAction;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.minecraft.util.Rarity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import static com.minelittlepony.unicopia.item.group.ItemGroupRegistry.*;

public interface UItems {
    Item GREEN_APPLE = register("green_apple", s -> AppleItem.registerTickCallback(new Item(s.food(FoodComponents.APPLE))), ItemGroups.FOOD_AND_DRINK);
    Item SWEET_APPLE = register("sweet_apple", s -> AppleItem.registerTickCallback(new Item(s.food(FoodComponents.APPLE))), ItemGroups.FOOD_AND_DRINK);
    Item SOUR_APPLE = register("sour_apple", s -> AppleItem.registerTickCallback(new Item(s.food(FoodComponents.APPLE))), ItemGroups.FOOD_AND_DRINK);

    ZapAppleItem ZAP_APPLE = register("zap_apple", s -> AppleItem.registerTickCallback(new ZapAppleItem(s.food(UFoodComponents.ZAP_APPLE, UConsumableComponents.SNACK))), ItemGroups.FOOD_AND_DRINK);
    Item ZAP_BULB = register("zap_bulb", s -> new Item(s.food(UFoodComponents.ZAP_BULB, UConsumableComponents.ZAP_BULB)), ItemGroups.FOOD_AND_DRINK);

    Item ROTTEN_APPLE = register("rotten_apple", s -> new Item(s.food(FoodComponents.APPLE)), ItemGroups.FOOD_AND_DRINK);
    Item COOKED_ZAP_APPLE = register("cooked_zap_apple", s -> new Item(s.food(FoodComponents.APPLE)), ItemGroups.FOOD_AND_DRINK);

    Item MUSIC_DISC_CRUSADE = register("music_disc_crusade", UJukeboxSongs.CRUSADE);
    Item MUSIC_DISC_PET = register("music_disc_pet", UJukeboxSongs.PET);
    Item MUSIC_DISC_POPULAR = register("music_disc_popular", UJukeboxSongs.POPULAR);
    Item MUSIC_DISC_FUNK = register("music_disc_funk", UJukeboxSongs.FUNK);

    FriendshipBraceletItem FRIENDSHIP_BRACELET = register("friendship_bracelet", s -> new FriendshipBraceletItem(s.rarity(Rarity.UNCOMMON)), ItemGroups.TOOLS);

    Item PLUNDER_VINE = register("plunder_vine", s -> new BlockItem(UBlocks.PLUNDER_VINE_BUD, s));
    Item EMPTY_JAR = register("empty_jar", s -> new EmptyJarItem(UBlocks.JAR, s.fireproof()), ItemGroups.FUNCTIONAL);
    FilledJarItem FILLED_JAR = register("filled_jar", s -> new FilledJarItem(s.maxCount(16).fireproof().recipeRemainder(EMPTY_JAR)));
    Item RAIN_CLOUD_JAR  = register("rain_cloud_jar", s -> new WeatherJarItem(UBlocks.CLOUD_JAR, s.maxCount(16).fireproof().recipeRemainder(EMPTY_JAR), WeatherJarItem.Type.RAIN), ItemGroups.FUNCTIONAL);
    Item STORM_CLOUD_JAR  = register("storm_cloud_jar", s -> new WeatherJarItem(UBlocks.STORM_JAR, s.maxCount(16).fireproof().recipeRemainder(EMPTY_JAR), WeatherJarItem.Type.THUNDER), ItemGroups.FUNCTIONAL);
    Item LIGHTNING_JAR  = register("lightning_jar", s -> new WeatherJarItem(UBlocks.LIGHTNING_JAR, s.maxCount(16).fireproof().recipeRemainder(EMPTY_JAR), WeatherJarItem.Type.LIGHTNING), ItemGroups.FUNCTIONAL);
    Item ZAP_APPLE_JAM_JAR = register("zap_apple_jam_jar", s -> new WeatherJarItem(UBlocks.ZAP_JAR, s.maxCount(16).fireproof().recipeRemainder(EMPTY_JAR), WeatherJarItem.Type.LIGHTNING), ItemGroups.FUNCTIONAL);

    Item TOAST = register("toast", s -> new Item(s.maxCount(16).food(UFoodComponents.TOAST, UConsumableComponents.SNACK)), ItemGroups.FOOD_AND_DRINK);
    Item BURNED_TOAST = register("burned_toast", s -> new Item(s.maxCount(16).food(UFoodComponents.BURNED_TOAST, UConsumableComponents.SNACK)), ItemGroups.FOOD_AND_DRINK);
    Item JAM_TOAST = register("jam_toast", s -> new Item(s.maxCount(16).food(UFoodComponents.JAM_TOAST, UConsumableComponents.SNACK)), ItemGroups.FOOD_AND_DRINK);

    Item CRYSTAL_HEART = register("crystal_heart", s -> new CrystalHeartItem(s.maxCount(1)), ItemGroups.TOOLS);
    Item CRYSTAL_SHARD = register("crystal_shard", Item::new, ItemGroups.NATURAL);

    GemstoneItem GEMSTONE = register("gemstone", GemstoneItem::new, ItemGroups.NATURAL);
    Item BOTCHED_GEM = register("botched_gem", Item::new, ItemGroups.NATURAL);

    Item PEGASUS_FEATHER = register("pegasus_feather", Item::new, ItemGroups.NATURAL);
    Item GRYPHON_FEATHER = register("gryphon_feather", Item::new, ItemGroups.NATURAL);

    Item OAT_SEEDS = register("oat_seeds", s -> new BlockItem(UBlocks.OATS, s.translationKey(UBlocks.OATS.getTranslationKey())), ItemGroups.NATURAL);
    Item OATS = register("oats", s -> new Item(s.food(UFoodComponents.OATS)), ItemGroups.FOOD_AND_DRINK);
    Item IMPORTED_OATS = register("imported_oats", s -> new Item(s.food(UFoodComponents.IMPORTED_OATS)), ItemGroups.FOOD_AND_DRINK);
    Item OATMEAL = register("oatmeal", s -> new Item(s.recipeRemainder(Items.BOWL).maxCount(1).food(UFoodComponents.OATMEAL, UConsumableComponents.OATMEAL).useRemainder(Items.BOWL)), ItemGroups.FOOD_AND_DRINK);

    Item OATMEAL_COOKIE = register("oatmeal_cookie", s -> new Item(s.food(UFoodComponents.OATMEAL_COOKIE)), ItemGroups.FOOD_AND_DRINK);
    Item CHOCOLATE_OATMEAL_COOKIE = register("chocolate_oatmeal_cookie", s -> new Item(s.food(UFoodComponents.CHOCOLATE_OATMEAL_COOKIE)), ItemGroups.FOOD_AND_DRINK);
    Item PINECONE_COOKIE = register("pinecone_cookie", s -> new Item(s.food(FoodComponents.COOKIE)), ItemGroups.FOOD_AND_DRINK);
    Item BOWL_OF_NUTS = register("bowl_of_nuts", s -> new Item(s.food(UFoodComponents.NUT_BOWL).recipeRemainder(Items.BOWL)), ItemGroups.FOOD_AND_DRINK);
    Item SCONE = register("scone", s -> new MuffinItem(s.maxCount(32).food(UFoodComponents.SCONE), 0), ItemGroups.FOOD_AND_DRINK);

    Item DAFFODIL_DAISY_SANDWICH = register("daffodil_daisy_sandwich", s -> new Item(s.food(UFoodComponents.DAFODIL_DAISY_SANDWICH)), ItemGroups.FOOD_AND_DRINK);
    Item HAY_BURGER = register("hay_burger", s -> new Item(s.maxCount(1).food(UFoodComponents.BURGER)), ItemGroups.FOOD_AND_DRINK);
    Item HAY_FRIES = register("hay_fries", s -> new Item(s.maxCount(16).food(UFoodComponents.HAY_FRIES)), ItemGroups.FOOD_AND_DRINK);
    Item CRISPY_HAY_FRIES = register("crispy_hay_fries", s -> new Item(s.maxCount(16).food(UFoodComponents.CRISPY_HAY_FRIES)), ItemGroups.FOOD_AND_DRINK);
    /**
     * https://mlp.fandom.com/wiki/Food_and_beverage
     */
    Item HORSE_SHOE_FRIES = register("horse_shoe_fries", s -> new Item(s.maxCount(32).food(UFoodComponents.HAY_FRIES)), ItemGroups.FOOD_AND_DRINK);

    Item WHEAT_WORMS = register("wheat_worms", s -> new Item(s.maxCount(16).food(UFoodComponents.WORMS)), ItemGroups.NATURAL); //ItemTags.WOLF_FOOD
    Item BAITED_FISHING_ROD = register("baited_fishing_rod", s -> new BaitedFishingRodItem(s.maxDamage(64)), ItemGroups.TOOLS);
    Item MUFFIN = register("muffin", s -> new MuffinItem(s.maxCount(32).food(FoodComponents.BREAD), 0), ItemGroups.FOOD_AND_DRINK);
    Item PINECONE = register("pinecone", s -> new ForageableItem(s.food(UFoodComponents.PINECONE, UConsumableComponents.SNACK).maxCount(16), () -> Blocks.SPRUCE_LEAVES), ItemGroups.FOOD_AND_DRINK);
    Item ACORN = register("acorn", s -> new ForageableItem(s.food(UFoodComponents.ACORN, UConsumableComponents.SNACK).maxCount(16), () -> Blocks.OAK_LEAVES), ItemGroups.FOOD_AND_DRINK);
    Item MANGO = register("mango", s -> new Item(s.food(UFoodComponents.MANGO)), ItemGroups.FOOD_AND_DRINK);
    Item BANANA = register("banana", s -> new Item(s.food(UFoodComponents.BANANA)), ItemGroups.FOOD_AND_DRINK);
    Item CURING_JOKE = register("curing_joke", s -> new CuringJokeItem(UBlocks.CURING_JOKE, s.food(UFoodComponents.POISON_JOKE, UConsumableComponents.SNACK)), ItemGroups.NATURAL);
    Item PINEAPPLE = register("pineapple", s -> new PineappleItem(s.food(UFoodComponents.BANANA).maxDamage(3)), ItemGroups.FOOD_AND_DRINK);
    Item PINEAPPLE_CROWN = register("pineapple_crown", s -> new BlockItem(UBlocks.PINEAPPLE, s.translationKey(UBlocks.PINEAPPLE.getTranslationKey())), ItemGroups.NATURAL);

    Item PEBBLES = register("pebbles", s -> new BlockItem(UBlocks.ROCKS, s.translationKey(UBlocks.ROCKS.getTranslationKey())), ItemGroups.NATURAL);
    Item ROCK = register("rock", s -> new HeavyProjectileItem(s, 3), ItemGroups.NATURAL);
    Item WEIRD_ROCK = register("weird_rock", s -> new Item(s.attributeModifiers(AttributeModifiersComponent.builder()
            .add(EntityAttributes.LUCK, new EntityAttributeModifier(UItemModifierIds.LUCK_MODIFIER_ID, 9, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
            .build())), ItemGroups.NATURAL);
    Item TOM = register("tom", s -> new Item(s.attributeModifiers(AttributeModifiersComponent.builder()
            .add(EntityAttributes.KNOCKBACK_RESISTANCE, new EntityAttributeModifier(UItemModifierIds.KNOCKBACK_MODIFIER_ID, 0.9, EntityAttributeModifier.Operation.ADD_VALUE), AttributeModifierSlot.MAINHAND)
            .build())), ItemGroups.NATURAL);
    Item ROCK_STEW = register("rock_stew", s -> new Item(s.food(FoodComponents.MUSHROOM_STEW).maxCount(1).recipeRemainder(Items.BOWL)), ItemGroups.FOOD_AND_DRINK);
    Item ROCK_CANDY = register("rock_candy", s -> new Item(s.food(UFoodComponents.CANDY).maxCount(16)), ItemGroups.FOOD_AND_DRINK);
    Item SALT_CUBE = register("salt_cube", s -> new Item(s.food(UFoodComponents.SALT_CUBE)), ItemGroups.FOOD_AND_DRINK);

    Item GREEN_APPLE_SEEDS = register("green_apple_seeds", s -> new BlockItem(UBlocks.GREEN_APPLE_SPROUT, s), ItemGroups.NATURAL);
    Item SWEET_APPLE_SEEDS = register("sweet_apple_seeds", s -> new BlockItem(UBlocks.SWEET_APPLE_SPROUT, s), ItemGroups.NATURAL);
    Item SOUR_APPLE_SEEDS = register("sour_apple_seeds", s -> new BlockItem(UBlocks.SOUR_APPLE_SPROUT, s), ItemGroups.NATURAL);
    Item GOLDEN_OAK_SEEDS = register("golden_oak_seeds", s -> new BlockItem(UBlocks.GOLDEN_OAK_SPROUT, s), ItemGroups.NATURAL);

    Item MUG = register("mug", Item::new, ItemGroups.TOOLS);
    Item CIDER = register("cider", s -> new ConsumableItem(s.food(UFoodComponents.CIDER).maxCount(16).recipeRemainder(MUG), UseAction.DRINK), ItemGroups.FOOD_AND_DRINK);
    Item JUICE = register("juice", s -> new ConsumableItem(s.recipeRemainder(Items.GLASS_BOTTLE).maxCount(16).food(UFoodComponents.JUICE), UseAction.DRINK), ItemGroups.FOOD_AND_DRINK);
    Item BURNED_JUICE = register("burned_juice", s -> new ConsumableItem(s.recipeRemainder(Items.GLASS_BOTTLE).maxCount(16).food(UFoodComponents.BURNED_JUICE), UseAction.DRINK), ItemGroups.FOOD_AND_DRINK);
    Item APPLE_PIE = register("apple_pie", s -> new BlockItem(UBlocks.APPLE_PIE, s.maxCount(1)), ItemGroups.FOOD_AND_DRINK);
    Item APPLE_PIE_HOOF = register("apple_pie_hoof", s -> new BlockItem(UBlocks.APPLE_PIE, s.maxCount(1)), ItemGroups.FOOD_AND_DRINK);
    Item APPLE_PIE_SLICE = register("apple_pie_slice", s -> new Item(s.maxCount(16).food(UFoodComponents.PIE)), ItemGroups.FOOD_AND_DRINK);
    Item CANDIED_APPLE = register("candied_apple", s -> new StagedFoodItem(s.food(UFoodComponents.CANDY).maxDamage(3), () -> Items.STICK), ItemGroups.FOOD_AND_DRINK);

    Item LOVE_BOTTLE = register("love_bottle", s -> new ConsumableItem(s.food(UFoodComponents.LOVE_BOTTLE, ConsumableComponents.DRINK).maxCount(1).recipeRemainder(Items.GLASS_BOTTLE), UseAction.DRINK), ItemGroups.FOOD_AND_DRINK);
    Item LOVE_BUCKET = register("love_bucket", s -> new ConsumableItem(s.food(UFoodComponents.LOVE_BUCKET, ConsumableComponents.DRINK).recipeRemainder(Items.BUCKET), UseAction.DRINK), ItemGroups.FOOD_AND_DRINK);
    Item LOVE_MUG = register("love_mug", s -> new ConsumableItem(s.food(UFoodComponents.LOVE_MUG, ConsumableComponents.DRINK).recipeRemainder(MUG), UseAction.DRINK), ItemGroups.FOOD_AND_DRINK);

    Item GOLDEN_FEATHER = register("golden_feather", s -> new Item(s.rarity(Rarity.UNCOMMON)), ItemGroups.NATURAL);
    Item GOLDEN_WING = register("golden_wing", s -> new Item(s.rarity(Rarity.UNCOMMON)), ItemGroups.NATURAL);

    Item DRAGON_BREATH_SCROLL = register("dragon_breath_scroll", s -> new DragonBreathScrollItem(s.rarity(Rarity.UNCOMMON)), ItemGroups.TOOLS);
    Item GROGARS_BELL = register("grogars_bell", s -> new BellItem(s.rarity(Rarity.RARE).maxCount(1)), ItemGroups.TOOLS);
    Item MEADOWBROOKS_STAFF = register("meadowbrooks_staff", s -> new StaffItem(s.rarity(Rarity.UNCOMMON).maxCount(1).maxDamage(120)), ItemGroups.TOOLS);
    Item MAGIC_STAFF = register("magic_staff", s -> new EnchantedStaffItem(s.rarity(Rarity.UNCOMMON).maxCount(1).maxDamage(120)), ItemGroups.TOOLS);

    Item IRON_HORSE_SHOE = register("iron_horse_shoe", s -> new HorseShoeItem(s.maxDamage(200), 4, 0.6F, 1), ItemGroups.COMBAT);
    Item GOLDEN_HORSE_SHOE = register("golden_horse_shoe", s -> new HorseShoeItem(s.maxDamage(100), 5, 0.1F, 0.5F), ItemGroups.COMBAT);
    Item COPPER_HORSE_SHOE = register("copper_horse_shoe", s -> new HorseShoeItem(s.maxDamage(250), 6, 0.5F, 0.8F), ItemGroups.COMBAT);
    Item NETHERITE_HORSE_SHOE = register("netherite_horse_shoe", s -> new HorseShoeItem(s.maxDamage(800), 3, 0.7F, 1.2F), ItemGroups.COMBAT);

    Item WOODEN_POLEARM = register("wooden_polearm", s -> new PolearmItem(ToolMaterial.WOOD, 2, -3.2F, 2, s), ItemGroups.COMBAT);
    Item STONE_POLEARM = register("stone_polearm", s -> new PolearmItem(ToolMaterial.STONE, 2, -3.2F, 2, s), ItemGroups.COMBAT);
    Item IRON_POLEARM = register("iron_polearm", s -> new PolearmItem(ToolMaterial.IRON, 2, -3.1F, 3, s), ItemGroups.COMBAT);
    Item GOLDEN_POLEARM = register("golden_polearm", s -> new PolearmItem(ToolMaterial.GOLD, 3, -3F, 4, s), ItemGroups.COMBAT);
    Item DIAMOND_POLEARM = register("diamond_polearm", s -> new PolearmItem(ToolMaterial.DIAMOND, 3, -3F, 5, s), ItemGroups.COMBAT);
    Item NETHERITE_POLEARM = register("netherite_polearm", s -> new PolearmItem(ToolMaterial.NETHERITE, 3, -3F, 6, s.fireproof()), ItemGroups.COMBAT);

    Item LOOT_BUG_SPAWN_EGG = register("loot_bug_spawn_egg", s -> new SpawnEggItem(UEntities.LOOT_BUG, 0x3C9D14, 0xE66F16, s), ItemGroups.SPAWN_EGGS);
    Item BUTTERFLY_SPAWN_EGG = register("butterfly_spawn_egg", s -> new SpawnEggItem(UEntities.BUTTERFLY, 0x222200, 0xAAEEFF, s), ItemGroups.SPAWN_EGGS);
    Item BUTTERFLY = register("butterfly", s -> new ButterflyItem(s.food(UFoodComponents.INSECTS)), ItemGroups.FOOD_AND_DRINK);

    Item PALM_BOAT = register("palm_boat", s -> new BoatItem(UEntities.PALM_BOAT, s.maxCount(1)), ItemGroups.FUNCTIONAL);
    Item PALM_CHEST_BOAT = register("palm_chest_boat", s -> new BoatItem(UEntities.PALM_CHEST_BOAT, s.maxCount(1)), ItemGroups.FUNCTIONAL);
    Item PALM_SIGN = register("palm_sign", s -> new SignItem(UBlocks.PALM_SIGN, UBlocks.PALM_WALL_SIGN, s), ItemGroups.FUNCTIONAL);
    Item PALM_HANGING_SIGN = register("palm_hanging_sign", s -> new HangingSignItem(UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_WALL_HANGING_SIGN, s), ItemGroups.FUNCTIONAL);

    Item SPELLBOOK = register("spellbook", s -> new SpellbookItem(s.maxCount(1).rarity(Rarity.UNCOMMON)), ItemGroups.TOOLS);

    Item OAK_BASKET = register("oak_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.OAK), s.maxCount(1)), ItemGroups.TOOLS);
    Item SPRUCE_BASKET = register("spruce_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.SPRUCE), s.maxCount(1)), ItemGroups.TOOLS);
    Item BIRCH_BASKET = register("birch_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.BIRCH), s.maxCount(1)), ItemGroups.TOOLS);
    Item JUNGLE_BASKET = register("jungle_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.JUNGLE), s.maxCount(1)), ItemGroups.TOOLS);
    Item ACACIA_BASKET = register("acacia_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.ACACIA), s.maxCount(1)), ItemGroups.TOOLS);
    Item CHERRY_BASKET = register("cherry_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.CHERRY), s.maxCount(1)), ItemGroups.TOOLS);
    Item DARK_OAK_BASKET = register("dark_oak_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.DARK_OAK), s.maxCount(1)), ItemGroups.TOOLS);
    Item MANGROVE_BASKET = register("mangrove_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.MANGROVE), s.maxCount(1)), ItemGroups.TOOLS);
    Item BAMBOO_BASKET = register("bamboo_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(WoodType.BAMBOO), s.maxCount(1)), ItemGroups.TOOLS);
    Item PALM_BASKET = register("palm_basket", s -> new BasketItem(AirBalloonEntity.BasketType.of(UWoodTypes.PALM), s.maxCount(1)), ItemGroups.TOOLS);

    Item GIANT_BALLOON = register("giant_balloon", s -> new GiantBalloonItem(s.maxCount(1).component(UDataComponentTypes.BALLOON_DESIGN, BalloonDesignComponent.DEFAULT)), ItemGroups.TOOLS);
    Item SPECTRAL_CLOCK = register("spectral_clock", Item::new, ItemGroups.TOOLS);
    Item TOTEM_OF_DYING = register("totem_of_dying", s -> new Item(s.maxCount(1).rarity(Rarity.UNCOMMON)), ItemGroups.COMBAT);

    Item WHITE_BED_SHEETS = register(CloudBedBlock.SheetPattern.WHITE);
    Item LIGHT_GRAY_BED_SHEETS = register(CloudBedBlock.SheetPattern.LIGHT_GRAY);
    Item GRAY_BED_SHEETS = register(CloudBedBlock.SheetPattern.GRAY);
    Item BLACK_BED_SHEETS = register(CloudBedBlock.SheetPattern.BLACK);
    Item BROWN_BED_SHEETS = register(CloudBedBlock.SheetPattern.BROWN);
    Item RED_BED_SHEETS = register(CloudBedBlock.SheetPattern.RED);
    Item ORANGE_BED_SHEETS = register(CloudBedBlock.SheetPattern.ORANGE);
    Item YELLOW_BED_SHEETS = register(CloudBedBlock.SheetPattern.YELLOW);
    Item LIME_BED_SHEETS = register(CloudBedBlock.SheetPattern.LIME);
    Item GREEN_BED_SHEETS = register(CloudBedBlock.SheetPattern.GREEN);
    Item CYAN_BED_SHEETS = register(CloudBedBlock.SheetPattern.CYAN);
    Item LIGHT_BLUE_BED_SHEETS = register(CloudBedBlock.SheetPattern.LIGHT_BLUE);
    Item BLUE_BED_SHEETS = register(CloudBedBlock.SheetPattern.BLUE);
    Item PURPLE_BED_SHEETS = register(CloudBedBlock.SheetPattern.PURPLE);
    Item MAGENTA_BED_SHEETS = register(CloudBedBlock.SheetPattern.MAGENTA);
    Item PINK_BED_SHEETS = register(CloudBedBlock.SheetPattern.PINK);

    Item APPLE_BED_SHEETS = register(CloudBedBlock.SheetPattern.APPLE);
    Item BARRED_BED_SHEETS = register("barred_bed_sheets", s -> new BedsheetsItem(CloudBedBlock.SheetPattern.BARS, s.maxCount(8)), ItemGroups.FUNCTIONAL);
    Item CHECKERED_BED_SHEETS = register("checkered_bed_sheets", s -> new BedsheetsItem(CloudBedBlock.SheetPattern.CHECKER, s.maxCount(8)), ItemGroups.FUNCTIONAL);
    Item KELP_BED_SHEETS = register(CloudBedBlock.SheetPattern.KELP);
    Item RAINBOW_BED_SHEETS = register(CloudBedBlock.SheetPattern.RAINBOW);
    Item RAINBOW_BPW_BED_SHEETS = register(CloudBedBlock.SheetPattern.RAINBOW_BPW);
    Item RAINBOW_BPY_BED_SHEETS = register(CloudBedBlock.SheetPattern.RAINBOW_BPY);
    Item RAINBOW_PBG_BED_SHEETS = register(CloudBedBlock.SheetPattern.RAINBOW_PBG);
    Item RAINBOW_PWR_BED_SHEETS = register(CloudBedBlock.SheetPattern.RAINBOW_PWR);

    AmuletItem PEGASUS_AMULET = register("pegasus_amulet", s -> new PegasusAmuletItem(s
            .maxCount(1)
            .maxDamage(890)
            .rarity(Rarity.UNCOMMON), 900), ItemGroups.TOOLS);
    AlicornAmuletItem ALICORN_AMULET = register("alicorn_amulet", s -> new AlicornAmuletItem(s
            .maxCount(1)
            .maxDamage(1000)
            .rarity(Rarity.RARE)), ItemGroups.TOOLS);
    Item BROKEN_ALICORN_AMULET = register("broken_alicorn_amulet", Item::new, ItemGroups.TOOLS);
    AmuletItem UNICORN_AMULET = register("unicorn_amulet", s -> new AmuletItem(s
            .maxCount(1)
            .maxDamage(890)
            .rarity(Rarity.UNCOMMON), 0), ItemGroups.TOOLS);
    AmuletItem PEARL_NECKLACE = register("pearl_necklace", s -> new AmuletItem(s
            .maxCount(1)
            .maxDamage(16)
            .rarity(Rarity.UNCOMMON), 0), ItemGroups.TOOLS);

    GlassesItem SUNGLASSES = register("sunglasses", s -> new GlassesItem(s
            .maxCount(1)
            .component(UDataComponentTypes.ITEM_AFTER_BREAKING, new BreaksIntoItemComponent(
                    UTags.DamageTypes.BREAKS_SUNGLASSES,
                    RegistryKey.of(RegistryKeys.ITEM, Unicopia.id("broken_sunglasses")),
                    USounds.ITEM_SUNGLASSES_SHATTER.getKey().get())
            )
            ), ItemGroups.COMBAT);
    GlassesItem BROKEN_SUNGLASSES = register("broken_sunglasses", s -> new GlassesItem(s.maxCount(1)), ItemGroups.COMBAT);

    Item CLAM_SHELL = register("clam_shell", Item::new, ItemGroups.INGREDIENTS);
    Item SCALLOP_SHELL = register("scallop_shell", Item::new, ItemGroups.INGREDIENTS);
    Item TURRET_SHELL = register("turret_shell", Item::new, ItemGroups.INGREDIENTS);
    Item SHELLY = register("shelly", Item::new, ItemGroups.INGREDIENTS);

    Item ROTTEN_COD = register("rotten_cod", s -> new Item(s.food(FoodComponents.ROTTEN_FLESH)), ItemGroups.FOOD_AND_DRINK);
    Item ROTTEN_SALMON = register("rotten_salmon", s -> new Item(s.food(FoodComponents.ROTTEN_FLESH)), ItemGroups.FOOD_AND_DRINK);
    Item ROTTEN_TROPICAL_FISH = register("rotten_tropical_fish", s -> new Item(s.food(FoodComponents.ROTTEN_FLESH)), ItemGroups.FOOD_AND_DRINK);
    Item ROTTEN_PUFFERFISH = register("rotten_pufferfish", s -> new Item(s.food(UFoodComponents.ROTTEN_PUFFERFISH, UConsumableComponents.poisonedFish(1, 1, 1))), ItemGroups.FOOD_AND_DRINK); //ItemTags.WOLF_FOOD

    Item COOKED_TROPICAL_FISH = register("cooked_tropical_fish", s -> new Item(s.food(FoodComponents.COOKED_COD)), ItemGroups.FOOD_AND_DRINK);
    Item COOKED_PUFFERFISH = register("cooked_pufferfish", s -> new Item(s.food(UFoodComponents.COOKED_PUFFERFISH, UConsumableComponents.poisonedFish(1, 0.3F, 0.4F))), ItemGroups.FOOD_AND_DRINK);
    Item FRIED_AXOLOTL = register("fried_axolotl", s -> new ConsumableItem(s.food(FoodComponents.COOKED_CHICKEN).maxCount(1).recipeRemainder(Items.BUCKET), UseAction.EAT), ItemGroups.FOOD_AND_DRINK);
    Item GREEN_FRIED_EGG = register("green_fried_egg", s -> new Item(s.food(UFoodComponents.FRIED_EGG)), ItemGroups.FOOD_AND_DRINK);

    Item FROG_LEGS = register("frog_legs", s -> new Item(s.food(FoodComponents.CHICKEN)), ItemGroups.FOOD_AND_DRINK);
    Item COOKED_FROG_LEGS = register("cooked_frog_legs", s -> new Item(s.food(FoodComponents.COOKED_CHICKEN)), ItemGroups.FOOD_AND_DRINK);

    Item GOLDEN_STICK = register("golden_stick", s -> new Item(s.recipeRemainder(Items.GOLD_NUGGET)), ItemGroups.NATURAL);

    Item CARAPACE = register("carapace", Item::new, ItemGroups.INGREDIENTS);
    Item CLOTH_BED = register("cloth_bed", s -> new FancyBedItem(UBlocks.CLOTH_BED, s.maxCount(1)), ItemGroups.FUNCTIONAL);
    Item CLOUD_BED = register("cloud_bed", s -> new CloudBedItem(UBlocks.CLOUD_BED, s.maxCount(1)), ItemGroups.FUNCTIONAL);
    Item CLOUD_LUMP = register("cloud_lump", Item::new, ItemGroups.NATURAL);

    Item ALICORN_BADGE = register(Race.ALICORN);
    Item PEGASUS_BADGE = register(Race.PEGASUS);
    Item UNICORN_BADGE = register(Race.UNICORN);
    Item EARTH_BADGE = register(Race.EARTH);
    Item BAT_BADGE = register(Race.BAT);
    Item CHANGELING_BADGE = register(Race.CHANGELING);
    Item KIRIN_BADGE = register(Race.KIRIN);
    Item HIPPOGRIFF_BADGE = register(Race.HIPPOGRIFF);

    static void bootstrap() {
        AppleItem.registerTickCallback(Items.APPLE);

        FuelRegistryEvents.BUILD.register((builder, context) -> {
            builder.add(ROTTEN_APPLE, 150);
            builder.add(WOODEN_POLEARM, 200);
            builder.add(MUG, 250);
            builder.add(DRAGON_BREATH_SCROLL, 20000);
            builder.add(BUTTERFLY, 2);
            builder.add(SPELLBOOK, 9000);
            builder.add(MEADOWBROOKS_STAFF, 800);
            builder.add(BURNED_TOAST, 1600);
            builder.add(UTags.Items.BASKETS, 1700);
        });

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
    }
}

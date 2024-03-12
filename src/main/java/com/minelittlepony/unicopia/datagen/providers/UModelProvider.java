package com.minelittlepony.unicopia.datagen.providers;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.item.BedsheetsItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.server.world.UTreeGen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateModelGenerator.TintType;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.ModelIds;

public class UModelProvider extends FabricModelProvider {
    public UModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator modelGenerator) {
        // palm wood
        registerLogSet(modelGenerator, UBlocks.PALM_LOG, UBlocks.PALM_WOOD);
        registerLogSet(modelGenerator, UBlocks.STRIPPED_PALM_LOG, UBlocks.STRIPPED_PALM_WOOD);
        modelGenerator.registerCubeAllModelTexturePool(UBlocks.PALM_PLANKS).family(new BlockFamily.Builder(UBlocks.PALM_PLANKS)
                .slab(UBlocks.PALM_SLAB).stairs(UBlocks.PALM_STAIRS).fence(UBlocks.PALM_FENCE).fenceGate(UBlocks.PALM_FENCE_GATE)
                .button(UBlocks.PALM_BUTTON).pressurePlate(UBlocks.PALM_PRESSURE_PLATE).sign(UBlocks.PALM_SIGN, UBlocks.PALM_WALL_SIGN)
                .door(UBlocks.PALM_DOOR).trapdoor(UBlocks.PALM_TRAPDOOR)
                .group("wooden").unlockCriterionName("has_planks")
                .build());
        modelGenerator.registerHangingSign(UBlocks.STRIPPED_PALM_LOG, UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_WALL_HANGING_SIGN);
        modelGenerator.registerSimpleCubeAll(UBlocks.PALM_LEAVES);
        modelGenerator.registerFlowerPotPlant(UTreeGen.BANANA_TREE.sapling().get(), UTreeGen.BANANA_TREE.pot().get(), TintType.NOT_TINTED);

        // zap wood
        modelGenerator.registerLog(UBlocks.ZAP_LOG)
            .log(UBlocks.ZAP_LOG).wood(UBlocks.ZAP_WOOD)
            .log(UBlocks.WAXED_ZAP_LOG).wood(UBlocks.WAXED_ZAP_WOOD);
        modelGenerator.registerLog(UBlocks.STRIPPED_ZAP_LOG)
            .log(UBlocks.STRIPPED_ZAP_LOG).wood(UBlocks.STRIPPED_ZAP_WOOD)
            .log(UBlocks.WAXED_STRIPPED_ZAP_LOG).wood(UBlocks.WAXED_STRIPPED_ZAP_WOOD);
        modelGenerator.registerCubeAllModelTexturePool(UBlocks.ZAP_PLANKS)
            .family(new BlockFamily.Builder(UBlocks.ZAP_PLANKS)
                .slab(UBlocks.ZAP_SLAB).stairs(UBlocks.ZAP_STAIRS).fence(UBlocks.ZAP_FENCE).fenceGate(UBlocks.ZAP_FENCE_GATE)
                .group("wooden").unlockCriterionName("has_planks")
                .build())
            .same(UBlocks.WAXED_ZAP_PLANKS).family(new BlockFamily.Builder(UBlocks.WAXED_ZAP_PLANKS)
                .slab(UBlocks.WAXED_ZAP_SLAB).stairs(UBlocks.WAXED_ZAP_STAIRS).fence(UBlocks.WAXED_ZAP_FENCE).fenceGate(UBlocks.WAXED_ZAP_FENCE_GATE)
                .group("wooden").unlockCriterionName("has_planks")
                .build());
        modelGenerator.registerFlowerPotPlant(UTreeGen.ZAP_APPLE_TREE.sapling().get(), UTreeGen.ZAP_APPLE_TREE.pot().get(), TintType.NOT_TINTED);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        ItemModels.register(itemModelGenerator,
                UItems.ACORN, UItems.APPLE_PIE_HOOF, UItems.APPLE_PIE_SLICE, UItems.APPLE_PIE,
                UItems.BANANA, UItems.BOTCHED_GEM, UItems.BROKEN_SUNGLASSES, UItems.BURNED_JUICE, UItems.BURNED_TOAST,
                UItems.CARAPACE, UItems.CLAM_SHELL, UItems.COOKED_ZAP_APPLE, UItems.CRISPY_HAY_FRIES, UItems.CRYSTAL_HEART, UItems.CRYSTAL_SHARD, UItems.CURING_JOKE,
                UItems.DAFFODIL_DAISY_SANDWICH, UItems.DRAGON_BREATH_SCROLL,
                UItems.EMPTY_JAR,
                UItems.FRIENDSHIP_BRACELET,
                UItems.GIANT_BALLOON, UItems.GOLDEN_FEATHER, UItems.GOLDEN_OAK_SEEDS, UItems.GOLDEN_WING, UItems.GREEN_APPLE_SEEDS, UItems.GREEN_APPLE, UItems.GROGARS_BELL,
                    UItems.GRYPHON_FEATHER,
                UItems.HAY_BURGER, UItems.HAY_FRIES, UItems.HORSE_SHOE_FRIES,
                UItems.IMPORTED_OATS,
                UItems.JAM_TOAST, UItems.JUICE,
                UItems.LIGHTNING_JAR,
                UItems.MANGO, UItems.MUFFIN,
                UItems.OAT_SEEDS, UItems.OATMEAL, UItems.OATS,
                UItems.PEBBLES, UItems.PEGASUS_FEATHER, UItems.PINECONE,
                UItems.RAIN_CLOUD_JAR, UItems.ROCK_STEW, UItems.ROCK, UItems.ROTTEN_APPLE,
                UItems.SALT_CUBE, UItems.SCALLOP_SHELL, UItems.SHELLY, UItems.SOUR_APPLE_SEEDS, UItems.SOUR_APPLE, UItems.SPELLBOOK, UItems.STORM_CLOUD_JAR,
                    UItems.SWEET_APPLE_SEEDS, UItems.SWEET_APPLE,
                UItems.TOAST, UItems.TOM, UItems.TURRET_SHELL,
                UItems.WEIRD_ROCK, UItems.WHEAT_WORMS,
                UItems.ZAP_APPLE_JAM_JAR, UItems.ZAP_APPLE, UItems.ZAP_BULB,

        // discs
                UItems.MUSIC_DISC_CRUSADE, UItems.MUSIC_DISC_FUNK, UItems.MUSIC_DISC_PET, UItems.MUSIC_DISC_POPULAR,

        // baskets
                UItems.ACACIA_BASKET, UItems.BAMBOO_BASKET, UItems.BIRCH_BASKET, UItems.CHERRY_BASKET,
                UItems.DARK_OAK_BASKET, UItems.JUNGLE_BASKET, UItems.MANGROVE_BASKET, UItems.OAK_BASKET, UItems.SPRUCE_BASKET,
                UItems.PALM_BASKET,

        // boats
                UItems.PALM_BOAT, UItems.PALM_CHEST_BOAT,

        // horseshoes
                UItems.COPPER_HORSE_SHOE, UItems.GOLDEN_HORSE_SHOE, UItems.IRON_HORSE_SHOE, UItems.NETHERITE_HORSE_SHOE
        );

        // spawn eggs
        ItemModels.register(itemModelGenerator, ItemModels.TEMPLATE_SPAWN_EGG,
                UItems.BUTTERFLY_SPAWN_EGG, UItems.LOOT_BUG_SPAWN_EGG
        );

        // amulets
        ItemModels.register(itemModelGenerator, ItemModels.TEMPLATE_AMULET,
                UItems.ALICORN_AMULET, UItems.BROKEN_ALICORN_AMULET, UItems.PEARL_NECKLACE, UItems.PEGASUS_AMULET, UItems.UNICORN_AMULET
        );

        // mugs
        ItemModels.register(itemModelGenerator, ItemModels.TEMPLATE_MUG,
                UItems.CIDER, UItems.LOVE_BOTTLE, UItems.LOVE_BUCKET, UItems.LOVE_MUG, UItems.MUG
        );

        // staffs
        ItemModels.register(itemModelGenerator, ItemModels.HANDHELD_STAFF,
                UItems.MEADOWBROOKS_STAFF
        );

        // polearms
        for (Item item : new Item[] {
                UItems.DIAMOND_POLEARM, UItems.GOLDEN_POLEARM, UItems.NETHERITE_POLEARM, UItems.STONE_POLEARM, UItems.WOODEN_POLEARM, UItems.IRON_POLEARM
        }) {
            ItemModels.registerPolearm(itemModelGenerator, item);
        }

        // sheets
        ItemModels.register(itemModelGenerator, BedsheetsItem.ITEMS.values().stream().toArray(Item[]::new));
        // badges
        ItemModels.register(itemModelGenerator, Race.REGISTRY.stream()
                .map(race -> race.getId().withPath(p -> p + "_badge"))
                .flatMap(id -> Registries.ITEM.getOrEmpty(id).stream())
                .toArray(Item[]::new));
    }

    private void registerLogSet(BlockStateModelGenerator modelGenerator, Block log, Block wood) {
        modelGenerator.registerLog(log).log(log).wood(wood);
    }

    public void registerParentedAxisRotatedCubeColumn(BlockStateModelGenerator modelGenerator, Block modelSource, Block child) {
        Identifier vertical = ModelIds.getBlockModelId(modelSource);
        Identifier horizontal = ModelIds.getBlockSubModelId(modelSource, "_horizontal");
        modelGenerator.blockStateCollector.accept(BlockStateModelGenerator.createAxisRotatedBlockState(child, vertical, horizontal));
        modelGenerator.registerParentedItemModel(child, vertical);
    }
}

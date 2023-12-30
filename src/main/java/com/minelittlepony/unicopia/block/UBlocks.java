package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.cloud.CloudPillarBlock;
import com.minelittlepony.unicopia.block.cloud.CloudSlabBlock;
import com.minelittlepony.unicopia.block.cloud.CloudStairsBlock;
import com.minelittlepony.unicopia.block.cloud.CompactedCloudBlock;
import com.minelittlepony.unicopia.block.cloud.NaturalCloudBlock;
import com.minelittlepony.unicopia.block.cloud.OrientedCloudBlock;
import com.minelittlepony.unicopia.block.cloud.PoreousCloudStairsBlock;
import com.minelittlepony.unicopia.block.cloud.ShapingBenchBlock;
import com.minelittlepony.unicopia.block.cloud.CloudBedBlock;
import com.minelittlepony.unicopia.block.cloud.CloudChestBlock;
import com.minelittlepony.unicopia.block.cloud.CloudDoorBlock;
import com.minelittlepony.unicopia.block.cloud.CloudLike;
import com.minelittlepony.unicopia.block.cloud.SoggyCloudBlock;
import com.minelittlepony.unicopia.block.cloud.SoggyCloudSlabBlock;
import com.minelittlepony.unicopia.block.cloud.SoggyCloudStairsBlock;
import com.minelittlepony.unicopia.block.cloud.UnstableCloudBlock;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.cloud.CloudBlockItem;
import com.minelittlepony.unicopia.item.group.ItemGroupRegistry;
import com.minelittlepony.unicopia.server.world.UTreeGen;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.Registries;

public interface UBlocks {
    List<Block> TRANSLUCENT_BLOCKS = new ArrayList<>();
    List<Block> SEMI_TRANSPARENT_BLOCKS = new ArrayList<>();

    Block ROCKS = register("rocks", new RockCropBlock(Settings.create()
            .mapColor(MapColor.STONE_GRAY)
            .nonOpaque()
            .pistonBehavior(PistonBehavior.DESTROY)
            .requiresTool()
            .ticksRandomly()
            .strength(2)
            .sounds(BlockSoundGroup.STONE)));

    Block FROSTED_OBSIDIAN = register("frosted_obsidian", new FrostedObsidianBlock(FabricBlockSettings.copy(Blocks.OBSIDIAN).ticksRandomly()));

    Block ZAP_LOG = register("zap_log", new ZapAppleLogBlock(Blocks.OAK_LOG.getDefaultState(), ZapAppleLogBlock.settings(MapColor.GRAY, MapColor.DEEPSLATE_GRAY)), ItemGroups.BUILDING_BLOCKS);
    Block ZAP_WOOD = register("zap_wood", new ZapAppleLogBlock(Blocks.OAK_WOOD.getDefaultState(), ZapAppleLogBlock.settings(MapColor.DEEPSLATE_GRAY, MapColor.DEEPSLATE_GRAY)), ItemGroups.BUILDING_BLOCKS);

    Block STRIPPED_ZAP_LOG = register("stripped_zap_log", new ZapAppleLogBlock(Blocks.STRIPPED_OAK_LOG.getDefaultState(), ZapAppleLogBlock.settings(MapColor.LIGHT_GRAY, MapColor.GRAY)), ItemGroups.BUILDING_BLOCKS);
    Block STRIPPED_ZAP_WOOD = register("stripped_zap_wood", new ZapAppleLogBlock(Blocks.STRIPPED_OAK_WOOD.getDefaultState(), ZapAppleLogBlock.settings(MapColor.GRAY, MapColor.GRAY)), ItemGroups.BUILDING_BLOCKS);

    Block ZAP_LEAVES = register("zap_leaves", new ZapAppleLeavesBlock(ZapAppleLeavesBlock.settings()), ItemGroups.NATURAL);
    Block FLOWERING_ZAP_LEAVES = register("flowering_zap_leaves", new BaseZapAppleLeavesBlock(BaseZapAppleLeavesBlock.settings()), ItemGroups.NATURAL);
    Block ZAP_LEAVES_PLACEHOLDER = register("zap_leaves_placeholder", new ZapAppleLeavesPlaceholderBlock(Settings.create().replaceable().noCollision().dropsNothing().air()));
    Block ZAP_BULB = register("zap_bulb", new FruitBlock(Direction.DOWN, ZAP_LEAVES, FruitBlock.DEFAULT_SHAPE, false, Settings.create().mapColor(MapColor.GRAY).strength(500, 1200).sounds(BlockSoundGroup.AZALEA_LEAVES)));
    Block ZAP_APPLE = register("zap_apple", new FruitBlock(Direction.DOWN, ZAP_LEAVES, FruitBlock.DEFAULT_SHAPE, false, Settings.create().mapColor(MapColor.GRAY).sounds(BlockSoundGroup.AZALEA_LEAVES)));

    Block PALM_LOG = register("palm_log", BlockConstructionUtils.createLogBlock(MapColor.OFF_WHITE, MapColor.SPRUCE_BROWN), ItemGroups.BUILDING_BLOCKS);
    Block PALM_WOOD = register("palm_wood", BlockConstructionUtils.createWoodBlock(MapColor.OFF_WHITE), ItemGroups.BUILDING_BLOCKS);
    Block STRIPPED_PALM_LOG = register("stripped_palm_log", BlockConstructionUtils.createLogBlock(MapColor.OFF_WHITE, MapColor.OFF_WHITE), ItemGroups.BUILDING_BLOCKS);
    Block STRIPPED_PALM_WOOD = register("stripped_palm_wood", BlockConstructionUtils.createWoodBlock(MapColor.OFF_WHITE), ItemGroups.BUILDING_BLOCKS);

    Block PALM_PLANKS = register("palm_planks", new Block(Settings.create().mapColor(MapColor.OFF_WHITE).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_STAIRS = register("palm_stairs", new StairsBlock(PALM_PLANKS.getDefaultState(), Settings.copy(PALM_PLANKS).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_SLAB = register("palm_slab", new SlabBlock(Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_FENCE = register("palm_fence", new FenceBlock(Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_FENCE_GATE = register("palm_fence_gate", new FenceGateBlock(WoodType.OAK, Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_DOOR = register("palm_door", new DoorBlock(UWoodTypes.PALM.setType(), Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).instrument(Instrument.BASS).strength(3.0f).nonOpaque().burnable().pistonBehavior(PistonBehavior.DESTROY)), ItemGroups.FUNCTIONAL);
    Block PALM_TRAPDOOR = register("palm_trapdoor", new TrapdoorBlock(UWoodTypes.PALM.setType(), Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).instrument(Instrument.BASS).strength(3).nonOpaque().allowsSpawning(BlockConstructionUtils::never).burnable()), ItemGroups.FUNCTIONAL);
    Block PALM_PRESSURE_PLATE = register("palm_pressure_plate", new PressurePlateBlock(UWoodTypes.PALM.setType(), Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).noCollision().strength(0.5f).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_BUTTON = register("palm_button", BlockConstructionUtils.woodenButton(UWoodTypes.PALM.setType()), ItemGroups.BUILDING_BLOCKS);
    Block PALM_SIGN = register("palm_sign", new SignBlock(UWoodTypes.PALM, Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).solid().instrument(Instrument.BASS).noCollision().strength(1).burnable().sounds(BlockSoundGroup.WOOD)), ItemGroups.FUNCTIONAL);
    Block PALM_WALL_SIGN = register("palm_wall_sign", new WallSignBlock(UWoodTypes.PALM, Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).solid().instrument(Instrument.BASS).noCollision().strength(1).dropsLike(PALM_SIGN).burnable()));
    Block PALM_HANGING_SIGN = register("palm_hanging_sign", new HangingSignBlock(UWoodTypes.PALM, Settings.create().mapColor(PALM_LOG.getDefaultMapColor()).solid().instrument(Instrument.BASS).noCollision().strength(1).burnable()), ItemGroups.FUNCTIONAL);
    Block PALM_WALL_HANGING_SIGN = register("palm_wall_hanging_sign", new WallHangingSignBlock(UWoodTypes.PALM, Settings.create().mapColor(PALM_LOG.getDefaultMapColor()).solid().instrument(Instrument.BASS).noCollision().strength(1.0f).burnable().dropsLike(PALM_HANGING_SIGN)));

    Block PALM_LEAVES = register("palm_leaves", BlockConstructionUtils.createLeavesBlock(BlockSoundGroup.GRASS), ItemGroups.BUILDING_BLOCKS);
    Block BANANAS = register("bananas", new FruitBlock(Direction.DOWN, PALM_LEAVES, VoxelShapes.fullCube(), Settings.create().mapColor(MapColor.YELLOW).sounds(BlockSoundGroup.WOOD).noCollision().ticksRandomly().breakInstantly().pistonBehavior(PistonBehavior.DESTROY)));

    PineappleCropBlock PINEAPPLE = register("pineapple", new PineappleCropBlock(Settings.create().sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().pistonBehavior(PistonBehavior.DESTROY)));

    Block MANGO_LEAVES = register("mango_leaves", new FruitBearingBlock(
            0xCCFFAA00,
            () -> UBlocks.MANGO,
            () -> UItems.MANGO.getDefaultStack(),
            FabricBlockSettings.copy(Blocks.JUNGLE_LEAVES)
    ), ItemGroups.NATURAL);
    Block MANGO = register("mango", new FruitBlock(Direction.DOWN, MANGO_LEAVES, FruitBlock.DEFAULT_SHAPE, Settings.create().mapColor(MapColor.ORANGE)));

    Block WEATHER_VANE = register("weather_vane", new WeatherVaneBlock(Settings.create().mapColor(MapColor.BLACK).requiresTool().strength(3.0f, 6.0f).sounds(BlockSoundGroup.METAL).nonOpaque().pistonBehavior(PistonBehavior.BLOCK)), ItemGroups.TOOLS);

    Block GREEN_APPLE_LEAVES = register("green_apple_leaves", new FruitBearingBlock(
            0xE5FFFF88,
            () -> UBlocks.GREEN_APPLE,
            () -> UItems.GREEN_APPLE.getDefaultStack(),
            FabricBlockSettings.copy(Blocks.OAK_LEAVES)
    ), ItemGroups.NATURAL);
    Block GREEN_APPLE = register("green_apple", new FruitBlock(Direction.DOWN, GREEN_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE, Settings.create().mapColor(MapColor.GREEN)));
    Block GREEN_APPLE_SPROUT = register("green_apple_sprout", new SproutBlock(0xE5FFFF88, () -> UItems.GREEN_APPLE_SEEDS, () -> UTreeGen.GREEN_APPLE_TREE.sapling().map(Block::getDefaultState).get(), SproutBlock.settings()));

    Block SWEET_APPLE_LEAVES = register("sweet_apple_leaves", new FruitBearingBlock(
            0xE5FFCC88,
            () -> UBlocks.SWEET_APPLE,
            () -> UItems.SWEET_APPLE.getDefaultStack(),
            FabricBlockSettings.copy(Blocks.OAK_LEAVES)
    ), ItemGroups.NATURAL);
    Block SWEET_APPLE = register("sweet_apple", new FruitBlock(Direction.DOWN, SWEET_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE, Settings.create().mapColor(MapColor.GREEN)));
    Block SWEET_APPLE_SPROUT = register("sweet_apple_sprout", new SproutBlock(0xE5FFCC88, () -> UItems.SWEET_APPLE_SEEDS, () -> UTreeGen.SWEET_APPLE_TREE.sapling().map(Block::getDefaultState).get(), SproutBlock.settings()));

    Block SOUR_APPLE_LEAVES = register("sour_apple_leaves", new FruitBearingBlock(
            0xE5FFCCCC,
            () -> UBlocks.SOUR_APPLE,
            () -> UItems.SOUR_APPLE.getDefaultStack(),
            FabricBlockSettings.copy(Blocks.OAK_LEAVES)
    ), ItemGroups.NATURAL);
    Block SOUR_APPLE = register("sour_apple", new FruitBlock(Direction.DOWN, SOUR_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE, Settings.create().mapColor(MapColor.GREEN)));
    Block SOUR_APPLE_SPROUT = register("sour_apple_sprout", new SproutBlock(0xE5FFCC88, () -> UItems.SOUR_APPLE_SEEDS, () -> UTreeGen.SOUR_APPLE_TREE.sapling().map(Block::getDefaultState).get(), SproutBlock.settings()));

    Block APPLE_PIE = register("apple_pie", new PieBlock(
            () -> UItems.APPLE_PIE_SLICE,
            () -> UItems.APPLE_PIE,
            () -> UItems.APPLE_PIE_HOOF,
            Settings.create().solid().mapColor(MapColor.ORANGE).strength(0.5F).sounds(BlockSoundGroup.WOOL).pistonBehavior(PistonBehavior.DESTROY)
    ));

    SegmentedCropBlock OATS = register("oats", SegmentedCropBlock.create(11, 5, () -> UItems.OAT_SEEDS, null, null, AbstractBlock.Settings.copy(Blocks.WHEAT)));
    SegmentedCropBlock OATS_STEM = register("oats_stem", OATS.createNext(5));
    SegmentedCropBlock OATS_CROWN = register("oats_crown", OATS_STEM.createNext(5));

    Block PLUNDER_VINE = register("plunder_vine", new ThornBlock(() -> UBlocks.PLUNDER_VINE_BUD, Settings.create().mapColor(MapColor.DARK_CRIMSON).hardness(1).ticksRandomly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY)));
    Block PLUNDER_VINE_BUD = register("plunder_vine_bud", new ThornBudBlock(PLUNDER_VINE.getDefaultState(), Settings.create().mapColor(MapColor.DARK_CRIMSON).hardness(1).nonOpaque().ticksRandomly().sounds(BlockSoundGroup.GRASS).pistonBehavior(PistonBehavior.DESTROY)));

    Block CHITIN = register("chitin", new SnowyBlock(Settings.create().mapColor(MapColor.PALE_PURPLE).hardness(5).requiresTool().ticksRandomly().sounds(BlockSoundGroup.CORAL)), ItemGroups.NATURAL);
    Block SURFACE_CHITIN = register("surface_chitin", new GrowableBlock(() -> CHITIN, Settings.copy(CHITIN)), ItemGroups.NATURAL);
    Block CHISELLED_CHITIN = register("chiselled_chitin", new Block(Settings.create().mapColor(MapColor.PALE_PURPLE).hardness(5).requiresTool()), ItemGroups.BUILDING_BLOCKS);
    Block CHITIN_SPIKES = register("chitin_spikes", new SpikesBlock(Settings.copy(CHISELLED_CHITIN).noCollision().nonOpaque()), ItemGroups.NATURAL);
    Block CHISELLED_CHITIN_SLAB = register("chiselled_chitin_slab", new SlabBlock(Settings.copy(CHISELLED_CHITIN)), ItemGroups.BUILDING_BLOCKS);
    Block CHISELLED_CHITIN_STAIRS = register("chiselled_chitin_stairs", new StairsBlock(CHISELLED_CHITIN.getDefaultState(), Settings.copy(CHISELLED_CHITIN)), ItemGroups.BUILDING_BLOCKS);
    Block CHISELLED_CHITIN_HULL = register("chiselled_chitin_hull", new OrientedBlock(Settings.copy(CHISELLED_CHITIN)), ItemGroups.BUILDING_BLOCKS);
    Block HIVE = register("hive", new HiveBlock(Settings.create().mapColor(MapColor.PURPLE).hardness(6).ticksRandomly().sounds(BlockSoundGroup.CORAL)), ItemGroups.NATURAL);
    Block MYSTERIOUS_EGG = register("mysterious_egg", new PileBlock(Settings.copy(Blocks.SLIME_BLOCK), PileBlock.MYSTERIOUS_EGG_SHAPES), ItemGroups.NATURAL);
    Block SLIME_PUSTULE = register("slime_pustule", new SlimePustuleBlock(Settings.copy(Blocks.SLIME_BLOCK)), ItemGroups.NATURAL);

    Block SHAPING_BENCH = register("shaping_bench", new ShapingBenchBlock(Settings.create().mapColor(MapColor.OFF_WHITE).hardness(0.3F).resistance(0).sounds(BlockSoundGroup.WOOL)), ItemGroups.FUNCTIONAL);
    Block CLOUD = register("cloud", new NaturalCloudBlock(Settings.create().mapColor(MapColor.OFF_WHITE).hardness(0.3F).resistance(0).sounds(BlockSoundGroup.WOOL), true,
            () -> UBlocks.SOGGY_CLOUD,
            () -> UBlocks.COMPACTED_CLOUD), ItemGroups.NATURAL);
    Block COMPACTED_CLOUD = register("compacted_cloud", new CompactedCloudBlock(CLOUD.getDefaultState()));
    Block CLOUD_SLAB = register("cloud_slab", new CloudSlabBlock(Settings.copy(CLOUD), true, () -> UBlocks.SOGGY_CLOUD_SLAB), ItemGroups.NATURAL);
    PoreousCloudStairsBlock CLOUD_STAIRS = register("cloud_stairs", new PoreousCloudStairsBlock(CLOUD.getDefaultState(), Settings.copy(CLOUD), () -> UBlocks.SOGGY_CLOUD_STAIRS), ItemGroups.NATURAL);

    Block CLOUD_PLANKS = register("cloud_planks", new NaturalCloudBlock(Settings.copy(CLOUD).hardness(0.4F).requiresTool().solid(), false,
            null,
            () -> UBlocks.COMPACTED_CLOUD_PLANKS), ItemGroups.BUILDING_BLOCKS);
    Block COMPACTED_CLOUD_PLANKS = register("compacted_cloud_planks", new CompactedCloudBlock(CLOUD_PLANKS.getDefaultState()));
    Block CLOUD_PLANK_SLAB = register("cloud_plank_slab", new CloudSlabBlock(Settings.copy(CLOUD_PLANKS), false, null), ItemGroups.BUILDING_BLOCKS);
    Block CLOUD_PLANK_STAIRS = register("cloud_plank_stairs", new CloudStairsBlock(CLOUD_PLANKS.getDefaultState(), Settings.copy(CLOUD_PLANKS)), ItemGroups.BUILDING_BLOCKS);

    Block CLOUD_BRICKS = register("cloud_bricks", new NaturalCloudBlock(Settings.copy(CLOUD).hardness(0.6F).requiresTool().solid(), false,
            null,
            () -> UBlocks.COMPACTED_CLOUD_BRICKS), ItemGroups.BUILDING_BLOCKS);
    Block COMPACTED_CLOUD_BRICKS = register("compacted_cloud_bricks", new CompactedCloudBlock(CLOUD_BRICKS.getDefaultState()));
    Block CLOUD_BRICK_SLAB = register("cloud_brick_slab", new CloudSlabBlock(Settings.copy(CLOUD_BRICKS), false, null), ItemGroups.BUILDING_BLOCKS);
    Block CLOUD_BRICK_STAIRS = register("cloud_brick_stairs", new CloudStairsBlock(CLOUD_BRICKS.getDefaultState(), Settings.copy(CLOUD_PLANKS)), ItemGroups.BUILDING_BLOCKS);

    Block ETCHED_CLOUD = register("etched_cloud", new NaturalCloudBlock(Settings.copy(CLOUD_BRICKS), false,
            null,
            () -> UBlocks.COMPACTED_CLOUD_BRICKS), ItemGroups.BUILDING_BLOCKS);
    Block COMPACTED_ETCHED_CLOUD = register("compacted_etched_cloud", new CompactedCloudBlock(ETCHED_CLOUD.getDefaultState()));
    Block ETCHED_CLOUD_SLAB = register("etched_cloud_slab", new CloudSlabBlock(Settings.copy(ETCHED_CLOUD), false, null), ItemGroups.BUILDING_BLOCKS);
    Block ETCHED_CLOUD_STAIRS = register("etched_cloud_stairs", new CloudStairsBlock(ETCHED_CLOUD.getDefaultState(), Settings.copy(CLOUD_PLANKS)), ItemGroups.BUILDING_BLOCKS);

    SoggyCloudBlock SOGGY_CLOUD = register("soggy_cloud", new SoggyCloudBlock(Settings.copy(CLOUD).hardness(0.7F), () -> UBlocks.CLOUD));
    SoggyCloudSlabBlock SOGGY_CLOUD_SLAB = register("soggy_cloud_slab", new SoggyCloudSlabBlock(Settings.copy(SOGGY_CLOUD), () -> UBlocks.CLOUD_SLAB));
    SoggyCloudStairsBlock SOGGY_CLOUD_STAIRS = register("soggy_cloud_stairs", new SoggyCloudStairsBlock(SOGGY_CLOUD.getDefaultState(), Settings.copy(CLOUD), () -> UBlocks.CLOUD_STAIRS));

    Block DENSE_CLOUD = register("dense_cloud", new NaturalCloudBlock(Settings.create().mapColor(MapColor.GRAY).hardness(0.5F).resistance(0).sounds(BlockSoundGroup.WOOL).solid(), false,
            null,
            () -> UBlocks.COMPACTED_DENSE_CLOUD), ItemGroups.BUILDING_BLOCKS);
    Block COMPACTED_DENSE_CLOUD = register("compacted_dense_cloud", new CompactedCloudBlock(DENSE_CLOUD.getDefaultState()));
    Block DENSE_CLOUD_SLAB = register("dense_cloud_slab", new CloudSlabBlock(Settings.copy(DENSE_CLOUD), false, null), ItemGroups.BUILDING_BLOCKS);
    Block DENSE_CLOUD_STAIRS = register("dense_cloud_stairs", new CloudStairsBlock(DENSE_CLOUD.getDefaultState(), Settings.copy(DENSE_CLOUD)), ItemGroups.BUILDING_BLOCKS);

    Block CARVED_CLOUD = register("carved_cloud", new OrientedCloudBlock(Settings.copy(CLOUD).hardness(0.4F).requiresTool().solid(), false), ItemGroups.BUILDING_BLOCKS);
    Block UNSTABLE_CLOUD = register("unstable_cloud", new UnstableCloudBlock(Settings.copy(CLOUD)), ItemGroups.NATURAL);
    Block CLOUD_PILLAR = register("cloud_pillar", new CloudPillarBlock(Settings.create().mapColor(MapColor.GRAY).hardness(0.5F).resistance(0).sounds(BlockSoundGroup.WOOL).solid()), ItemGroups.NATURAL);
    Block CLOUD_CHEST = register("cloud_chest", new CloudChestBlock(Settings.copy(DENSE_CLOUD).instrument(Instrument.BASS).strength(2.5f), DENSE_CLOUD.getDefaultState()), ItemGroups.FUNCTIONAL);
    Block CLOTH_BED = register("cloth_bed", new FancyBedBlock("cloth", Settings.copy(Blocks.WHITE_BED).sounds(BlockSoundGroup.WOOD)));
    Block CLOUD_BED = register("cloud_bed", new CloudBedBlock("cloud", CLOUD.getDefaultState(), Settings.copy(Blocks.WHITE_BED).sounds(BlockSoundGroup.WOOL)));

    Block CLAM_SHELL = register("clam_shell", new ShellsBlock(Settings.create().mapColor(MapColor.DULL_PINK).breakInstantly().nonOpaque()));
    Block SCALLOP_SHELL = register("scallop_shell", new ShellsBlock(Settings.create().mapColor(MapColor.DULL_PINK).breakInstantly().nonOpaque()));
    Block TURRET_SHELL = register("turret_shell", new ShellsBlock(Settings.create().mapColor(MapColor.DULL_PINK).breakInstantly().nonOpaque()));

    Block STABLE_DOOR = register("stable_door", new StableDoorBlock(BlockSetType.OAK, Settings.copy(Blocks.OAK_DOOR)), ItemGroups.FUNCTIONAL);
    Block DARK_OAK_DOOR = register("dark_oak_stable_door", new StableDoorBlock(BlockSetType.OAK, Settings.copy(Blocks.OAK_DOOR)), ItemGroups.FUNCTIONAL);
    Block CRYSTAL_DOOR = register("crystal_door", new CrystalDoorBlock(UWoodTypes.CRYSTAL, Settings.copy(Blocks.IRON_DOOR)), ItemGroups.FUNCTIONAL);
    Block CLOUD_DOOR = register("cloud_door", new CloudDoorBlock(Settings.copy(CLOUD), CLOUD.getDefaultState(), UWoodTypes.CLOUD), ItemGroups.FUNCTIONAL);

    private static <T extends Block> T register(String name, T item) {
        return register(Unicopia.id(name), item);
    }

    private static <T extends Block> T register(String name, T block, RegistryKey<ItemGroup> group) {
        return register(Unicopia.id(name), block, group);
    }

    static <T extends Block> T register(Identifier id, T block, RegistryKey<ItemGroup> group) {
        ItemGroupRegistry.register(id, block instanceof CloudLike ? new CloudBlockItem(block, new Item.Settings()) : new BlockItem(block, new Item.Settings()), group);
        return register(id, block);
    }

    private static <T extends Block> T register(Identifier id, T block) {
        if (block instanceof TintedBlock) {
            TintedBlock.REGISTRY.add(block);
        }
        if (block instanceof SaplingBlock || block instanceof SproutBlock || block instanceof FruitBlock || block instanceof CropBlock || block instanceof DoorBlock || block instanceof TrapdoorBlock) {
            TRANSLUCENT_BLOCKS.add(block);
        }
        if (block instanceof CloudLike || block instanceof SlimePustuleBlock || block instanceof PileBlock) {
            SEMI_TRANSPARENT_BLOCKS.add(block);
        }
        return Registry.register(Registries.BLOCK, id, block);
    }

    static void bootstrap() {
        BlockEntityTypeSupportHelper.of(BlockEntityType.SIGN).addSupportedBlocks(PALM_SIGN, PALM_WALL_SIGN);
        BlockEntityTypeSupportHelper.of(BlockEntityType.HANGING_SIGN).addSupportedBlocks(PALM_HANGING_SIGN, PALM_WALL_HANGING_SIGN);

        StrippableBlockRegistry.register(ZAP_LOG, STRIPPED_ZAP_LOG);
        StrippableBlockRegistry.register(PALM_LOG, STRIPPED_PALM_LOG);
        StrippableBlockRegistry.register(ZAP_WOOD, STRIPPED_ZAP_WOOD);
        StrippableBlockRegistry.register(PALM_WOOD, STRIPPED_PALM_WOOD);
        Collections.addAll(TRANSLUCENT_BLOCKS, WEATHER_VANE, CHITIN_SPIKES, PLUNDER_VINE, PLUNDER_VINE_BUD, CLAM_SHELL, SCALLOP_SHELL, TURRET_SHELL);
        TintedBlock.REGISTRY.add(PALM_LEAVES);

        FlammableBlockRegistry.getDefaultInstance().add(GREEN_APPLE_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(SWEET_APPLE_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(SOUR_APPLE_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(MANGO_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_LOG, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_WOOD, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(STRIPPED_PALM_LOG, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(STRIPPED_PALM_WOOD, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_PLANKS, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(BANANAS, 5, 20);

        UBlockEntities.bootstrap();
    }
}

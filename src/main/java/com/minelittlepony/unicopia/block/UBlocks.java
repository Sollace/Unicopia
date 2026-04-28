package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

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
import com.minelittlepony.unicopia.block.zap.BaseZapAppleLeavesBlock;
import com.minelittlepony.unicopia.block.zap.ElectrifiedFenceBlock;
import com.minelittlepony.unicopia.block.zap.ElectrifiedFenceGateBlock;
import com.minelittlepony.unicopia.block.zap.ZapAppleLeavesBlock;
import com.minelittlepony.unicopia.block.zap.ZapAppleLeavesPlaceholderBlock;
import com.minelittlepony.unicopia.block.zap.ZapAppleLogBlock;
import com.minelittlepony.unicopia.block.zap.ZapBlock;
import com.minelittlepony.unicopia.block.zap.ZapSlabBlock;
import com.minelittlepony.unicopia.block.zap.ZapStairsBlock;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.cloud.CloudBlockItem;
import com.minelittlepony.unicopia.item.group.ItemGroupRegistry;
import com.minelittlepony.unicopia.server.world.UTreeGen;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ColorCode;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;

public interface UBlocks {
    List<Block> TRANSLUCENT_BLOCKS = new ArrayList<>();
    List<Block> SEMI_TRANSPARENT_BLOCKS = new ArrayList<>();

    @SuppressWarnings("deprecation")
    Block ROCKS = register("rocks", s -> new RockCropBlock(s
            .notSolid()
            .mapColor(MapColor.STONE_GRAY)
            .nonOpaque()
            .pistonBehavior(PistonBehavior.DESTROY)
            .requiresTool()
            .ticksRandomly()
            .strength(2)
            .sounds(BlockSoundGroup.STONE)));

    Block FROSTED_OBSIDIAN = register("frosted_obsidian", Settings.copy(Blocks.OBSIDIAN), s -> new FrostedObsidianBlock(s.ticksRandomly()));

    Block ZAP_LOG = register("zap_log", ZapAppleLogBlock.settings(MapColor.GRAY, MapColor.DEEPSLATE_GRAY), s -> new ZapAppleLogBlock(Blocks.OAK_LOG.getDefaultState(), s), ItemGroups.BUILDING_BLOCKS);
    Block ZAP_WOOD = register("zap_wood", ZapAppleLogBlock.settings(MapColor.DEEPSLATE_GRAY, MapColor.DEEPSLATE_GRAY), s -> new ZapAppleLogBlock(Blocks.OAK_WOOD.getDefaultState(), s), ItemGroups.BUILDING_BLOCKS);

    Block STRIPPED_ZAP_LOG = register("stripped_zap_log", ZapAppleLogBlock.settings(MapColor.LIGHT_GRAY, MapColor.GRAY), s -> new ZapAppleLogBlock(Blocks.STRIPPED_OAK_LOG.getDefaultState(), s), ItemGroups.BUILDING_BLOCKS);
    Block STRIPPED_ZAP_WOOD = register("stripped_zap_wood", ZapAppleLogBlock.settings(MapColor.GRAY, MapColor.GRAY), s -> new ZapAppleLogBlock(Blocks.STRIPPED_OAK_WOOD.getDefaultState(), s), ItemGroups.BUILDING_BLOCKS);

    Block WAXED_ZAP_LOG = register("waxed_zap_log", BlockConstructionUtils.createLogBlock(UMapColors.ZAP_LOG_END, UMapColors.ZAP_LOG_SIDE), ItemGroups.BUILDING_BLOCKS);
    Block WAXED_ZAP_WOOD = register("waxed_zap_wood", BlockConstructionUtils.createLogBlock(UMapColors.ZAP_LOG_SIDE, UMapColors.ZAP_LOG_SIDE), ItemGroups.BUILDING_BLOCKS);
    Block WAXED_STRIPPED_ZAP_LOG = register("waxed_stripped_zap_log", BlockConstructionUtils.createLogBlock(UMapColors.ZAP_LOG_END, UMapColors.STRIPPED_ZAP_LOG_SIDE), ItemGroups.BUILDING_BLOCKS);
    Block WAXED_STRIPPED_ZAP_WOOD = register("waxed_stripped_zap_wood", BlockConstructionUtils.createLogBlock(UMapColors.STRIPPED_ZAP_LOG_SIDE, UMapColors.STRIPPED_ZAP_LOG_SIDE), ItemGroups.BUILDING_BLOCKS);

    Block ZAP_PLANKS = register("zap_planks", s -> new ZapBlock(s.mapColor(UMapColors.ZAP_PLANKS).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block ZAP_STAIRS = register("zap_stairs", Settings.copy(ZAP_PLANKS), s -> new ZapStairsBlock(ZAP_PLANKS.getDefaultState(), s.pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block ZAP_SLAB = register("zap_slab", s -> new ZapSlabBlock(s.mapColor(ZAP_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block ZAP_FENCE = register("zap_fence", s -> new ElectrifiedFenceBlock(s.mapColor(ZAP_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block ZAP_FENCE_GATE = register("zap_fence_gate", s -> new ElectrifiedFenceGateBlock(UWoodTypes.ZAP, s.mapColor(ZAP_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);

    Block WAXED_ZAP_PLANKS = register("waxed_zap_planks", s -> new Block(s.mapColor(UMapColors.ZAP_PLANKS).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block WAXED_ZAP_STAIRS = register("waxed_zap_stairs", Settings.copy(WAXED_ZAP_PLANKS), s -> new StairsBlock(WAXED_ZAP_PLANKS.getDefaultState(), s.pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block WAXED_ZAP_SLAB = register("waxed_zap_slab", s -> new SlabBlock(s.mapColor(WAXED_ZAP_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block WAXED_ZAP_FENCE = register("waxed_zap_fence", s -> new FenceBlock(s.mapColor(WAXED_ZAP_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block WAXED_ZAP_FENCE_GATE = register("waxed_zap_fence_gate", s -> new FenceGateBlock(UWoodTypes.ZAP, s.mapColor(WAXED_ZAP_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);

    Block ZAP_LEAVES = register("zap_leaves", ZapAppleLeavesBlock.settings(), ZapAppleLeavesBlock::new, ItemGroups.NATURAL);
    Block FLOWERING_ZAP_LEAVES = register("flowering_zap_leaves", BaseZapAppleLeavesBlock.settings(), BaseZapAppleLeavesBlock::new, ItemGroups.NATURAL);
    Block ZAP_LEAVES_PLACEHOLDER = register("zap_leaves_placeholder", s -> new ZapAppleLeavesPlaceholderBlock(s.replaceable().noCollision().dropsNothing().air()));
    Block ZAP_BULB = register("zap_bulb", s -> new FruitBlock(Direction.DOWN, ZAP_LEAVES, FruitBlock.DEFAULT_SHAPE, false, s.mapColor(MapColor.GRAY).strength(500, 1200).sounds(BlockSoundGroup.AZALEA_LEAVES)));
    Block ZAP_APPLE = register("zap_apple", s -> new FruitBlock(Direction.DOWN, ZAP_LEAVES, FruitBlock.DEFAULT_SHAPE, false, s.mapColor(MapColor.GRAY).sounds(BlockSoundGroup.AZALEA_LEAVES)));

    Block PALM_LOG = register("palm_log", BlockConstructionUtils.createLogBlock(UMapColors.PALM_LOG_END, UMapColors.PALM_LOG_SIDE), ItemGroups.BUILDING_BLOCKS);
    Block PALM_WOOD = register("palm_wood", BlockConstructionUtils.createWoodBlock(UMapColors.PALM_LOG_SIDE), ItemGroups.BUILDING_BLOCKS);
    Block STRIPPED_PALM_LOG = register("stripped_palm_log", BlockConstructionUtils.createLogBlock(UMapColors.PALM_LOG_END, UMapColors.STRIPPED_PALM_LOG_SIDE), ItemGroups.BUILDING_BLOCKS);
    Block STRIPPED_PALM_WOOD = register("stripped_palm_wood", BlockConstructionUtils.createWoodBlock(UMapColors.STRIPPED_PALM_LOG_SIDE), ItemGroups.BUILDING_BLOCKS);

    Block PALM_PLANKS = register("palm_planks", s -> new Block(s.mapColor(UMapColors.PALM_PLANKS).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_STAIRS = register("palm_stairs", Settings.copy(PALM_PLANKS), s -> new StairsBlock(PALM_PLANKS.getDefaultState(), s.pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_SLAB = register("palm_slab", s -> new SlabBlock(s.mapColor(PALM_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_FENCE = register("palm_fence", s -> new FenceBlock(s.mapColor(PALM_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_FENCE_GATE = register("palm_fence_gate", s -> new FenceGateBlock(UWoodTypes.PALM, s.mapColor(PALM_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_DOOR = register("palm_door", s -> new DoorBlock(UWoodTypes.PALM.setType(), s.mapColor(PALM_PLANKS.getDefaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(3.0f).nonOpaque().burnable().pistonBehavior(PistonBehavior.DESTROY)), ItemGroups.FUNCTIONAL);
    Block PALM_TRAPDOOR = register("palm_trapdoor", s -> new TrapdoorBlock(UWoodTypes.PALM.setType(), s.mapColor(PALM_PLANKS.getDefaultMapColor()).instrument(NoteBlockInstrument.BASS).strength(3).nonOpaque().allowsSpawning(BlockConstructionUtils::never).burnable()), ItemGroups.FUNCTIONAL);
    Block PALM_PRESSURE_PLATE = register("palm_pressure_plate", s -> new PressurePlateBlock(UWoodTypes.PALM.setType(), s.mapColor(PALM_PLANKS.getDefaultMapColor()).noCollision().strength(0.5f).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_BUTTON = register("palm_button", BlockConstructionUtils.woodenButton(UWoodTypes.PALM.setType()), ItemGroups.BUILDING_BLOCKS);
    Block PALM_SIGN = register("palm_sign", s -> new SignBlock(UWoodTypes.PALM, s.mapColor(PALM_PLANKS.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1).burnable().sounds(BlockSoundGroup.WOOD)));
    Block PALM_WALL_SIGN = register("palm_wall_sign", s -> new WallSignBlock(UWoodTypes.PALM, s.mapColor(PALM_PLANKS.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1).lootTable(PALM_SIGN.getLootTableKey()).burnable()));
    Block PALM_HANGING_SIGN = register("palm_hanging_sign", s -> new HangingSignBlock(UWoodTypes.PALM, s.mapColor(PALM_LOG.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1).burnable()));
    Block PALM_WALL_HANGING_SIGN = register("palm_wall_hanging_sign", s -> new WallHangingSignBlock(UWoodTypes.PALM, s.mapColor(PALM_LOG.getDefaultMapColor()).solid().instrument(NoteBlockInstrument.BASS).noCollision().strength(1.0f).burnable().lootTable(PALM_HANGING_SIGN.getLootTableKey())));

    Block PALM_LEAVES = register("palm_leaves", BlockConstructionUtils.createLeavesBlock(BlockSoundGroup.GRASS), ItemGroups.BUILDING_BLOCKS);
    Block BANANAS = register("bananas", s -> new FruitBlock(Direction.DOWN, PALM_LEAVES, VoxelShapes.fullCube(), s.mapColor(MapColor.YELLOW).sounds(BlockSoundGroup.WOOD).noCollision().ticksRandomly().breakInstantly().pistonBehavior(PistonBehavior.DESTROY)));

    PineappleCropBlock PINEAPPLE = register("pineapple", s -> new PineappleCropBlock(s.sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().pistonBehavior(PistonBehavior.DESTROY)));

    Block MANGO_LEAVES = register("mango_leaves", Settings.copy(Blocks.JUNGLE_LEAVES), s -> new FruitBearingBlock(
            0xCCFFAA00,
            () -> UBlocks.MANGO,
            () -> UItems.MANGO.getDefaultStack(),
            s
    ), ItemGroups.NATURAL);
    Block MANGO = register("mango", s -> new FruitBlock(Direction.DOWN, MANGO_LEAVES, FruitBlock.DEFAULT_SHAPE, s.mapColor(MapColor.ORANGE)));

    Block WEATHER_VANE = register("weather_vane", s -> new WeatherVaneBlock(s.mapColor(MapColor.BLACK).requiresTool().strength(3.0f, 6.0f).sounds(BlockSoundGroup.METAL).nonOpaque().pistonBehavior(PistonBehavior.BLOCK)), ItemGroups.TOOLS);

    Block GREEN_APPLE_LEAVES = register("green_apple_leaves", Settings.copy(Blocks.OAK_LEAVES), s -> new FruitBearingBlock(
            0xE5FFFF88,
            () -> UBlocks.GREEN_APPLE,
            () -> UItems.GREEN_APPLE.getDefaultStack(),
            s
    ), ItemGroups.NATURAL);
    Block GREEN_APPLE = register("green_apple", s -> new FruitBlock(Direction.DOWN, GREEN_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE, s.mapColor(MapColor.GREEN)));
    Block GREEN_APPLE_SPROUT = register("green_apple_sprout", SproutBlock.settings(), s -> new SproutBlock(0xE5FFFF88, () -> UItems.GREEN_APPLE_SEEDS, () -> UTreeGen.GREEN_APPLE_TREE.sapling().map(Block::getDefaultState).get(), s));

    Block SWEET_APPLE_LEAVES = register("sweet_apple_leaves", Settings.copy(Blocks.OAK_LEAVES), s -> new FruitBearingBlock(
            0xE5FFCC88,
            () -> UBlocks.SWEET_APPLE,
            () -> UItems.SWEET_APPLE.getDefaultStack(),
            s
    ), ItemGroups.NATURAL);
    Block SWEET_APPLE = register("sweet_apple", s -> new FruitBlock(Direction.DOWN, SWEET_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE, s.mapColor(MapColor.GREEN)));
    Block SWEET_APPLE_SPROUT = register("sweet_apple_sprout", SproutBlock.settings(), s -> new SproutBlock(0xE5FFCC88, () -> UItems.SWEET_APPLE_SEEDS, () -> UTreeGen.SWEET_APPLE_TREE.sapling().map(Block::getDefaultState).get(), s));

    Block SOUR_APPLE_LEAVES = register("sour_apple_leaves", Settings.copy(Blocks.OAK_LEAVES), s -> new FruitBearingBlock(
            0xE5FFCCCC,
            () -> UBlocks.SOUR_APPLE,
            () -> UItems.SOUR_APPLE.getDefaultStack(),
            s
    ), ItemGroups.NATURAL);
    Block SOUR_APPLE = register("sour_apple", s -> new FruitBlock(Direction.DOWN, SOUR_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE, s.mapColor(MapColor.GREEN)));
    Block SOUR_APPLE_SPROUT = register("sour_apple_sprout", SproutBlock.settings(), s -> new SproutBlock(0xE5FFCC88, () -> UItems.SOUR_APPLE_SEEDS, () -> UTreeGen.SOUR_APPLE_TREE.sapling().map(Block::getDefaultState).get(), s));

    Block APPLE_PIE = register("apple_pie", s -> new PieBlock(
            () -> UItems.APPLE_PIE_SLICE,
            () -> UItems.APPLE_PIE,
            () -> UItems.APPLE_PIE_HOOF,
            s.solid().mapColor(MapColor.ORANGE).strength(0.5F).sounds(BlockSoundGroup.WOOL).pistonBehavior(PistonBehavior.DESTROY)
    ));
    Block GOLDEN_OAK_LEAVES = register("golden_oak_leaves", Settings.copy(Blocks.OAK_LEAVES), s -> new GoldenOakLeavesBlock(
            MapColor.GOLD.color,
            () -> UBlocks.GOLDEN_APPLE,
            () -> Items.GOLDEN_APPLE.getDefaultStack(),
            s
    ), ItemGroups.NATURAL);
    Block GOLDEN_APPLE = register("golden_apple", s -> new EnchantedFruitBlock(Direction.DOWN, GOLDEN_OAK_LEAVES, FruitBlock.DEFAULT_SHAPE, false, s.mapColor(MapColor.GOLD)));
    Block GOLDEN_OAK_SPROUT = register("golden_oak_sprout", SproutBlock.settings(), s -> new SproutBlock(0xE5FFCC88, () -> UItems.GOLDEN_OAK_SEEDS, () -> UTreeGen.GOLDEN_OAK_TREE.sapling().map(Block::getDefaultState).get(), s));
    StrippablePillarBlock GOLDEN_OAK_LOG = register("golden_oak_log", BlockConstructionUtils.createMetallicLogBlock(MapColor.OFF_WHITE, MapColor.GOLD), ItemGroups.BUILDING_BLOCKS);
    StrippablePillarBlock GOLDEN_OAK_WOOD = register("golden_oak_wood", BlockConstructionUtils.createMetallicWoodBlock(MapColor.OFF_WHITE), ItemGroups.BUILDING_BLOCKS);
    Block GOLDEN_OAK_PLANKS = register("golden_oak_planks", s -> new Block(s.mapColor(MapColor.GOLD).strength(3, 4).sounds(BlockSoundGroup.METAL).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block GOLDEN_OAK_SLAB = register("golden_oak_slab", s -> new SlabBlock(s.mapColor(GOLDEN_OAK_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.METAL).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block GOLDEN_OAK_STAIRS = register("golden_oak_stairs", Settings.copy(GOLDEN_OAK_PLANKS), s -> new StairsBlock(GOLDEN_OAK_PLANKS.getDefaultState(), s), ItemGroups.BUILDING_BLOCKS);

    StrippablePillarBlock STRIPPED_GOLDEN_OAK_LOG = register("stripped_golden_oak_log", BlockConstructionUtils.createMetallicLogBlock(MapColor.OFF_WHITE, MapColor.GOLD), ItemGroups.BUILDING_BLOCKS);
    StrippablePillarBlock STRIPPED_GOLDEN_OAK_WOOD = register("stripped_golden_oak_wood", BlockConstructionUtils.createMetallicWoodBlock(MapColor.OFF_WHITE), ItemGroups.BUILDING_BLOCKS);

    SegmentedCropBlock OATS = register("oats", Settings.copy(Blocks.WHEAT), s -> SegmentedCropBlock.create(11, 5, () -> UItems.OAT_SEEDS, null, null, s));
    SegmentedCropBlock OATS_STEM = register("oats_stem", Settings.copy(Blocks.WHEAT), OATS.createNext(5));
    SegmentedCropBlock OATS_CROWN = register("oats_crown", Settings.copy(Blocks.WHEAT), OATS_STEM.createNext(5));

    Block PLUNDER_VINE = register("plunder_vine", s -> new ThornBlock(() -> UBlocks.PLUNDER_VINE_BUD, s.mapColor(MapColor.DARK_CRIMSON).hardness(1).ticksRandomly().sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY)));
    Block PLUNDER_VINE_BUD = register("plunder_vine_bud", s -> new ThornBudBlock(PLUNDER_VINE.getDefaultState(), s.mapColor(MapColor.DARK_CRIMSON).hardness(1).nonOpaque().ticksRandomly().noCollision().sounds(BlockSoundGroup.GRASS).pistonBehavior(PistonBehavior.DESTROY)));
    CuringJokeBlock CURING_JOKE = register("curing_joke", s -> new CuringJokeBlock(UEffects.BUTTER_FINGERS, 7, s.mapColor(MapColor.PALE_PURPLE).noCollision().breakInstantly().sounds(BlockSoundGroup.GRASS).offset(AbstractBlock.OffsetType.XZ).pistonBehavior(PistonBehavior.DESTROY)));
    Block GOLD_ROOT = register("gold_root", s -> new CarrotsBlock(s.mapColor(MapColor.GOLD).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP).pistonBehavior(PistonBehavior.DESTROY)) {
        @Override
        protected ItemConvertible getSeedsItem() {
            return Items.GOLDEN_CARROT;
        }
    });

    Block CHITIN = register("chitin", s -> new SnowyBlock(s.mapColor(MapColor.PALE_PURPLE).hardness(5).requiresTool().ticksRandomly().sounds(BlockSoundGroup.CORAL)), ItemGroups.NATURAL);
    Block SURFACE_CHITIN = register("surface_chitin", Settings.copy(CHITIN), s -> new GrowableBlock(() -> CHITIN, s), ItemGroups.NATURAL);
    Block CHISELLED_CHITIN = register("chiselled_chitin", s -> new Block(s.mapColor(MapColor.PALE_PURPLE).hardness(5).requiresTool()), ItemGroups.BUILDING_BLOCKS);
    Block CHITIN_SPIKES = register("chitin_spikes", Settings.copy(CHISELLED_CHITIN), s -> new SpikesBlock(s.noCollision().nonOpaque()), ItemGroups.NATURAL);
    Block CHISELLED_CHITIN_SLAB = register("chiselled_chitin_slab", Settings.copy(CHISELLED_CHITIN), SlabBlock::new, ItemGroups.BUILDING_BLOCKS);
    Block CHISELLED_CHITIN_STAIRS = register("chiselled_chitin_stairs", Settings.copy(CHISELLED_CHITIN), s -> new StairsBlock(CHISELLED_CHITIN.getDefaultState(), s), ItemGroups.BUILDING_BLOCKS);
    Block CHISELLED_CHITIN_HULL = register("chiselled_chitin_hull", Settings.copy(CHISELLED_CHITIN), OrientedBlock::new, ItemGroups.BUILDING_BLOCKS);
    Block CHISELLED_CHITIN_WALL = register("chiselled_chitin_wall", Settings.copy(CHISELLED_CHITIN), WallBlock::new, ItemGroups.BUILDING_BLOCKS);
    Block POLISHED_CHITIN = register("polished_chitin", Settings.copy(CHISELLED_CHITIN), Block::new, ItemGroups.BUILDING_BLOCKS);
    Block POLISHED_CHITIN_SLAB = register("polished_chitin_slab", Settings.copy(POLISHED_CHITIN), SlabBlock::new, ItemGroups.BUILDING_BLOCKS);
    Block POLISHED_CHITIN_STAIRS = register("polished_chitin_stairs", Settings.copy(POLISHED_CHITIN), s -> new StairsBlock(POLISHED_CHITIN.getDefaultState(), s), ItemGroups.BUILDING_BLOCKS);
    Block POLISHED_CHITIN_HULL = register("polished_chitin_hull", Settings.copy(POLISHED_CHITIN), OrientedBlock::new, ItemGroups.BUILDING_BLOCKS);
    Block POLISHED_CHITIN_WALL = register("polished_chitin_wall", Settings.copy(POLISHED_CHITIN), WallBlock::new, ItemGroups.BUILDING_BLOCKS);
    Block HIVE = register("hive", s -> new HiveBlock(s.mapColor(MapColor.PURPLE).hardness(6).ticksRandomly().sounds(BlockSoundGroup.CORAL)), ItemGroups.NATURAL);
    Block MYSTERIOUS_EGG = register("mysterious_egg", Settings.copy(Blocks.SLIME_BLOCK), s -> new PileBlock(s, PileBlock.MYSTERIOUS_EGG_SHAPES), ItemGroups.NATURAL);
    Block SLIME_PUSTULE = register("slime_pustule", Settings.copy(Blocks.SLIME_BLOCK), SlimePustuleBlock::new, ItemGroups.NATURAL);
    Block SLIME = register("slime", s -> new SlimeLayerBlock(s
            .mapColor(MapColor.PALE_GREEN)
            .replaceable()
            .nonOpaque()
            .ticksRandomly()
            .slipperiness(0.8F)
            .strength(0.1F)
            .requiresTool()
            .sounds(BlockSoundGroup.SLIME)
            .blockVision((state, world, pos) -> state.get(SlimeLayerBlock.LAYERS) >= 8)
            .pistonBehavior(PistonBehavior.DESTROY)
    ), ItemGroups.NATURAL);

    Block SHAPING_BENCH = register("shaping_bench", s -> new ShapingBenchBlock(s.mapColor(MapColor.OFF_WHITE).hardness(0.3F).resistance(0).sounds(BlockSoundGroup.WOOL)), ItemGroups.FUNCTIONAL);
    @SuppressWarnings("deprecation")
    Block CLOUD = register("cloud", s -> new NaturalCloudBlock(true,
            () -> UBlocks.SOGGY_CLOUD,
            () -> UBlocks.COMPACTED_CLOUD,
            s.notSolid().mapColor(MapColor.OFF_WHITE).hardness(0.3F).resistance(0).sounds(BlockSoundGroup.WOOL)), ItemGroups.NATURAL);
    Block COMPACTED_CLOUD = register("compacted_cloud", Settings.copy(CLOUD), s -> new CompactedCloudBlock(CLOUD.getDefaultState(), s));
    Block CLOUD_SLAB = register("cloud_slab", Settings.copy(CLOUD), s -> new CloudSlabBlock(true, () -> UBlocks.SOGGY_CLOUD_SLAB, s), ItemGroups.NATURAL);
    PoreousCloudStairsBlock CLOUD_STAIRS = register("cloud_stairs", Settings.copy(CLOUD), s -> new PoreousCloudStairsBlock(CLOUD.getDefaultState(), () -> UBlocks.SOGGY_CLOUD_STAIRS, s), ItemGroups.NATURAL);

    Block CLOUD_PLANKS = register("cloud_planks", Settings.copy(CLOUD), s -> new NaturalCloudBlock(false, null, () -> UBlocks.COMPACTED_CLOUD_PLANKS, s.hardness(0.4F).requiresTool().solid()), ItemGroups.BUILDING_BLOCKS);
    Block COMPACTED_CLOUD_PLANKS = register("compacted_cloud_planks", Settings.copy(CLOUD_PLANKS), s -> new CompactedCloudBlock(CLOUD_PLANKS.getDefaultState(), s));
    Block CLOUD_PLANK_SLAB = register("cloud_plank_slab", Settings.copy(CLOUD_PLANKS), s -> new CloudSlabBlock(false, null, s), ItemGroups.BUILDING_BLOCKS);
    Block CLOUD_PLANK_STAIRS = register("cloud_plank_stairs", Settings.copy(CLOUD_PLANKS), s -> new CloudStairsBlock(CLOUD_PLANKS.getDefaultState(), s), ItemGroups.BUILDING_BLOCKS);

    Block CLOUD_BRICKS = register("cloud_bricks", Settings.copy(CLOUD), s -> new NaturalCloudBlock(false, null, () -> UBlocks.COMPACTED_CLOUD_BRICKS, s.hardness(0.6F).requiresTool().solid()), ItemGroups.BUILDING_BLOCKS);
    Block COMPACTED_CLOUD_BRICKS = register("compacted_cloud_bricks", Settings.copy(CLOUD_BRICKS), s -> new CompactedCloudBlock(CLOUD_BRICKS.getDefaultState(), s));
    Block CLOUD_BRICK_SLAB = register("cloud_brick_slab", Settings.copy(CLOUD_BRICKS), s -> new CloudSlabBlock(false, null, s), ItemGroups.BUILDING_BLOCKS);
    Block CLOUD_BRICK_STAIRS = register("cloud_brick_stairs", Settings.copy(CLOUD_BRICKS), s -> new CloudStairsBlock(CLOUD_BRICKS.getDefaultState(), s), ItemGroups.BUILDING_BLOCKS);

    Block ETCHED_CLOUD = register("etched_cloud", Settings.copy(CLOUD_BRICKS), s -> new NaturalCloudBlock(false, null, () -> UBlocks.COMPACTED_CLOUD_BRICKS, s), ItemGroups.BUILDING_BLOCKS);
    Block COMPACTED_ETCHED_CLOUD = register("compacted_etched_cloud", Settings.copy(ETCHED_CLOUD), s -> new CompactedCloudBlock(ETCHED_CLOUD.getDefaultState(), s));
    Block ETCHED_CLOUD_SLAB = register("etched_cloud_slab", Settings.copy(ETCHED_CLOUD), s -> new CloudSlabBlock(false, null, s), ItemGroups.BUILDING_BLOCKS);
    Block ETCHED_CLOUD_STAIRS = register("etched_cloud_stairs", Settings.copy(ETCHED_CLOUD), s -> new CloudStairsBlock(ETCHED_CLOUD.getDefaultState(), s), ItemGroups.BUILDING_BLOCKS);

    SoggyCloudBlock SOGGY_CLOUD = register("soggy_cloud", Settings.copy(CLOUD), s -> new SoggyCloudBlock(() -> UBlocks.CLOUD.getDefaultState(), s.hardness(0.7F)));
    SoggyCloudSlabBlock SOGGY_CLOUD_SLAB = register("soggy_cloud_slab", Settings.copy(SOGGY_CLOUD), s -> new SoggyCloudSlabBlock(() -> UBlocks.CLOUD_SLAB.getDefaultState(), s));
    SoggyCloudStairsBlock SOGGY_CLOUD_STAIRS = register("soggy_cloud_stairs", Settings.copy(CLOUD), s -> new SoggyCloudStairsBlock(SOGGY_CLOUD.getDefaultState(), () -> UBlocks.CLOUD_STAIRS.getDefaultState(), s));

    Block DENSE_CLOUD = register("dense_cloud", s -> new NaturalCloudBlock(false, null, () -> UBlocks.COMPACTED_DENSE_CLOUD, s.mapColor(MapColor.GRAY).hardness(0.5F).resistance(0).sounds(BlockSoundGroup.WOOL).solid()), ItemGroups.BUILDING_BLOCKS);
    Block COMPACTED_DENSE_CLOUD = register("compacted_dense_cloud", Settings.copy(DENSE_CLOUD), s -> new CompactedCloudBlock(DENSE_CLOUD.getDefaultState(), s));
    Block DENSE_CLOUD_SLAB = register("dense_cloud_slab", Settings.copy(DENSE_CLOUD), s -> new CloudSlabBlock(false, null, s), ItemGroups.BUILDING_BLOCKS);
    Block DENSE_CLOUD_STAIRS = register("dense_cloud_stairs", Settings.copy(DENSE_CLOUD), s -> new CloudStairsBlock(DENSE_CLOUD.getDefaultState(), s), ItemGroups.BUILDING_BLOCKS);

    Block CARVED_CLOUD = register("carved_cloud", Settings.copy(CLOUD), s -> new OrientedCloudBlock(false, s.hardness(0.4F).requiresTool().solid()), ItemGroups.BUILDING_BLOCKS);
    Block UNSTABLE_CLOUD = register("unstable_cloud", Settings.copy(CLOUD), UnstableCloudBlock::new, ItemGroups.NATURAL);
    Block CLOUD_PILLAR = register("cloud_pillar", Settings.create(), s -> new CloudPillarBlock(s.mapColor(MapColor.GRAY).hardness(0.5F).resistance(0).sounds(BlockSoundGroup.WOOL).solid()), ItemGroups.NATURAL);
    Block CLOUD_CHEST = register("cloud_chest", Settings.copy(DENSE_CLOUD), s -> new CloudChestBlock(DENSE_CLOUD.getDefaultState(), s.instrument(NoteBlockInstrument.BASS).strength(2.5f)), ItemGroups.FUNCTIONAL);
    Block CLOTH_BED = register("cloth_bed", Settings.copy(Blocks.WHITE_BED), s -> new FancyBedBlock("cloth", s.sounds(BlockSoundGroup.WOOD)));
    Block CLOUD_BED = register("cloud_bed", Settings.copy(Blocks.WHITE_BED), s -> new CloudBedBlock("cloud", CLOUD.getDefaultState(), s.sounds(BlockSoundGroup.WOOL)));

    Block CLAM_SHELL = register("clam_shell", s -> new ShellsBlock(s.mapColor(MapColor.DULL_PINK).breakInstantly().nonOpaque()));
    Block SCALLOP_SHELL = register("scallop_shell", s -> new ShellsBlock(s.mapColor(MapColor.DULL_PINK).breakInstantly().nonOpaque()));
    Block TURRET_SHELL = register("turret_shell", s -> new ShellsBlock(s.mapColor(MapColor.DULL_PINK).breakInstantly().nonOpaque()));

    Block STABLE_DOOR = register("stable_door", Settings.copy(Blocks.OAK_DOOR), s -> new StableDoorBlock(BlockSetType.OAK, s), ItemGroups.FUNCTIONAL);
    Block DARK_OAK_DOOR = register("dark_oak_stable_door", Settings.copy(Blocks.OAK_DOOR), s -> new StableDoorBlock(BlockSetType.OAK, s), ItemGroups.FUNCTIONAL);
    Block CRYSTAL_DOOR = register("crystal_door", Settings.copy(Blocks.IRON_DOOR), s -> new CrystalDoorBlock(UWoodTypes.CRYSTAL, s), ItemGroups.FUNCTIONAL);
    Block CLOUD_DOOR = register("cloud_door", Settings.copy(CLOUD), s -> new CloudDoorBlock(CLOUD.getDefaultState(), UWoodTypes.CLOUD, s), ItemGroups.FUNCTIONAL);

    Block SPECTRAL_FIRE = register("spectral_fire", Settings.copy(Blocks.SOUL_FIRE), SpectralFireBlock::new);
    Block JAR = register("jar", Settings.copy(Blocks.GLASS), ItemJarBlock::new);
    Block CLOUD_JAR = register("cloud_jar", Settings.copy(Blocks.GLASS), JarBlock::new);
    Block STORM_JAR = register("storm_jar", Settings.copy(Blocks.GLASS), JarBlock::new);
    Block LIGHTNING_JAR = register("lightning_jar", Settings.copy(Blocks.GLASS), JarBlock::new);
    Block ZAP_JAR = register("zap_jar", Settings.copy(Blocks.GLASS), JarBlock::new);

    Block WORM_BLOCK = register("worm_block", s -> new ColoredFallingBlock(new ColorCode(0xFF0088), s.hardness(0.1F).resistance(0).requiresTool().sounds(BlockSoundGroup.MUD)), ItemGroups.NATURAL);
    EdibleBlock HAY_BLOCK = register("hay_block", Settings.copy(Blocks.HAY_BLOCK), s -> new EdibleBlock(Identifier.ofVanilla("hay_block"), Identifier.ofVanilla("wheat"), true, s));

    private static <T extends Block> T register(String name, Function<Block.Settings, T> block) {
        return register(name, block, null);
    }

    static <T extends Block> T register(String name, Function<Block.Settings, T> block, RegistryKey<ItemGroup> group) {
        return register(name, Settings.create(), block, group);
    }

    private static <T extends Block> T register(String name, Block.Settings settings, Function<Block.Settings, T> blockFunc) {
        return register(name, settings, blockFunc, null);
    }

    private static <T extends Block> T register(String name, Block.Settings settings, Function<Block.Settings, T> blockFunc, @Nullable RegistryKey<ItemGroup> group) {
        return register(Unicopia.id(name), settings, blockFunc, group);
    }

    static <T extends Block> T register(Identifier id, Block.Settings settings, Function<Block.Settings, T> blockFunc, @Nullable RegistryKey<ItemGroup> group) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);
        T block = blockFunc.apply(settings.registryKey(key));
        if (block instanceof TintedBlock) {
            TintedBlock.REGISTRY.add(block);
        }
        if (block instanceof SaplingBlock || block instanceof SproutBlock || block instanceof FruitBlock || block instanceof CropBlock || block instanceof DoorBlock || block instanceof TrapdoorBlock) {
            TRANSLUCENT_BLOCKS.add(block);
        }
        if (block instanceof CloudLike || block instanceof SlimePustuleBlock || block instanceof PileBlock || block instanceof SlimeLayerBlock) {
            SEMI_TRANSPARENT_BLOCKS.add(block);
        }

        if (group != null) {
            ItemGroupRegistry.register(id, s -> block instanceof CloudLike ? new CloudBlockItem(block, s) : new BlockItem(block, s), group);
        }
        return Registry.register(Registries.BLOCK, key, block);
    }

    static void bootstrap() {
        if (FabricLoader.getInstance().isModLoaded("farmersdelight")) {
            register("rice_block", Settings.copy(Blocks.HAY_BLOCK), s -> new EdibleBlock(Identifier.of("farmersdelight", "rice_bale"), Identifier.of("farmersdelight", "rice_panicle"), true, s));
            register("straw_block", Settings.copy(Blocks.HAY_BLOCK), s -> new EdibleBlock(Identifier.of("farmersdelight", "straw_bale"), Identifier.of("farmersdelight", "straw"), true, s));
        }
        BlockEntityTypeSupportHelper.of(BlockEntityType.SIGN).addSupportedBlocks(PALM_SIGN, PALM_WALL_SIGN);
        BlockEntityTypeSupportHelper.of(BlockEntityType.HANGING_SIGN).addSupportedBlocks(PALM_HANGING_SIGN, PALM_WALL_HANGING_SIGN);

        StrippableBlockRegistry.register(ZAP_LOG, STRIPPED_ZAP_LOG);
        StrippableBlockRegistry.register(PALM_LOG, STRIPPED_PALM_LOG);
        StrippableBlockRegistry.register(GOLDEN_OAK_LOG, STRIPPED_GOLDEN_OAK_LOG);
        StrippableBlockRegistry.register(ZAP_WOOD, STRIPPED_ZAP_WOOD);
        StrippableBlockRegistry.register(PALM_WOOD, STRIPPED_PALM_WOOD);
        StrippableBlockRegistry.register(GOLDEN_OAK_WOOD, STRIPPED_GOLDEN_OAK_WOOD);
        OxidizableBlocksRegistry.registerWaxableBlockPair(ZAP_LOG, WAXED_ZAP_LOG);
        OxidizableBlocksRegistry.registerWaxableBlockPair(ZAP_WOOD, WAXED_ZAP_WOOD);
        OxidizableBlocksRegistry.registerWaxableBlockPair(STRIPPED_ZAP_LOG, WAXED_STRIPPED_ZAP_LOG);
        OxidizableBlocksRegistry.registerWaxableBlockPair(STRIPPED_ZAP_WOOD, WAXED_STRIPPED_ZAP_WOOD);
        OxidizableBlocksRegistry.registerWaxableBlockPair(ZAP_PLANKS, WAXED_ZAP_PLANKS);
        OxidizableBlocksRegistry.registerWaxableBlockPair(ZAP_STAIRS, WAXED_ZAP_STAIRS);
        OxidizableBlocksRegistry.registerWaxableBlockPair(ZAP_SLAB, WAXED_ZAP_SLAB);
        OxidizableBlocksRegistry.registerWaxableBlockPair(ZAP_FENCE, WAXED_ZAP_FENCE);
        OxidizableBlocksRegistry.registerWaxableBlockPair(ZAP_FENCE_GATE, WAXED_ZAP_FENCE_GATE);
        Collections.addAll(TRANSLUCENT_BLOCKS,
                WEATHER_VANE, CHITIN_SPIKES, PLUNDER_VINE, PLUNDER_VINE_BUD, CLAM_SHELL, SCALLOP_SHELL, TURRET_SHELL, CURING_JOKE, SPECTRAL_FIRE,
                JAR, CLOUD_JAR, STORM_JAR, LIGHTNING_JAR, ZAP_JAR
        );

        FlammableBlockRegistry.getDefaultInstance().add(GREEN_APPLE_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(SWEET_APPLE_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(SOUR_APPLE_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(MANGO_LEAVES, 30, 60);

        FlammableBlockRegistry.getDefaultInstance().add(PALM_PLANKS, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_SLAB, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_FENCE_GATE, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_FENCE, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_STAIRS, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_LOG, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(STRIPPED_PALM_LOG, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(STRIPPED_PALM_WOOD, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_WOOD, 5, 5);
        FlammableBlockRegistry.getDefaultInstance().add(PALM_LEAVES, 30, 60);

        FlammableBlockRegistry.getDefaultInstance().add(BANANAS, 5, 20);
        FlammableBlockRegistry.getDefaultInstance().add(CURING_JOKE, 60, 100);

        CompostingChanceRegistry.INSTANCE.add(WORM_BLOCK, 1F);

        UBlockEntities.bootstrap();
        EdibleBlock.bootstrap();
    }
}

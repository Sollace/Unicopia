package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.item.UItems;
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

    Block ROCKS = register("rocks", new RockCropBlock(Settings.create()
            .mapColor(MapColor.STONE_GRAY)
            .nonOpaque()
            .pistonBehavior(PistonBehavior.DESTROY)
            .requiresTool()
            .ticksRandomly()
            .strength(2)
            .sounds(BlockSoundGroup.STONE)));

    Block FROSTED_OBSIDIAN = register("frosted_obsidian", new FrostedObsidianBlock(FabricBlockSettings.copy(Blocks.OBSIDIAN).ticksRandomly()));

    Block ZAP_LOG = register("zap_log", new ZapAppleLogBlock(Blocks.OAK_LOG, MapColor.GRAY, MapColor.DEEPSLATE_GRAY), ItemGroups.BUILDING_BLOCKS);
    Block ZAP_WOOD = register("zap_wood", new ZapAppleLogBlock(Blocks.OAK_WOOD, MapColor.DEEPSLATE_GRAY, MapColor.DEEPSLATE_GRAY), ItemGroups.BUILDING_BLOCKS);

    Block STRIPPED_ZAP_LOG = register("stripped_zap_log", new ZapAppleLogBlock(Blocks.STRIPPED_OAK_LOG, MapColor.LIGHT_GRAY, MapColor.GRAY), ItemGroups.BUILDING_BLOCKS);
    Block STRIPPED_ZAP_WOOD = register("stripped_zap_wood", new ZapAppleLogBlock(Blocks.STRIPPED_OAK_WOOD, MapColor.GRAY, MapColor.GRAY), ItemGroups.BUILDING_BLOCKS);

    Block ZAP_LEAVES = register("zap_leaves", new ZapAppleLeavesBlock(), ItemGroups.NATURAL);
    Block FLOWERING_ZAP_LEAVES = register("flowering_zap_leaves", new BaseZapAppleLeavesBlock(), ItemGroups.NATURAL);
    Block ZAP_LEAVES_PLACEHOLDER = register("zap_leaves_placeholder", new ZapAppleLeavesPlaceholderBlock());
    Block ZAP_BULB = register("zap_bulb", new FruitBlock(Settings.create().mapColor(MapColor.GRAY).strength(500, 1200).sounds(BlockSoundGroup.AZALEA_LEAVES), Direction.DOWN, ZAP_LEAVES, FruitBlock.DEFAULT_SHAPE, false));
    Block ZAP_APPLE = register("zap_apple", new FruitBlock(Settings.create().mapColor(MapColor.GRAY).sounds(BlockSoundGroup.AZALEA_LEAVES), Direction.DOWN, ZAP_LEAVES, FruitBlock.DEFAULT_SHAPE, false));

    Block PALM_LOG = register("palm_log", BlockConstructionUtils.createLogBlock(MapColor.OFF_WHITE, MapColor.SPRUCE_BROWN), ItemGroups.BUILDING_BLOCKS);
    Block PALM_WOOD = register("palm_wood", BlockConstructionUtils.createWoodBlock(MapColor.OFF_WHITE), ItemGroups.BUILDING_BLOCKS);
    Block STRIPPED_PALM_LOG = register("stripped_palm_log", BlockConstructionUtils.createLogBlock(MapColor.OFF_WHITE, MapColor.OFF_WHITE), ItemGroups.BUILDING_BLOCKS);
    Block STRIPPED_PALM_WOOD = register("stripped_palm_wood", BlockConstructionUtils.createWoodBlock(MapColor.OFF_WHITE), ItemGroups.BUILDING_BLOCKS);

    Block PALM_PLANKS = register("palm_planks", new Block(Settings.create().mapColor(MapColor.OFF_WHITE).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_STAIRS = register("palm_stairs", new StairsBlock(PALM_PLANKS.getDefaultState(), Settings.copy(PALM_PLANKS).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_SLAB = register("palm_slab", new SlabBlock(Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_FENCE = register("palm_fence", new FenceBlock(Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL)), ItemGroups.BUILDING_BLOCKS);
    Block PALM_FENCE_GATE = register("palm_fence_gate", new FenceGateBlock(Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).strength(2, 3).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.NORMAL), WoodType.OAK), ItemGroups.BUILDING_BLOCKS);
    Block PALM_DOOR = register("palm_door", new DoorBlock(Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).instrument(Instrument.BASS).strength(3.0f).nonOpaque().burnable().pistonBehavior(PistonBehavior.DESTROY), UWoodTypes.PALM.setType()), ItemGroups.FUNCTIONAL);
    Block PALM_TRAPDOOR = register("palm_trapdoor", new TrapdoorBlock(Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).instrument(Instrument.BASS).strength(3).nonOpaque().allowsSpawning(BlockConstructionUtils::never).burnable(), UWoodTypes.PALM.setType()), ItemGroups.FUNCTIONAL);
    Block PALM_PRESSURE_PLATE = register("palm_pressure_plate", new PressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).noCollision().strength(0.5f).sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY), BlockSetType.OAK), ItemGroups.BUILDING_BLOCKS);
    Block PALM_BUTTON = register("palm_button", BlockConstructionUtils.woodenButton(), ItemGroups.BUILDING_BLOCKS);
    Block PALM_SIGN = register("palm_sign", new SignBlock(Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).solid().instrument(Instrument.BASS).noCollision().strength(1).burnable().sounds(BlockSoundGroup.WOOD), UWoodTypes.PALM), ItemGroups.FUNCTIONAL);
    Block PALM_WALL_SIGN = register("palm_wall_sign", new WallSignBlock(Settings.create().mapColor(PALM_PLANKS.getDefaultMapColor()).solid().instrument(Instrument.BASS).noCollision().strength(1).dropsLike(PALM_SIGN).burnable(), UWoodTypes.PALM));
    Block PALM_HANGING_SIGN = register("palm_hanging_sign", new HangingSignBlock(Settings.create().mapColor(PALM_LOG.getDefaultMapColor()).solid().instrument(Instrument.BASS).noCollision().strength(1).burnable(), UWoodTypes.PALM), ItemGroups.FUNCTIONAL);
    Block PALM_WALL_HANGING_SIGN = register("palm_wall_hanging_sign", new WallHangingSignBlock(Settings.create().mapColor(PALM_LOG.getDefaultMapColor()).solid().instrument(Instrument.BASS).noCollision().strength(1.0f).burnable().dropsLike(PALM_HANGING_SIGN), UWoodTypes.PALM));

    Block PALM_LEAVES = register("palm_leaves", BlockConstructionUtils.createLeavesBlock(BlockSoundGroup.GRASS), ItemGroups.BUILDING_BLOCKS);
    Block BANANAS = register("bananas", new FruitBlock(Settings.create().mapColor(MapColor.YELLOW).sounds(BlockSoundGroup.WOOD).noCollision().ticksRandomly().breakInstantly().pistonBehavior(PistonBehavior.DESTROY), Direction.DOWN, PALM_LEAVES, VoxelShapes.fullCube()));

    PineappleCropBlock PINEAPPLE = register("pineapple", new PineappleCropBlock(Settings.create().sounds(BlockSoundGroup.GRASS).noCollision().breakInstantly().pistonBehavior(PistonBehavior.DESTROY)));

    Block MANGO_LEAVES = register("mango_leaves", new FruitBearingBlock(FabricBlockSettings.copy(Blocks.JUNGLE_LEAVES),
            0xCCFFAA00,
            () -> UBlocks.MANGO,
            () -> UItems.MANGO.getDefaultStack()
    ), ItemGroups.NATURAL);
    Block MANGO = register("mango", new FruitBlock(Settings.create().mapColor(MapColor.ORANGE), Direction.DOWN, MANGO_LEAVES, FruitBlock.DEFAULT_SHAPE));

    Block WEATHER_VANE = register("weather_vane", new WeatherVaneBlock(Settings.create().mapColor(MapColor.BLACK).requiresTool().strength(3.0f, 6.0f).sounds(BlockSoundGroup.METAL).nonOpaque().pistonBehavior(PistonBehavior.BLOCK)), ItemGroups.TOOLS);

    Block GREEN_APPLE_LEAVES = register("green_apple_leaves", new FruitBearingBlock(FabricBlockSettings.copy(Blocks.OAK_LEAVES),
            0xE5FFFF88,
            () -> UBlocks.GREEN_APPLE,
            () -> UItems.GREEN_APPLE.getDefaultStack()
    ), ItemGroups.NATURAL);
    Block GREEN_APPLE = register("green_apple", new FruitBlock(Settings.create().mapColor(MapColor.GREEN), Direction.DOWN, GREEN_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE));
    Block GREEN_APPLE_SPROUT = register("green_apple_sprout", new SproutBlock(0xE5FFFF88, () -> UItems.GREEN_APPLE_SEEDS, () -> UTreeGen.GREEN_APPLE_TREE.sapling().map(Block::getDefaultState).get()));

    Block SWEET_APPLE_LEAVES = register("sweet_apple_leaves", new FruitBearingBlock(FabricBlockSettings.copy(Blocks.OAK_LEAVES),
            0xE5FFCC88,
            () -> UBlocks.SWEET_APPLE,
            () -> UItems.SWEET_APPLE.getDefaultStack()
    ), ItemGroups.NATURAL);
    Block SWEET_APPLE = register("sweet_apple", new FruitBlock(Settings.create().mapColor(MapColor.GREEN), Direction.DOWN, SWEET_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE));
    Block SWEET_APPLE_SPROUT = register("sweet_apple_sprout", new SproutBlock(0xE5FFCC88, () -> UItems.SWEET_APPLE_SEEDS, () -> UTreeGen.SWEET_APPLE_TREE.sapling().map(Block::getDefaultState).get()));

    Block SOUR_APPLE_LEAVES = register("sour_apple_leaves", new FruitBearingBlock(FabricBlockSettings.copy(Blocks.OAK_LEAVES),
            0xE5FFCCCC,
            () -> UBlocks.SOUR_APPLE,
            () -> UItems.SOUR_APPLE.getDefaultStack()
    ), ItemGroups.NATURAL);
    Block SOUR_APPLE = register("sour_apple", new FruitBlock(Settings.create().mapColor(MapColor.GREEN), Direction.DOWN, SOUR_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE));
    Block SOUR_APPLE_SPROUT = register("sour_apple_sprout", new SproutBlock(0xE5FFCC88, () -> UItems.SOUR_APPLE_SEEDS, () -> UTreeGen.SOUR_APPLE_TREE.sapling().map(Block::getDefaultState).get()));

    Block APPLE_PIE = register("apple_pie", new PieBlock(Settings.create().solid().mapColor(MapColor.ORANGE).strength(0.5F).sounds(BlockSoundGroup.WOOL).pistonBehavior(PistonBehavior.DESTROY),
            () -> UItems.APPLE_PIE_SLICE,
            () -> UItems.APPLE_PIE,
            () -> UItems.APPLE_PIE_HOOF
    ));

    Block CHITIN = register("chitin", new SnowyBlock(Settings.create().mapColor(MapColor.PALE_PURPLE).hardness(5).requiresTool().ticksRandomly()), ItemGroups.NATURAL);
    Block SURFACE_CHITIN = register("surface_chitin", new GrowableBlock(Settings.copy(CHITIN), () -> CHITIN), ItemGroups.NATURAL);
    Block CHISELLED_CHITIN = register("chiselled_chitin", new Block(Settings.create().mapColor(MapColor.PALE_PURPLE).hardness(5).requiresTool()), ItemGroups.BUILDING_BLOCKS);
    Block CHISELLED_CHITIN_SLAB = register("chiselled_chitin_slab", new SlabBlock(Settings.copy(CHISELLED_CHITIN)), ItemGroups.BUILDING_BLOCKS);
    Block CHISELLED_CHITIN_STAIRS = register("chiselled_chitin_stairs", new StairsBlock(CHISELLED_CHITIN.getDefaultState(), Settings.copy(CHISELLED_CHITIN)), ItemGroups.BUILDING_BLOCKS);
    Block CHISELLED_CHITIN_HULL = register("chiselled_chitin_hull", new OrientedBlock(Settings.copy(CHISELLED_CHITIN)), ItemGroups.BUILDING_BLOCKS);

    SegmentedCropBlock OATS = register("oats", SegmentedCropBlock.create(11, 5, AbstractBlock.Settings.copy(Blocks.WHEAT), () -> UItems.OAT_SEEDS, null, null));
    SegmentedCropBlock OATS_STEM = register("oats_stem", OATS.createNext(5));
    SegmentedCropBlock OATS_CROWN = register("oats_crown", OATS_STEM.createNext(5));

    private static <T extends Block> T register(String name, T item) {
        return register(Unicopia.id(name), item);
    }

    private static <T extends Block> T register(String name, T block, RegistryKey<ItemGroup> group) {
        return register(Unicopia.id(name), block, group);
    }

    static <T extends Block> T register(Identifier id, T block, RegistryKey<ItemGroup> group) {
        ItemGroupRegistry.register(id, new BlockItem(block, new Item.Settings()), group);
        return register(id, block);
    }

    private static <T extends Block> T register(Identifier id, T block) {
        if (block instanceof TintedBlock) {
            TintedBlock.REGISTRY.add(block);
        }
        if (block instanceof SaplingBlock || block instanceof SproutBlock || block instanceof FruitBlock || block instanceof CropBlock || block instanceof DoorBlock || block instanceof TrapdoorBlock) {
            TRANSLUCENT_BLOCKS.add(block);
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
        TRANSLUCENT_BLOCKS.add(WEATHER_VANE);
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

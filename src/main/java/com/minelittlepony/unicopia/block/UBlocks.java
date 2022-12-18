package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.group.ItemGroupRegistry;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.world.BlockView;

public interface UBlocks {
    List<Block> TRANSLUCENT_BLOCKS = new ArrayList<>();

    Block ROCKS = register("rocks", new RockCropBlock(FabricBlockSettings.of(
                new FabricMaterialBuilder(MapColor.STONE_GRAY).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().build()
            )
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
    Block ZAP_BULB = register("zap_bulb", new FruitBlock(FabricBlockSettings.of(Material.GOURD, MapColor.GRAY).strength(500, 1200).sounds(BlockSoundGroup.AZALEA_LEAVES), Direction.DOWN, ZAP_LEAVES, FruitBlock.DEFAULT_SHAPE, false));
    Block ZAP_APPLE = register("zap_apple", new FruitBlock(FabricBlockSettings.of(Material.GOURD, MapColor.GRAY).sounds(BlockSoundGroup.AZALEA_LEAVES), Direction.DOWN, ZAP_LEAVES, FruitBlock.DEFAULT_SHAPE, false));

    Block WEATHER_VANE = register("weather_vane", new WeatherVaneBlock(FabricBlockSettings.of(Material.METAL, MapColor.BLACK).requiresTool().strength(3.0f, 6.0f).sounds(BlockSoundGroup.METAL).nonOpaque()), ItemGroups.TOOLS);

    Block GREEN_APPLE_LEAVES = register("green_apple_leaves", new FruitBearingBlock(FabricBlockSettings.copy(Blocks.OAK_LEAVES),
            0xE5FFFF88,
            () -> UBlocks.GREEN_APPLE,
            () -> UItems.GREEN_APPLE.getDefaultStack()
    ), ItemGroups.NATURAL);
    Block GREEN_APPLE = register("green_apple", new FruitBlock(FabricBlockSettings.of(Material.GOURD, MapColor.GREEN).sounds(BlockSoundGroup.WOOD), Direction.DOWN, GREEN_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE));
    Block GREEN_APPLE_SPROUT = register("green_apple_sprout", new SproutBlock(0xE5FFFF88, () -> UItems.GREEN_APPLE_SEEDS, () -> UTreeGen.GREEN_APPLE_TREE.sapling().map(Block::getDefaultState).get()));

    Block SWEET_APPLE_LEAVES = register("sweet_apple_leaves", new FruitBearingBlock(FabricBlockSettings.copy(Blocks.OAK_LEAVES),
            0xE5FFCC88,
            () -> UBlocks.SWEET_APPLE,
            () -> UItems.SWEET_APPLE.getDefaultStack()
    ), ItemGroups.NATURAL);
    Block SWEET_APPLE = register("sweet_apple", new FruitBlock(FabricBlockSettings.of(Material.GOURD, MapColor.GREEN).sounds(BlockSoundGroup.WOOD), Direction.DOWN, SWEET_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE));
    Block SWEET_APPLE_SPROUT = register("sweet_apple_sprout", new SproutBlock(0xE5FFCC88, () -> UItems.SWEET_APPLE_SEEDS, () -> UTreeGen.SWEET_APPLE_TREE.sapling().map(Block::getDefaultState).get()));

    Block SOUR_APPLE_LEAVES = register("sour_apple_leaves", new FruitBearingBlock(FabricBlockSettings.copy(Blocks.OAK_LEAVES),
            0xE5FFCCCC,
            () -> UBlocks.SOUR_APPLE,
            () -> UItems.SOUR_APPLE.getDefaultStack()
    ), ItemGroups.NATURAL);
    Block SOUR_APPLE = register("sour_apple", new FruitBlock(FabricBlockSettings.of(Material.GOURD, MapColor.GREEN).sounds(BlockSoundGroup.WOOD), Direction.DOWN, SOUR_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE));
    Block SOUR_APPLE_SPROUT = register("sour_apple_sprout", new SproutBlock(0xE5FFCC88, () -> UItems.SOUR_APPLE_SEEDS, () -> UTreeGen.SOUR_APPLE_TREE.sapling().map(Block::getDefaultState).get()));

    Block APPLE_PIE = register("apple_pie", new PieBlock(FabricBlockSettings.of(Material.CAKE, MapColor.ORANGE).strength(0.5F).sounds(BlockSoundGroup.WET_GRASS), () -> UItems.APPLE_PIE_SLICE));

    SegmentedCropBlock OATS = register("oats", SegmentedCropBlock.create(11, 5, AbstractBlock.Settings.copy(Blocks.WHEAT), () -> UItems.OAT_SEEDS, null, () -> UBlocks.OATS_STEM));
    SegmentedCropBlock OATS_STEM = register("oats_stem", OATS.createNext(5));
    SegmentedCropBlock OATS_CROWN = register("oats_crown", OATS_STEM.createNext(5));

    static <T extends Block> T register(String name, T item) {
        return register(Unicopia.id(name), item);
    }

    static <T extends Block> T register(String name, T block, ItemGroup group) {
        return register(Unicopia.id(name), block, group);
    }

    static <T extends Block> T register(Identifier id, T block, ItemGroup group) {
        UItems.register(id, ItemGroupRegistry.register(new BlockItem(block, new Item.Settings()), group));
        return register(id, block);
    }

    static <T extends Block> T register(Identifier id, T block) {
        if (block instanceof TintedBlock) {
            TintedBlock.REGISTRY.add(block);
        }
        if (block instanceof SaplingBlock || block instanceof SproutBlock || block instanceof FruitBlock || block instanceof CropBlock) {
            TRANSLUCENT_BLOCKS.add(block);
        }
        return Registry.register(Registries.BLOCK, id, block);
    }

    static void bootstrap() {
        StrippableBlockRegistry.register(ZAP_LOG, STRIPPED_ZAP_LOG);
        StrippableBlockRegistry.register(ZAP_WOOD, STRIPPED_ZAP_WOOD);
        TRANSLUCENT_BLOCKS.add(WEATHER_VANE);

        UBlockEntities.bootstrap();
    }

    static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    static Boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }
}

package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.TreeType;
import com.minelittlepony.unicopia.gas.CloudAnvilBlock;
import com.minelittlepony.unicopia.gas.CloudBlock;
import com.minelittlepony.unicopia.gas.CloudDoorBlock;
import com.minelittlepony.unicopia.gas.CloudFarmlandBlock;
import com.minelittlepony.unicopia.gas.CloudFenceBlock;
import com.minelittlepony.unicopia.gas.CloudSlabBlock;
import com.minelittlepony.unicopia.gas.CloudSoilBlock;
import com.minelittlepony.unicopia.gas.CloudStairsBlock;
import com.minelittlepony.unicopia.gas.CoverableCloudBlock;
import com.minelittlepony.unicopia.gas.GasState;
import com.minelittlepony.unicopia.gas.PillarCloudBlock;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.structure.CustomSaplingGenerator;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface UBlocks {
    CloudFarmlandBlock CLOUD_FARMLAND = register("cloud_farmland", new CloudFarmlandBlock(GasState.NORMAL.configure().build()));
    CloudBlock CLOUD_BLOCK = register("cloud_block", new CloudSoilBlock(GasState.NORMAL));
    CloudBlock ENCHANTED_CLOUD_BLOCK = register("enchanted_cloud_block", new CoverableCloudBlock(GasState.ENCHANTED));
    CloudBlock DENSE_CLOUD_BLOCK = register("dense_cloud_block", new CloudBlock(GasState.DENSE));
    CloudBlock DENSE_CLOUD_PILLAR = register("dense_cloud_pillar", new PillarCloudBlock(GasState.DENSE));

    CloudStairsBlock CLOUD_STAIRS = register("cloud_stairs", new CloudStairsBlock(CLOUD_BLOCK.getDefaultState(), GasState.NORMAL.configure().build()));
    CloudStairsBlock ENCHANTED_CLOUD_STAIRS = register("enchanted_cloud_stairs", new CloudStairsBlock(ENCHANTED_CLOUD_BLOCK.getDefaultState(), GasState.ENCHANTED.configure().build()));
    CloudStairsBlock DENSE_CLOUD_STAIRS = register("dense_cloud_stairs", new CloudStairsBlock(DENSE_CLOUD_BLOCK.getDefaultState(), GasState.DENSE.configure().build()));

    CloudSlabBlock CLOUD_SLAB = register("cloud_slab", new CloudSlabBlock(CLOUD_BLOCK.getDefaultState(), GasState.NORMAL.configure().build()));
    CloudSlabBlock ENCHANTED_CLOUD_SLAB = register("enchanted_cloud_slab", new CloudSlabBlock(ENCHANTED_CLOUD_BLOCK.getDefaultState(), GasState.ENCHANTED.configure().build()));
    CloudSlabBlock DENSE_CLOUD_SLAB = register("dense_cloud_slab", new CloudSlabBlock(DENSE_CLOUD_BLOCK.getDefaultState(), GasState.DENSE.configure().build()));

    CloudDoorBlock MISTED_GLASS_DOOR = register("misted_glass_door", new CloudDoorBlock(FabricBlockSettings.of(Material.GLASS)
                    .sounds(BlockSoundGroup.GLASS)
                    .hardness(3)
                    .resistance(200)
                    .nonOpaque()
                    .breakByTool(FabricToolTags.PICKAXES, 0)
                    .build()));
    DutchDoorBlock LIBRARY_DOOR = register("library_door", new DutchDoorBlock(FabricBlockSettings.of(Material.WOOD).
                    sounds(BlockSoundGroup.WOOD)
                    .hardness(3)
                    .nonOpaque()
                    .build()));
    DutchDoorBlock BAKERY_DOOR = register("bakery_door", new DutchDoorBlock(FabricBlockSettings.of(Material.WOOD)
                    .sounds(BlockSoundGroup.WOOD)
                    .hardness(3)
                    .nonOpaque()
                    .build()));
    DiamondDoorBlock DIAMOND_DOOR = register("diamond_door", new DiamondDoorBlock(FabricBlockSettings.of(Material.METAL)
                    .sounds(BlockSoundGroup.METAL)
                    .materialColor(MaterialColor.DIAMOND)
                    .strength(6, 20)
                    .nonOpaque()
                    .build()));

    GemTorchBlock ENCHANTED_TORCH = register("enchanted_torch", new GemTorchBlock(FabricBlockSettings.of(Material.PART)
                    .noCollision()
                    .breakInstantly()
                    .ticksRandomly()
                    .lightLevel(1).sounds(BlockSoundGroup.GLASS).build()));
    GemTorchBlock ENCHANTED_WALL_TORCH = register("enchanted_wall_torch", new WallGemTorchBlock(FabricBlockSettings.of(Material.PART)
                    .noCollision()
                    .breakInstantly()
                    .ticksRandomly()
                    .lightLevel(1).sounds(BlockSoundGroup.GLASS).build()));

    CloudAnvilBlock CLOUD_ANVIL = register("cloud_anvil", new CloudAnvilBlock(GasState.NORMAL.configure()
                    .strength(0.025F, 1)
                    .resistance(2000)
                    .breakByTool(FabricToolTags.SHOVELS, 0)
                    .ticksRandomly()
                    .build()));

    CloudFenceBlock CLOUD_FENCE = register("cloud_fence", new CloudFenceBlock(GasState.NORMAL));

    TallCropBlock ALFALFA_CROPS = register("alfalfa_crops", new TallCropBlock(FabricBlockSettings.of(Material.PLANT)
                    .noCollision()
                    .ticksRandomly()
                    .breakInstantly()
                    .sounds(BlockSoundGroup.CROP).build()));

    StickPlantBlock STICK = register("stick", new StickPlantBlock(FabricBlockSettings.of(UMaterials.STICK)
                    .nonOpaque()
                    .strength(0.2F, 0.2F)
                    .sounds(BlockSoundGroup.WOOD)
                    .build(), Items.AIR, Items.AIR, Items.AIR));
    StickPlantBlock TOMATO_PLANT = register("tomato_plant", new StickPlantBlock(FabricBlockSettings.of(UMaterials.STICK)
                    .nonOpaque()
                    .strength(0.2F, 0.2F)
                    .ticksRandomly()
                    .sounds(BlockSoundGroup.WOOD)
                    .build(), () -> UItems.TOMATO_SEEDS, () -> UItems.TOMATO, () -> UItems.ROTTEN_TOMATO));
    StickPlantBlock CLOUDSDALE_TOMATO_PLANT = register("cloudsdale_tomato_plant", new StickPlantBlock(FabricBlockSettings.of(UMaterials.STICK)
                    .nonOpaque()
                    .strength(0.2F, 0.2F)
                    .ticksRandomly()
                    .sounds(BlockSoundGroup.WOOD)
                    .build(), () -> UItems.TOMATO_SEEDS, () -> UItems.CLOUDSDALE_TOMATO, () -> UItems.ROTTEN_CLOUDSDALE_TOMATO));

    HiveWallBlock HIVE_WALL_BLOCK = register("hive_wall_block", new HiveWallBlock(FabricBlockSettings.of(UMaterials.HIVE)
                    .strength(10, 10)
                    .hardness(2)
                    .ticksRandomly()
                    .lightLevel(3)
                    .sounds(BlockSoundGroup.SAND)
                    .breakByTool(FabricToolTags.SHOVELS, 1)
                    .build()));
    ChitinBlock CHITIN_SHELL_BLOCK = register("chitin_shell_block", new ChitinBlock(FabricBlockSettings.of(UMaterials.CHITIN)
                    .strength(50, 2000)
                    .breakByTool(FabricToolTags.PICKAXES, 2)
                    .build()));
    Block CHISELED_CHITIN_SHELL_BLOCK = register("chiseled_chitin_shell_block", new ChiselledChitinBlock(FabricBlockSettings.of(UMaterials.CHITIN)
                    .strength(50, 2000)
                    .breakByTool(FabricToolTags.PICKAXES, 2)
                    .build()));

    SlimeDropBlock SLIME_DROP = register("slime_drop", new SlimeDropBlock(FabricBlockSettings.of(UMaterials.HIVE, MaterialColor.GRASS)
                    .ticksRandomly()
                    .breakInstantly()
                    .lightLevel(9)
                    .slipperiness(0.5F)
                    .sounds(BlockSoundGroup.SLIME)
                    .breakByTool(FabricToolTags.SHOVELS, 2)
                    .build()));
    SlimeLayerBlock SLIME_LAYER = register("slime_layer", new SlimeLayerBlock(FabricBlockSettings.of(Material.CLAY, MaterialColor.GRASS)
                    .sounds(BlockSoundGroup.SLIME)
                    .slipperiness(0.8F)
                    .nonOpaque()
                    .build()));

    Block SMOOTH_MARBLE_BLOCK = register("smooth_marble_block", new Block(FabricBlockSettings.of(Material.STONE)
                    .strength(0.7F, 10)
                    .breakByTool(FabricToolTags.PICKAXES)
                    .build()));
    Block CHISELED_MARBLE_BLOCK = register("chiseled_marble_block", new Block(FabricBlockSettings.of(Material.STONE)
                    .strength(0.8F, 10)
                    .breakByTool(FabricToolTags.PICKAXES)
                    .build()));
    Block SMOOTH_MARBLE_SLAB = register("smooth_marble_slab", new SlabBlock(FabricBlockSettings.of(Material.STONE)
                    .strength(0.7F, 10)
                    .breakByTool(FabricToolTags.PICKAXES)
                    .build()));

    Block SUGAR_BLOCK = register("sugar_block", new FallingBlock(FabricBlockSettings.of(Material.SAND)
                    .strength(10, 10)
                    .hardness(0.7F)
                    .sounds(BlockSoundGroup.SAND)
                    .breakByTool(FabricToolTags.SHOVELS)
                    .build()));
    Block APPLE_LEAVES = register("apple_leaves", new FruitLeavesBlock(FabricBlockSettings.of(Material.LEAVES)
                    .nonOpaque()
                    .strength(0.2F, 0.2F)
                    .ticksRandomly()
                    .sounds(BlockSoundGroup.GRASS)
                    .build())
                    .growthChance(1200)
                    .tint(0xFFEE81)
                    .fruit(W -> TreeType.OAK.pickRandomStack())
                    .compost(w -> new ItemStack(UItems.ROTTEN_APPLE)));

    SaplingBlock APPLE_SAPLING = register("apple_sapling", new SaplingBlock(CustomSaplingGenerator.APPLE_TREE, FabricBlockSettings.of(Material.WOOD)
                    .noCollision()
                    .ticksRandomly()
                    .breakInstantly()
                    .sounds(BlockSoundGroup.GRASS)
                    .build()) {});


    static <T extends Block> T register(String name, T block) {
        return Registry.BLOCK.add(new Identifier("unicopia", name), block);
    }

    static void bootstrap() { }
}

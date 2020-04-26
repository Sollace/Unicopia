package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.TreeType;
import com.minelittlepony.unicopia.gas.CloudAnvilBlock;
import com.minelittlepony.unicopia.gas.CloudBlock;
import com.minelittlepony.unicopia.gas.CloudDoorBlock;
import com.minelittlepony.unicopia.gas.CloudFarmlandBlock;
import com.minelittlepony.unicopia.gas.CloudFenceBlock;
import com.minelittlepony.unicopia.gas.CloudSlabBlock;
import com.minelittlepony.unicopia.gas.CloudStairsBlock;
import com.minelittlepony.unicopia.gas.GasState;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.structure.CustomSaplingGenerator;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.SaplingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface UBlocks {
    CloudFarmlandBlock CLOUD_FARMLAND = register(new CloudFarmlandBlock(FabricBlockSettings.of(UMaterials.CLOUD).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.WOOL).build()), "cloud_farmland");
    CloudBlock CLOUD_BLOCK = register(new CloudBlock(GasState.NORMAL), "cloud_block");
    CloudBlock ENCHANTED_CLOUD_BLOCK = register(new CloudBlock(GasState.ENCHANTED), "enchanted_cloud_block");
    CloudBlock DENSE_CLOUD_BLOCK = register(new CloudBlock(GasState.DENSE), "dense_cloud_block");

    CloudStairsBlock<CloudBlock> CLOUD_STAIRS = register(new CloudStairsBlock<>(CLOUD_BLOCK.getDefaultState(), GasState.NORMAL.configure().build()), "cloud_stairs");

    CloudSlabBlock<CloudBlock> CLOUD_SLAB = register(new CloudSlabBlock<>(CLOUD_BLOCK.getDefaultState(), GasState.NORMAL.configure().build()), "cloud_slab");
    CloudSlabBlock<CloudBlock> ENCHANTED_CLOUD_SLAB = register(new CloudSlabBlock<>(ENCHANTED_CLOUD_BLOCK.getDefaultState(), GasState.ENCHANTED.configure().build()), "enchanted_cloud_slab");
    CloudSlabBlock<CloudBlock> DENSE_CLOUD_SLAB = register(new CloudSlabBlock<>(ENCHANTED_CLOUD_BLOCK.getDefaultState(), GasState.DENSE.configure().build()), "dense_cloud_slab");

    CloudDoorBlock MISTED_GLASS_DOOR = register(new CloudDoorBlock(), "misted_glass_door");
    DutchDoorBlock LIBRARY_DOOR = register(new DutchDoorBlock(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(3).build()), "library_door");
    DutchDoorBlock BAKERY_DOOR = register(new DutchDoorBlock(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(3).build()), "bakery_door");
    DiamondDoorBlock DIAMOND_DOOR = register(new DiamondDoorBlock(), "diamond_door");

    GlowingGemBlock ENCHANTED_TORCH = register(new GlowingGemBlock(), "enchanted_torch");

    CloudAnvilBlock CLOUD_ANVIL = register(new CloudAnvilBlock(), "cloud_anvil");

    CloudFenceBlock CLOUD_FENCE = register(new CloudFenceBlock(GasState.NORMAL), "cloud_fence");

    TallCropBlock ALFALFA_CROPS = register(new TallCropBlock(FabricBlockSettings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP).build()), "alfalfa_crops");

    StickBlock STICK = register(new StickBlock(), "stick");
    TomatoPlantBlock TOMATO_PLANT = register(new TomatoPlantBlock(), "tomato_plant");

    HiveWallBlock HIVE_WALL_BLOCK = register(new HiveWallBlock(), "hive_wall_block");
    ChitinBlock CHITIN_SHELL_BLOCK = register(new ChitinBlock(), "chitin_shell_block");
    Block CHISELED_CHITIN_SHELL_BLOCK = register(new ChiselledChitinBlock(), "chiseled_chitin_shell_block");

    SlimeDropBlock SLIME_DROP = register(new SlimeDropBlock(), "slime_drop");
    SlimeLayerBlock SLIME_LAYER = register(new SlimeLayerBlock(), "slime_layer");

    Block SUGAR_BLOCK = register(new SugarBlock(), "sugar_block");
    Block APPLE_LEAVES = register(new FruitLeavesBlock()
            .growthChance(1200)
            .tint(0xFFEE81)
            .fruit(W -> TreeType.OAK.pickRandomStack())
            .compost(w -> new ItemStack(UItems.ROTTEN_APPLE)), "apple_leaves");

    SaplingBlock APPLE_SAPLING = register(new SaplingBlock(
            new CustomSaplingGenerator(5, Blocks.OAK_LOG.getDefaultState(), APPLE_LEAVES.getDefaultState()),
            FabricBlockSettings.of(Material.WOOD).build()
        ) {}, "apple_sapling");


    static <T extends Block> T register(T block, String name) {
        return Registry.BLOCK.add(new Identifier("unicopia", name), block);
    }

    static void bootstrap() { }
}

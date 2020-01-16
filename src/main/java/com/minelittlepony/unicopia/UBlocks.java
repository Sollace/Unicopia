package com.minelittlepony.unicopia;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.BlockAlfalfa;
import com.minelittlepony.unicopia.block.ChiselledChitinBlock;
import com.minelittlepony.unicopia.block.ChitinBlock;
import com.minelittlepony.unicopia.block.FruitLeavesBlock;
import com.minelittlepony.unicopia.block.GlowingGemBlock;
import com.minelittlepony.unicopia.block.BlockGrowingCuccoon;
import com.minelittlepony.unicopia.block.HiveWallBlock;
import com.minelittlepony.unicopia.block.IColourful;
import com.minelittlepony.unicopia.block.SlimeLayerBlock;
import com.minelittlepony.unicopia.block.StickBlock;
import com.minelittlepony.unicopia.block.BlockCloudAnvil;
import com.minelittlepony.unicopia.block.BlockCloudBanister;
import com.minelittlepony.unicopia.block.BlockCloudSlab;
import com.minelittlepony.unicopia.block.BlockCloudStairs;
import com.minelittlepony.unicopia.block.BlockDutchDoor;
import com.minelittlepony.unicopia.block.SugarBlock;
import com.minelittlepony.unicopia.block.UPot;
import com.minelittlepony.unicopia.block.USapling;
import com.minelittlepony.unicopia.block.BlockTomatoPlant;
import com.minelittlepony.unicopia.block.BlockCloudDoor;
import com.minelittlepony.unicopia.block.BlockDiamondDoor;
import com.minelittlepony.unicopia.block.BlockCloudFarm;
import com.minelittlepony.unicopia.block.BlockCloudFence;
import com.minelittlepony.unicopia.block.BlockCloud;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class UBlocks {
    public static final BlockCloud normal_cloud = register(new BlockCloud(UMaterials.cloud, CloudType.NORMAL), "cloud_block");
    public static final BlockCloud enchanted_cloud = register(new BlockCloud(UMaterials.cloud, CloudType.ENCHANTED), "enchanted_cloud_block");
    public static final BlockCloud packed_cloud = register(new BlockCloud(UMaterials.cloud, CloudType.PACKED), "packed_cloud_block");

    public static final BlockCloudStairs cloud_stairs = register(new BlockCloudStairs(normal_cloud.getDefaultState(), Unicopia.MODID, "cloud_stairs"));

    public static final BlockCloudSlab.Single<?> cloud_slab = register(new BlockCloudSlab.Single<>(normal_cloud, UMaterials.cloud, Unicopia.MODID, "cloud_slab"));
    public static final BlockCloudSlab.Single<?> enchanted_cloud_slab = register(new BlockCloudSlab.Single<>(enchanted_cloud, UMaterials.cloud, Unicopia.MODID, "enchanted_cloud_slab"));
    public static final BlockCloudSlab.Single<?> packed_cloud_slab = register(new BlockCloudSlab.Single<>(enchanted_cloud, UMaterials.cloud, Unicopia.MODID, "packed_cloud_slab"));

    public static final BlockCloudDoor mist_door = register(new BlockCloudDoor(UMaterials.cloud, Unicopia.MODID, "mist_door", () -> UItems.mist_door));
    public static final Block library_door = register(new BlockDutchDoor(Material.WOOD, Unicopia.MODID, "library_door", () -> UItems.library_door)
            .setSoundType(SoundType.WOOD)
            .setHardness(3));
    public static final Block bakery_door = register(new BlockDutchDoor(Material.WOOD, Unicopia.MODID, "bakery_door", () -> UItems.bakery_door)
            .setSoundType(SoundType.WOOD)
            .setHardness(3));
    public static final Block diamond_door = register(new BlockDiamondDoor(Unicopia.MODID, "diamond_door", () -> UItems.diamond_door));

    public static final GlowingGemBlock enchanted_torch = register(new GlowingGemBlock(Unicopia.MODID, "enchanted_torch"));

    public static final BlockCloudAnvil anvil = register(new BlockCloudAnvil(Unicopia.MODID, "anvil"));

    public static final BlockCloudFence cloud_fence = register(new BlockCloudFence(UMaterials.cloud, CloudType.NORMAL, Unicopia.MODID, "cloud_fence"));
    public static final BlockCloudBanister cloud_banister = register(new BlockCloudBanister(UMaterials.cloud, Unicopia.MODID, "cloud_banister"));

    public static final BlockAlfalfa alfalfa = register(new BlockAlfalfa(Unicopia.MODID, "alfalfa"));

    public static final StickBlock stick = register(new StickBlock(), "stick");
    public static final BlockTomatoPlant tomato_plant = register(new BlockTomatoPlant(Unicopia.MODID, "tomato_plant"));

    public static final BlockCloudFarm cloud_farmland = register(new BlockCloudFarm(Unicopia.MODID, "cloud_farmland"));

    public static final HiveWallBlock hive = register(new HiveWallBlock(), "hive");
    public static final ChitinBlock chitin = register(new ChitinBlock(), "chitin_block");
    public static final Block chissled_chitin = register(new ChiselledChitinBlock(), "chissled_chitin");

    public static final BlockGrowingCuccoon cuccoon = register(new BlockGrowingCuccoon(Unicopia.MODID, "cuccoon"));
    public static final SlimeLayerBlock slime_layer = register(new SlimeLayerBlock(), "slime_layer");

    public static final Block sugar_block = register(new SugarBlock(), "sugar_block");
    public static final UPot flower_pot = register(new UPot(), "flower_pot");

    public static final USapling apple_tree = register(new USapling(Unicopia.MODID, "apple_sapling")
            .setTreeGen((w, s, m) -> new WorldGenTrees(true, 5, Blocks.LOG.getDefaultState(), UBlocks.apple_leaves.getDefaultState(), false));
    public static final Block apple_leaves = register(new FruitLeavesBlock()
            .growthChance(1200)
            .tint(0xFFEE81)
            .fruit(ItemApple::getRandomItemStack)
            .compost(w -> new ItemStack(UItems.rotten_apple)), "apple_leaves");


    private static <T extends Block> T register(T block, String name) {
        return Registry.BLOCK.add(new Identifier(Unicopia.MODID, name), block);
    }

    static void registerColors(ItemColors items, BlockColors blocks) {
        items.register((stack, tint) -> {
            BlockState state = ((BlockItem)stack.getItem()).getBlock().getDefaultState();

            return blocks.getColorMultiplier(state, null, null, tint);
        }, apple_leaves);
        blocks.register((state, world, pos, tint) -> {
            Block block = state.getBlock();

            if (block instanceof IColourful) {
                return ((IColourful)block).getCustomTint(state, tint);
            }

            if (world != null && pos != null) {
                return BiomeColors.getGrassColor(world, pos);
            }

            return GrassColors.getColor(0.5D, 1);
        }, apple_leaves);
    }

    static void bootstrap() {

    }
}

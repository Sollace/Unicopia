package com.minelittlepony.unicopia.block;

import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.TempCategory;

public class BlockFruitLeaves extends BlockLeaves implements IColourful {

    public static final PropertyBool HEAVY = PropertyBool.create("heavy");

    private final Block sapling;

    private boolean hardy;

    private int baseGrowthChance;
    private int customTint;

    private Function<World, ItemStack> fruitProducer = w -> ItemStack.EMPTY;
    private Function<World, ItemStack> compostProducer = w -> ItemStack.EMPTY;

    public BlockFruitLeaves(String domain, String name, Block sapling) {
        setTranslationKey(name);
        setRegistryName(domain, name);

        setDefaultState(blockState.getBaseState()
            .withProperty(HEAVY, false)
            .withProperty(CHECK_DECAY, true)
            .withProperty(DECAYABLE, true)
        );

        this.sapling = sapling;
    }

    public BlockFruitLeaves setHardy(boolean value) {
        hardy = value;

        return this;
    }

    public BlockFruitLeaves setHarvestFruit(@Nonnull Function<World, ItemStack> producer) {
        fruitProducer = producer;

        return this;
    }

    public BlockFruitLeaves setUnharvestFruit(@Nonnull Function<World, ItemStack> producer) {
        compostProducer = producer;

        return this;
    }

    public BlockFruitLeaves setBaseGrowthChance(int chance) {
        baseGrowthChance = chance;

        return this;
    }

    public BlockFruitLeaves setTint(int tint) {
        customTint = tint;

        return this;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return Blocks.LEAVES.getDefaultState().isOpaqueCube();
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        setGraphicsLevel(!Blocks.LEAVES.getDefaultState().isOpaqueCube());
        return super.getRenderLayer();
    }

    @Deprecated
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        setGraphicsLevel(!Blocks.LEAVES.getDefaultState().isOpaqueCube());
        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(sapling);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        if (PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies().canUseEarth()) {
            if (stack.isEmpty()) {

                if (state.getValue(HEAVY)) {
                    dropApple(world, pos, state, 0);
                    world.setBlockState(pos, state.withProperty(HEAVY, false));
                }
            } else if (stack.getItem() instanceof ItemDye && EnumDyeColor.byDyeDamage(stack.getMetadata()) == EnumDyeColor.WHITE) {
                if (!state.getValue(HEAVY)) {
                    world.setBlockState(pos, state.withProperty(HEAVY, true));

                    if (!world.isRemote) {
                        world.playEvent(2005, pos, 0);
                    }

                    if (!player.capabilities.isCreativeMode) {
                        stack.shrink(1);
                    }
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote && world.isAreaLoaded(pos, 1)) {
            if (state.getValue(DECAYABLE)) {
                int growthChance = getGrowthChance(world, pos, state);

                if (!state.getValue(HEAVY) && (growthChance <= 0 || rand.nextInt(growthChance) == 0)) {
                    world.setBlockState(pos, state.withProperty(HEAVY, true));
                } else {
                    growthChance /= 10;

                    if (state.getValue(HEAVY) && (growthChance <= 0 || rand.nextInt(growthChance) == 0)) {
                        dropApple(world, pos, state, 0);
                        world.setBlockState(pos, state.withProperty(HEAVY, false));
                    } else {
                        super.updateTick(world, pos, state, rand);
                    }
                }
            }
        }
    }

    protected int getGrowthChance(World world, BlockPos pos, IBlockState state) {
        int chance = baseGrowthChance;

        if (!hardy && !world.isDaytime()) {
            chance *= 40;
        }

        if (world.getLight(pos) >= 4) {
            chance /= 3;
        }

        TempCategory temp = world.getBiome(pos).getTempCategory();

        if (!hardy && temp == TempCategory.COLD) {
            chance *= 1000;
        }

        if (temp == TempCategory.WARM) {
            chance /= 100;
        }

        if (temp == TempCategory.MEDIUM) {
            chance /= 50;
        }

        return chance;
    }

    @Override
    public int getCustomTint(IBlockState state, int tint) {
        return customTint;
    }

    @Override
    protected void dropApple(World world, BlockPos pos, IBlockState state, int chance) {
        if (state.getValue(HEAVY)) {
            Function<World, ItemStack> fruit = world.rand.nextInt(40) == 0 ? compostProducer : fruitProducer;
            spawnAsEntity(world, pos, fruit.apply(world));

            world.playSound(null, pos, SoundEvents.ENTITY_ITEMFRAME_PLACE, SoundCategory.BLOCKS, 0.3F, 1);
        }
    }

    @Override
    public NonNullList<ItemStack> onSheared(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune) {
        return NonNullList.withSize(1, new ItemStack(this, 1, 0));
    }

    @Override
    public EnumType getWoodType(int meta) {
        return EnumType.OAK;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(HEAVY, (meta & 1) != 0)
                .withProperty(DECAYABLE, (meta & 2) != 0)
                .withProperty(CHECK_DECAY, (meta & 4) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;

        if (state.getValue(HEAVY)) {
            i |= 1;
        }

        if (!state.getValue(DECAYABLE)) {
            i |= 2;
        }

        if (state.getValue(CHECK_DECAY)) {
            i |= 4;
        }

        return i;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HEAVY, CHECK_DECAY, DECAYABLE);
    }
}

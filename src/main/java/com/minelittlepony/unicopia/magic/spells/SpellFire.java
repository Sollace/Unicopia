package com.minelittlepony.unicopia.magic.spells;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.entity.IMagicals;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.magic.IDispenceable;
import com.minelittlepony.unicopia.magic.IUseable;
import com.minelittlepony.util.MagicalDamageSource;
import com.minelittlepony.util.PosHelper;
import com.minelittlepony.util.VecHelper;
import com.minelittlepony.util.collection.IStateMapping;
import com.minelittlepony.util.collection.StateMapList;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class SpellFire extends AbstractSpell.RangedAreaSpell implements IUseable, IDispenceable {

    public final StateMapList affected = new StateMapList();

    private static final IShape visual_effect_region = new Sphere(false, 0.5);
    private static final IShape effect_range = new Sphere(false, 4);

    public SpellFire() {
        affected.removeBlock(s -> s.getBlock() == Blocks.SNOW_LAYER || s.getBlock() == Blocks.SNOW);
        affected.removeBlock(s -> s.getBlock() instanceof BlockBush);

        affected.replaceBlock(Blocks.CLAY, Blocks.HARDENED_CLAY);
        affected.replaceBlock(Blocks.OBSIDIAN, Blocks.LAVA);
        affected.replaceBlock(Blocks.GRASS, Blocks.DIRT);
        affected.replaceBlock(Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE);

        affected.replaceProperty(Blocks.COBBLESTONE_WALL, BlockWall.VARIANT, BlockWall.EnumType.MOSSY, BlockWall.EnumType.NORMAL);
        affected.replaceProperty(Blocks.STONEBRICK, BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY, BlockStoneBrick.EnumType.DEFAULT);
        affected.replaceProperty(Blocks.MONSTER_EGG, BlockSilverfish.VARIANT, BlockSilverfish.EnumType.MOSSY_STONEBRICK, BlockSilverfish.EnumType.STONEBRICK);
        affected.replaceProperty(Blocks.DIRT, BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL, BlockDirt.DirtType.COARSE_DIRT);

        affected.setProperty(Blocks.FARMLAND, BlockFarmland.MOISTURE, 0);

        affected.add(IStateMapping.build(
                s -> s.getBlock() == Blocks.DIRT && s.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT,
                s -> (Math.random() <= 0.15 ? s.with(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT) : s)));
    }

    @Override
    public String getName() {
        return "fire";
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.GOOD;
    }

    @Override
    public int getTint() {
        return 0xFF5D00;
    }

    @Override
    public boolean update(ICaster<?> source) {
        return false;
    }

    @Override
    public void render(ICaster<?> source) {
        source.spawnParticles(visual_effect_region, source.getCurrentLevel() * 6, pos -> {
            source.getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.x, pos.y, pos.z, 0, 0, 0);
        });
    }

    @Override
    public SpellCastResult onUse(ItemStack stack, Affinity affinity, PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ) {
        boolean result = false;

        if (player == null || player.isSneaking()) {
            result = applyBlocks(world, pos);
        } else {

            for (BlockPos i : PosHelper.getAllInRegionMutable(pos, effect_range)) {
                result |= applyBlocks(world, i);
            }
        }

        if (!result) {
            result = applyEntities(player, world, pos);
        }

        return result ? SpellCastResult.DEFAULT : SpellCastResult.NONE;
    }

    @Override
    public SpellCastResult onUse(ItemStack stack, Affinity affinity, PlayerEntity player, World world, @Nullable Entity hitEntity) {
        if (hitEntity == null) {
            return SpellCastResult.NONE;
        }

        return applyEntitySingle(player, world, hitEntity) ? SpellCastResult.DEFAULT : SpellCastResult.NONE;
    }

    @Override
    public SpellCastResult onDispenced(BlockPos pos, Direction facing, IBlockSource source, Affinity affinity) {
        pos = pos.offset(facing, 4);

        boolean result = false;

        for (BlockPos i : PosHelper.getAllInRegionMutable(pos, effect_range)) {
            result |= applyBlocks(source.getWorld(), i);
        }

        if (!result) {
            result = applyEntities(null, source.getWorld(), pos);
        }

        return result ? SpellCastResult.NONE : SpellCastResult.DEFAULT;
    }

    protected boolean applyBlocks(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block id = state.getBlock();

        if (id != Blocks.AIR) {
            if (id == Blocks.ICE || id == Blocks.PACKED_ICE) {
                world.setBlockState(pos, (world.provider.doesWaterVaporize() ? Blocks.AIR : Blocks.WATER).getDefaultState());
                playEffect(world, pos);

                return true;
            } else if (id == Blocks.NETHERRACK) {
                if (world.getBlockState(pos.up()).getMaterial() == Material.AIR) {

                    if (world.rand.nextInt(300) == 0) {
                        world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
                    }

                    return true;
                }
            } else if (id == Blocks.REDSTONE_WIRE) {
                int power = world.rand.nextInt(5) == 3 ? 15 : 3;

                sendPower(world, pos, power, 3, 0);

                return true;
            } else if (id == Blocks.SAND && world.rand.nextInt(10) == 0) {
                if (isSurroundedBySand(world, pos)) {
                    world.setBlockState(pos, Blocks.GLASS.getDefaultState());

                    playEffect(world, pos);
                    return true;
                }
            } else if (id instanceof BlockLeaves) {
                if (world.getBlockState(pos.up()).getMaterial() == Material.AIR) {
                    world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());

                    playEffect(world, pos);
                    return true;
                }
            } else {
                BlockState newState = affected.getConverted(state);

                if (!state.equals(newState)) {
                    world.setBlockState(pos, newState, 3);

                    playEffect(world, pos);
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean applyEntities(Entity owner, World world, BlockPos pos) {
        return VecHelper
                .findAllEntitiesInRange(owner, world, pos, 3)
                .filter(i -> applyEntitySingle(owner, world, i))
                .count() > 0;
    }

    protected boolean applyEntitySingle(Entity owner, World world, Entity e) {
        if ((!e.equals(owner) ||
                (owner instanceof PlayerEntity && !Predicates.MAGI.test(owner))) && !(e instanceof EntityItem)
        && !(e instanceof IMagicals)) {
            e.setFire(60);
            e.attackEntityFrom(getDamageCause(e, (LivingEntity)owner), 0.1f);
            playEffect(world, e.getPosition());
            return true;
        }

        return false;
    }

    protected DamageSource getDamageCause(Entity target, LivingEntity attacker) {
        return MagicalDamageSource.causeMobDamage("fire", attacker);
    }

    /**
     * Transmists power to a piece of redstone
     */
    private void sendPower(World w, BlockPos pos, int power, int max, int i) {
        BlockState state = w.getBlockState(pos);
        Block id = state.getBlock();

        if (i < max && id == Blocks.REDSTONE_WIRE) {
            i++;

            w.setBlockState(pos, state.with(BlockRedstoneWire.POWER, power));

            sendPower(w, pos.up(), power, max, i);
            sendPower(w, pos.down(), power, max, i);
            sendPower(w, pos.north(), power, max, i);
            sendPower(w, pos.south(), power, max, i);
            sendPower(w, pos.east(), power, max, i);
            sendPower(w, pos.west(), power, max, i);
        }
    }

    protected void playEffect(World world, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        world.playSound((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.AMBIENT, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F, true);

        for (int i = 0; i < 8; ++i) {
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)x + Math.random(), (double)y + Math.random(), (double)z + Math.random(), 0.0D, 0.0D, 0.0D);
        }
    }

    public static boolean isSurroundedBySand(World w, BlockPos pos) {
        return isSand(w, pos.up()) && isSand(w, pos.down()) &&
                isSand(w, pos.north()) && isSand(w, pos.south()) &&
                isSand(w, pos.east()) && isSand(w, pos.west());
    }

    public static boolean isSand(World world, BlockPos pos) {
        Block id = world.getBlockState(pos).getBlock();
        return id == Blocks.SAND || id == Blocks.GLASS;
    }
}

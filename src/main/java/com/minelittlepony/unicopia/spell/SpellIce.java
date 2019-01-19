package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.UMaterials;
import com.minelittlepony.util.MagicalDamageSource;
import com.minelittlepony.util.PosHelper;
import com.minelittlepony.util.blockstate.IStateMapping;
import com.minelittlepony.util.blockstate.StateMapList;
import com.minelittlepony.util.shape.Sphere;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class SpellIce extends AbstractSpell implements IUseAction, IDispenceable {

	public static final StateMapList affected = new StateMapList();

	static {
		affected.add(IStateMapping.build(
		        s -> s.getMaterial() == Material.WATER,
		        s -> Blocks.ICE.getDefaultState()));
        affected.add(IStateMapping.build(
                s -> s.getMaterial() == Material.LAVA,
                s -> Blocks.OBSIDIAN.getDefaultState()));
        affected.add(IStateMapping.build(
                s -> s.getBlock() == Blocks.SNOW_LAYER,
                s -> s.cycleProperty(BlockSnow.LAYERS)));
		affected.replaceBlock(Blocks.FIRE, Blocks.AIR);
		affected.setProperty(Blocks.REDSTONE_WIRE, BlockRedstoneWire.POWER, 0);
	}

	protected int rad = 3;

    @Override
    public int getCurrentLevel() {
        return 0;
    }

    @Override
    public void setCurrentLevel(int level) {
    }

    @Override
    public String getName() {
        return "ice";
    }

    @Override
    public boolean update(ICaster<?> source, int level) {
        return false;
    }

    @Override
    public void render(ICaster<?> source, int level) {
    }

	@Override
	public SpellCastResult onDispenced(BlockPos pos, EnumFacing facing, IBlockSource source) {
		return applyBlocks(null, source.getWorld(), pos.offset(facing, rad)) ? SpellCastResult.NONE : SpellCastResult.DEFAULT;
	}

	@Override
	public SpellCastResult onUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (player != null && player.isSneaking()) {
			applyBlockSingle(world, pos);
		} else {
			applyBlocks(player, world, pos);
		}

		return SpellCastResult.DEFAULT;
	}

	@Override
	public SpellCastResult onUse(ItemStack stack, EntityPlayer player, World world, Entity hitEntity) {
		if (hitEntity != null) {
			applyEntitySingle(player, hitEntity);
			return SpellCastResult.DEFAULT;
		}

		return SpellCastResult.NONE;
	}

	private boolean applyBlocks(EntityPlayer owner, World world, BlockPos pos) {

		for (BlockPos i : PosHelper.getAllInRegionMutable(pos, new Sphere(false, rad))) {
		    applyBlockSingle(world, i);
		}

		return applyEntities(owner, world, pos);
	}

	protected boolean applyEntities(EntityPlayer owner, World world, BlockPos pos) {
	    return VecHelper.findAllEntitiesInRange(owner, world, pos, 3).filter(i -> {
	        applyEntitySingle(owner, i);
	        return true;
	    }).count() > 0;
	}

	protected void applyEntitySingle(EntityPlayer owner, Entity e) {
		if (e instanceof EntityTNTPrimed) {
			e.setDead();
			e.getEntityWorld().setBlockState(e.getPosition(), Blocks.TNT.getDefaultState());
		} else {
			if (e.isBurning()) {
				e.extinguish();
			} else {
				DamageSource d = MagicalDamageSource.causePlayerDamage("cold", owner);
				e.attackEntityFrom(d, 2);
			}
		}
	}

	private void applyBlockSingle(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		Block id = state.getBlock();

		IBlockState converted = affected.getConverted(state);

		if (!state.equals(converted)) {
			world.setBlockState(pos, converted, 3);
		} else if (state.getMaterial() != UMaterials.cloud && state.isSideSolid(world, pos, EnumFacing.UP)
		        || (id == Blocks.SNOW)
		        || (id instanceof BlockLeaves)) {
			incrementIce(world, pos.up());
		} else if (id == Blocks.ICE && world.rand.nextInt(10) == 0) {
			if (isSurroundedByIce(world, pos)) {
				world.setBlockState(pos, Blocks.PACKED_ICE.getDefaultState());
			}
		}

		world.spawnParticle(EnumParticleTypes.WATER_SPLASH, pos.getX() + world.rand.nextFloat(), pos.getY() + 1, pos.getZ() + world.rand.nextFloat(), 0, 0, 0);
	}

	public static boolean isSurroundedByIce(World w, BlockPos pos) {
		return isIce(w, pos.up()) && isIce(w, pos.down()) &&
				isIce(w, pos.north()) && isIce(w, pos.south()) &&
				isIce(w, pos.east()) && isIce(w, pos.west());
	}

	public static boolean isIce(World world, BlockPos pos) {
		return world.getBlockState(pos).getMaterial() == Material.ICE;
	}

	private void incrementIce(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		Block id = state.getBlock();

		if (id == Blocks.AIR || (id instanceof BlockBush)) {
			world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState(), 3);
		}
	}
}

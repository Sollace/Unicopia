package com.minelittlepony.unicopia.magic.spell;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.CastResult;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.magic.DispenceableSpell;
import com.minelittlepony.unicopia.magic.Useable;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.blockstate.StateMaps;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;
import com.minelittlepony.unicopia.world.block.UMaterials;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class IceSpell extends AbstractRangedAreaSpell implements Useable, DispenceableSpell {

    private final int rad = 3;
    private final Shape effect_range = new Sphere(false, rad);

    @Override
    public String getName() {
        return "ice";
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.GOOD;
    }

    @Override
    public int getTint() {
        return 0xBDBDF9;
    }

    @Override
    public boolean update(Caster<?> source) {
        return false;
    }

    @Override
    public void render(Caster<?> source) {
    }

    @Override
    public CastResult onDispenced(BlockPos pos, Direction facing, BlockPointer source, Affinity affinity) {
        return applyBlocks(null, source.getWorld(), pos.offset(facing, rad)) ? CastResult.NONE : CastResult.DEFAULT;
    }

    @Override
    public CastResult onUse(ItemUsageContext context, Affinity affinity) {
        if (context.getPlayer() != null && context.getPlayer().isSneaking()) {
            applyBlockSingle(context.getPlayer(), context.getWorld(), context.getBlockPos());
        } else {
            applyBlocks(context.getPlayer(), context.getWorld(), context.getBlockPos());
        }

        return CastResult.DEFAULT;
    }

    @Override
    public CastResult onUse(ItemStack stack, Affinity affinity, PlayerEntity player, World world, @Nullable Entity hitEntity) {
        if (hitEntity != null && applyEntitySingle(player, hitEntity)) {
            return CastResult.DEFAULT;
        }

        return CastResult.NONE;
    }

    private boolean applyBlocks(PlayerEntity owner, World world, BlockPos pos) {

        PosHelper.getAllInRegionMutable(pos, effect_range).forEach(i -> applyBlockSingle(owner, world, i));

        return applyEntities(owner, world, pos);
    }

    protected boolean applyEntities(PlayerEntity owner, World world, BlockPos pos) {
        return VecHelper.findAllEntitiesInRange(owner, world, pos, 3).filter(i ->
            applyEntitySingle(owner, i)
        ).count() > 0;
    }

    protected boolean applyEntitySingle(PlayerEntity owner, Entity e) {
        if (e instanceof TntEntity) {
            e.remove();
            e.getEntityWorld().setBlockState(e.getBlockPos(), Blocks.TNT.getDefaultState());
        } else if (e.isOnFire()) {
            e.extinguish();
        } else {
            e.damage(MagicalDamageSource.causePlayerDamage("cold", owner), 2);
        }

        return true;
    }

    private void applyBlockSingle(PlayerEntity owner, World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block id = state.getBlock();

        BlockState converted = StateMaps.ICE_AFFECTED.getConverted(state);

        if (!state.equals(converted)) {
            world.setBlockState(pos, converted, 3);
        } else if (state.getMaterial() != UMaterials.CLOUD && world.isTopSolid(pos, owner)
                || (id == Blocks.SNOW)
                || state.isIn(BlockTags.LEAVES)) {
            incrementIce(world, pos.up());
        } else if (state.getMaterial() == Material.ICE && world.random.nextInt(10) == 0) {
            if (isSurroundedByIce(world, pos)) {
                world.setBlockState(pos, Blocks.PACKED_ICE.getDefaultState());
            }
        }

        world.addParticle(ParticleTypes.SPLASH, pos.getX() + world.random.nextFloat(), pos.getY() + 1, pos.getZ() + world.random.nextFloat(), 0, 0, 0);
    }

    public static boolean isSurroundedByIce(World w, BlockPos pos) {
        return !PosHelper.adjacentNeighbours(pos).anyMatch(i ->
            w.getBlockState(i).getMaterial() == Material.ICE
        );
    }

    private void incrementIce(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block id = state.getBlock();

        if (id == Blocks.AIR || (id instanceof PlantBlock)) {
            world.setBlockState(pos, Blocks.SNOW.getDefaultState(), 3);
        }
    }
}

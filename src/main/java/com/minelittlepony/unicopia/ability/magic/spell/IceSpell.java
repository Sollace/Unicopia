package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Thrown;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class IceSpell extends AbstractSpell implements Thrown, Attached {

    private final int rad = 3;
    private final Shape effect_range = new Sphere(false, rad);

    protected IceSpell(SpellType<?> type) {
        super(type);
    }

    @Override
    public boolean onBodyTick(Caster<?> source) {
        return onThrownTick(source);
    }

    @Override
    public boolean onThrownTick(Caster<?> source) {
        LivingEntity owner = source.getMaster();

        PosHelper.getAllInRegionMutable(source.getOrigin(), effect_range)
            .forEach(i -> applyBlockSingle(owner, source.getWorld(), i));

        return applyEntities(source.getMaster(), source.getWorld(), source.getOriginVector());
    }

    protected boolean applyEntities(LivingEntity owner, World world, Vec3d pos) {
        return !VecHelper.findInRange(owner, world, pos, 3, i -> applyEntitySingle(owner, i)).isEmpty();
    }

    protected boolean applyEntitySingle(LivingEntity owner, Entity e) {
        if (e instanceof TntEntity) {
            e.remove();
            e.getEntityWorld().setBlockState(e.getBlockPos(), Blocks.TNT.getDefaultState());
        } else if (e.isOnFire()) {
            e.extinguish();
        } else {
            e.damage(MagicalDamageSource.create("cold", owner), 2);
        }

        return true;
    }

    private void applyBlockSingle(Entity owner, World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block id = state.getBlock();

        BlockState converted = StateMaps.ICE_AFFECTED.getConverted(state);

        if (!state.equals(converted)) {
            world.setBlockState(pos, converted, 3);
        } else if (world.isTopSolid(pos, owner)
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

    private static boolean isSurroundedByIce(World w, BlockPos pos) {
        return !PosHelper.adjacentNeighbours(pos).anyMatch(i ->
            w.getBlockState(i).getMaterial() == Material.ICE
        );
    }

    private static void incrementIce(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block id = state.getBlock();

        if (id == Blocks.AIR || (id instanceof PlantBlock)) {
            world.setBlockState(pos, Blocks.SNOW.getDefaultState(), 3);
        }
    }
}

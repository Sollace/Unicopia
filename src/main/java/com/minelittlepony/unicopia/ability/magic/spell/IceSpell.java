package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
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
import net.minecraft.world.World;

public class IceSpell extends AbstractRangedAreaSpell {

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
        LivingEntity owner = source.getOwner();

        PosHelper.getAllInRegionMutable(source.getOrigin(), effect_range)
            .forEach(i -> applyBlockSingle(owner, source.getWorld(), i));

        return applyEntities(source.getOwner(), source.getWorld(), source.getOrigin());
    }

    @Override
    public void render(Caster<?> source) {
    }

    protected boolean applyEntities(LivingEntity owner, World world, BlockPos pos) {
        return VecHelper.findAllEntitiesInRange(owner, world, pos, 3).filter(i ->
            applyEntitySingle(owner, i)
        ).count() > 0;
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

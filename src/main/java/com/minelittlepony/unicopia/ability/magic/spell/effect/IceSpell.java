package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.ProjectileCapable;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.particle.ParticleUtils;
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
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class IceSpell extends AbstractSpell implements ProjectileCapable {

    private final int rad = 3;
    private final Shape effect_range = new Sphere(false, rad);

    protected IceSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        LivingEntity owner = source.getMaster();

        PosHelper.getAllInRegionMutable(source.getOrigin(), effect_range)
            .forEach(i -> {
                if (applyBlockSingle(owner, source.getWorld(), i)) {
                    ParticleUtils.spawnParticle(ParticleTypes.SPLASH, source.getWorld(), new Vec3d(
                            i.getX() + source.getWorld().random.nextFloat(),
                            i.getY() + 1,
                            i.getZ() + source.getWorld().random.nextFloat()), Vec3d.ZERO);
                }
            });

        return applyEntities(source.getMaster(), source.getWorld(), source.getOriginVector());
    }

    protected boolean applyEntities(LivingEntity owner, World world, Vec3d pos) {
        return !VecHelper.findInRange(owner, world, pos, 3, i -> applyEntitySingle(owner, i)).isEmpty();
    }

    protected boolean applyEntitySingle(LivingEntity owner, Entity e) {
        if (e instanceof TntEntity) {
            e.remove(RemovalReason.DISCARDED);
            e.getEntityWorld().setBlockState(e.getBlockPos(), Blocks.TNT.getDefaultState());
        } else if (e.isOnFire()) {
            e.extinguish();
        } else {
            e.damage(MagicalDamageSource.create("cold", owner), 2);
        }

        return true;
    }

    private boolean applyBlockSingle(Entity owner, World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (StateMaps.ICE_AFFECTED.convert(world, pos)) {
            return true;
        }

        if (world.isTopSolid(pos, owner)
                || state.isOf(Blocks.SNOW)
                || state.isIn(BlockTags.LEAVES)) {
            incrementIce(world, pos.up());
            return true;
        }

        if (state.getMaterial() == Material.ICE
                && world.random.nextInt(10) == 0
                && isSurroundedByIce(world, pos)) {
            world.setBlockState(pos, Blocks.PACKED_ICE.getDefaultState());
            return true;
        }

        return false;
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

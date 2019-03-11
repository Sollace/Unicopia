package com.minelittlepony.unicopia.spell;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.EntityProjectile;
import com.minelittlepony.unicopia.init.UParticles;
import com.minelittlepony.util.PosHelper;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;

public class SpellScorch extends SpellFire implements ITossedEffect {

    @Override
    public String getName() {
        return "scorch";
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public int getTint() {
        return 0;
    }

    @Override
    public boolean update(ICaster<?> source) {

        BlockPos pos = PosHelper.findSolidGroundAt(source.getWorld(), source.getOrigin());

        IBlockState state = source.getWorld().getBlockState(pos);

        IBlockState newState = affected.getConverted(state);

        if (!state.equals(newState)) {
            source.getWorld().setBlockState(pos, newState, 3);
            source.spawnParticles(new Sphere(false, 1), 5, p -> {
                p = PosHelper.offset(p, pos);

                source.getWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                        p.x, p.y, p.z, 0, 0, 0);
            });
        }

        return true;
    }

    @Override
    public void render(ICaster<?> source) {
        source.spawnParticles(EnumParticleTypes.FLAME.getParticleID(), 3);
        source.spawnParticles(UParticles.UNICORN_MAGIC, 3, getTint());
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.BAD;
    }

    @Override
    @Nullable
    public EntityProjectile toss(ICaster<?> caster) {
        EntityProjectile projectile = ITossedEffect.super.toss(caster);

        if (projectile != null) {
            projectile.setNoGravity(true);
        }

        return projectile;
    }

    @Override
    public void onImpact(ICaster<?> caster, BlockPos pos, IBlockState state) {
        if (caster.isLocal()) {
            caster.getWorld().newExplosion(caster.getOwner(), pos.getX(), pos.getY(), pos.getZ(), 2, true, true);
        }
    }
}

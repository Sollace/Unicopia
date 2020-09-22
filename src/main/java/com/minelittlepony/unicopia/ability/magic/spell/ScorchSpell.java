package com.minelittlepony.unicopia.ability.magic.spell;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.ThrowableSpell;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.projectile.Projectile;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion.DestructionType;

public class ScorchSpell extends FireSpell implements ThrowableSpell {

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
    public boolean update(Caster<?> source) {

        BlockPos pos = PosHelper.findSolidGroundAt(source.getWorld(), source.getOrigin());

        BlockState state = source.getWorld().getBlockState(pos);

        BlockState newState = StateMaps.FIRE_AFFECTED.getConverted(state);

        if (!state.equals(newState)) {
            source.getWorld().setBlockState(pos, newState, 3);
            source.spawnParticles(new Sphere(false, 1), 5, p -> {
                source.addParticle(ParticleTypes.SMOKE, PosHelper.offset(p, pos), Vec3d.ZERO);
            });
        }

        return true;
    }

    @Override
    public void render(Caster<?> source) {
        source.addParticle(ParticleTypes.END_ROD, source.getOriginVector(), Vec3d.ZERO);
        source.spawnParticles(ParticleTypes.FLAME, 3);
        source.spawnParticles(new MagicParticleEffect(getTint()), 3);
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BAD;
    }

    @Override
    @Nullable
    public Projectile toss(Caster<?> caster) {
        Projectile projectile = ThrowableSpell.super.toss(caster);

        if (projectile != null) {
            projectile.setGravity(false);
        }

        return projectile;
    }

    @Override
    public void onImpact(Caster<?> caster, BlockPos pos, BlockState state) {
        if (caster.isLocal()) {
            caster.getWorld().createExplosion(caster.getOwner(), pos.getX(), pos.getY(), pos.getZ(), 2, DestructionType.DESTROY);
        }
    }
}

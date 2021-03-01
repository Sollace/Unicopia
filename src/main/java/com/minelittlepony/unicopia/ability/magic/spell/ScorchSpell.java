package com.minelittlepony.unicopia.ability.magic.spell;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ScorchSpell extends FireSpell {

    @Override
    public String getName() {
        return "scorch";
    }

    @Override
    public int getTint() {
        return 0;
    }

    @Override
    public boolean update(Caster<?> source) {

        BlockPos pos = PosHelper.findSolidGroundAt(source.getWorld(), source.getOrigin(), source.getPhysics().getGravitySignum());

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
    public MagicProjectileEntity toss(Caster<?> caster) {
        MagicProjectileEntity projectile = super.toss(caster);

        if (projectile != null) {
            projectile.setNoGravity(true);
        }

        return projectile;
    }
}

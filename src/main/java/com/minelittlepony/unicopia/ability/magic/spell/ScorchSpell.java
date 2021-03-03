package com.minelittlepony.unicopia.ability.magic.spell;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ScorchSpell extends FireSpell {

    protected ScorchSpell(SpellType<?> type) {
        super(type);
    }

    @Override
    public boolean onThrownTick(Caster<?> source) {

        BlockPos pos = PosHelper.findSolidGroundAt(source.getWorld(), source.getOrigin(), source.getPhysics().getGravitySignum());

        if (StateMaps.FIRE_AFFECTED.convert(source.getWorld(), pos)) {
            source.spawnParticles(new Sphere(false, 1), 5, p -> {
                source.addParticle(ParticleTypes.SMOKE, PosHelper.offset(p, pos), Vec3d.ZERO);
            });
        }

        return true;
    }

    @Override
    protected void generateParticles(Caster<?> source) {
        source.addParticle(ParticleTypes.END_ROD, source.getOriginVector(), Vec3d.ZERO);
        source.spawnParticles(ParticleTypes.FLAME, 3);
        source.spawnParticles(new MagicParticleEffect(getType().getColor()), 3);
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

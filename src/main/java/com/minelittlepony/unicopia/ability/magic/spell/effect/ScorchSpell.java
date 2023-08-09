package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ScorchSpell extends FireSpell implements ProjectileDelegate.ConfigurationListener {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FIRE, 10)
            .build();

    protected ScorchSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return method == CastingMethod.STAFF ? this : toPlaceable();
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        BlockPos pos = PosHelper.findSolidGroundAt(source.asWorld(), source.getOrigin(), source.getPhysics().getGravitySignum());

        if (source.canModifyAt(pos) && StateMaps.FIRE_AFFECTED.convert(source.asWorld(), pos)) {
            source.spawnParticles(new Sphere(false, Math.max(1, getTraits().get(Trait.POWER))), 5, p -> {
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
    public void configureProjectile(MagicProjectileEntity projectile, Caster<?> caster) {
        projectile.setNoGravity(true);
    }
}

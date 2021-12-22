package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.ProjectileSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;

import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;

public class FireBoltSpell extends AbstractSpell implements ProjectileSpell {

    protected FireBoltSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, Entity entity) {
        entity.setOnFireFor(90);
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        if (situation == Situation.PROJECTILE) {
            return true;
        }

        for (int i = 0; i < getNumberOfBalls(caster); i++) {
            getType().create(getTraits()).toThrowable().throwProjectile(caster);
            caster.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 0.7F, 0.4F / (caster.getWorld().random.nextFloat() * 0.4F + 0.8F));
        }
        return false;
    }

    @Override
    public void configureProjectile(MagicProjectileEntity projectile, Caster<?> caster) {
        projectile.setItem(Items.FIRE_CHARGE.getDefaultStack());
        projectile.addThrowDamage(9);
        projectile.setFireTicks(900000);
        projectile.setVelocity(projectile.getVelocity().multiply(1.3));
    }

    protected int getNumberOfBalls(Caster<?> caster) {
        return 1 + caster.getWorld().random.nextInt(3);
    }
}

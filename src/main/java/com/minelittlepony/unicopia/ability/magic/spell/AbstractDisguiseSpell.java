package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Optional;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.projectile.ProjectileImpactListener;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * Base implementation for a spell that changes the player's appearance.
 */
public abstract class AbstractDisguiseSpell extends AbstractSpell implements Disguise, ProjectileImpactListener {

    private final EntityAppearance disguise = new EntityAppearance();

    public AbstractDisguiseSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public void onDestroyed(Caster<?> caster) {
        caster.asEntity().calculateDimensions();
        caster.asEntity().setInvisible(false);
        if (caster instanceof Pony) {
            ((Pony) caster).setInvisible(false);
        }
        disguise.remove();
    }

    @Override
    public EntityAppearance getDisguise() {
        return disguise;
    }

    @Override
    public boolean onProjectileImpact(ProjectileEntity projectile) {
        return disguise.getAppearance() == projectile;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        return situation == Situation.BODY && update(source, true);
    }

    @Override
    public void setDead() {
        super.setDead();
        disguise.remove();
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        disguise.toNBT(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        disguise.fromNBT(compound);
    }

    @Override
    public Optional<EntityAppearance> getAppearance() {
        return Optional.ofNullable(disguise);
    }

    public static Entity getAppearance(Entity e) {
        return e instanceof PlayerEntity ? Pony.of((PlayerEntity)e)
                .getSpellSlot()
                .get(SpellPredicate.IS_DISGUISE, true)
                .map(AbstractDisguiseSpell::getDisguise)
                .map(EntityAppearance::getAppearance)
                .orElse(e) : e;
    }
}

package com.minelittlepony.unicopia.redux.magic;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.core.magic.Affinity;
import com.minelittlepony.unicopia.core.magic.ICaster;
import com.minelittlepony.unicopia.core.magic.IMagicEffect;
import com.minelittlepony.unicopia.core.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.core.util.projectile.ITossable;
import com.minelittlepony.unicopia.redux.entity.ProjectileEntity;
import com.minelittlepony.unicopia.redux.item.UItems;
import com.minelittlepony.unicopia.redux.util.projectile.IAdvancedProjectile;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Magic effects that can be thrown.
 */
public interface ITossedEffect extends IMagicEffect, ITossable<ICaster<?>> {

    @Override
    default SoundEvent getThrowSound(ICaster<?> caster) {
        return SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
    }

    /**
     * Gets the appearance to be used when projecting this spell.
     */
    default ItemStack getCastAppearance(ICaster<?> caster) {
        Item item = getAffinity() == Affinity.BAD ? UItems.curse : UItems.spell;

        return SpellRegistry.instance().enchantStack(new ItemStack(item), getName());
    }

    /**
     * Projects this spell.
     *
     * Returns the resulting projectile entity for customization (or null if on the client).
     */
    @Nullable
    default IAdvancedProjectile toss(ICaster<?> caster) {
        World world = caster.getWorld();

        Entity entity = caster.getOwner();

        world.playSound(null, entity.x, entity.y, entity.z, getThrowSound(caster), SoundCategory.NEUTRAL, 0.7F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (caster.isLocal()) {
            IAdvancedProjectile projectile = new ProjectileEntity(null, world, caster.getOwner());

            projectile.setItem(getCastAppearance(caster));
            projectile.setThrowDamage(getThrowDamage(caster));
            projectile.setOwner(caster.getOwner());
            projectile.setEffect(this);
            projectile.setHydrophobic();
            projectile.launch(entity, entity.pitch, entity.yaw, 0, 1.5F, 1);

            projectile.spawn(world);

            return projectile;
        }

        return null;
    }
}

package com.minelittlepony.unicopia.spell;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.EntityProjectile;
import com.minelittlepony.unicopia.init.UItems;
import com.minelittlepony.unicopia.tossable.ITossable;

import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * Magic effects that can be thrown.
 */
public interface ITossedEffect extends IMagicEffect, ITossable<ICaster<?>> {

    default SoundEvent getThrowSound(ICaster<?> caster) {
        return SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
    }

    /**
     * Gets the appearance to be used when projecting this spell.
     */
    default ItemStack getCastAppearance(ICaster<?> caster) {
        Item item = getAffinity() == SpellAffinity.BAD ? UItems.curse : UItems.spell;

        return SpellRegistry.instance().enchantStack(new ItemStack(item), getName());
    }

    /**
     * Projects this spell.
     *
     * Returns the resulting projectile entity for customization (or null if on the client).
     */
    @Nullable
    default EntityProjectile toss(ICaster<?> caster) {
        World world = caster.getWorld();

        Entity entity = caster.getOwner();

        world.playSound(null, entity.posX, entity.posY, entity.posZ, getThrowSound(caster), SoundCategory.NEUTRAL, 0.7F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));

        if (caster.isLocal()) {
            EntityProjectile projectile = new EntityProjectile(world, caster.getOwner());

            projectile.setItem(getCastAppearance(caster));
            projectile.setThrowDamage(getThrowDamage(caster));
            projectile.setOwner(caster.getOwner());
            projectile.setEffect(this);
            projectile.setHydrophobic();
            projectile.shoot(entity, entity.rotationPitch, entity.rotationYaw, 0, 1.5F, 1);

            world.spawnEntity(projectile);

            return projectile;
        }

        return null;
    }
}

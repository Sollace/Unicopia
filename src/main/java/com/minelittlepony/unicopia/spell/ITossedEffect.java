package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.entity.EntityProjectile;
import com.minelittlepony.unicopia.init.UItems;
import com.minelittlepony.unicopia.tossable.ITossable;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public interface ITossedEffect extends IMagicEffect, ITossable<ICaster<?>> {

    default void toss(ICaster<?> caster) {
        World world = caster.getWorld();

        Entity entity = caster.getOwner();

        world.playSound(null, entity.posX, entity.posY, entity.posZ, getThrowSound(caster), SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote) {
            EntityProjectile projectile = new EntityProjectile(world, caster.getOwner());

            Item item = this.getAffinity() == SpellAffinity.BAD ? UItems.curse : UItems.spell;

            projectile.setItem(SpellRegistry.instance().enchantStack(new ItemStack(item), getName()));
            projectile.setThrowDamage(getThrowDamage(caster));
            projectile.setOwner(caster.getOwner());
            projectile.setEffect(this);
            projectile.setHydrophobic();
            projectile.shoot(entity, entity.rotationPitch, entity.rotationYaw, 0, 1.5F, 1);

            world.spawnEntity(projectile);
        }
    }
}

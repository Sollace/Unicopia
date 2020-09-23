package com.minelittlepony.unicopia.ability.magic;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.UEntities;
import com.minelittlepony.unicopia.ability.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.Projectile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Magic effects that can be thrown.
 */
public interface ThrowableSpell extends Spell {

    /**
     * Called once the projectile lands either hitting the ground or an entity.
     */
    void onImpact(Caster<?> caster, BlockPos pos, BlockState state);

    /**
     * The amount of damage to be dealt when the projectile collides with an entity.
     */
    default int getThrowDamage(Caster<?> stack) {
        return 0;
    }

    /**
     * The sound made when thrown.
     */
    default SoundEvent getThrowSound(Caster<?> caster) {
        return SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
    }

    /**
     * Gets the appearance to be used when projecting this spell.
     */
    default ItemStack getCastAppearance(Caster<?> caster) {
        Item item = getAffinity() == Affinity.BAD ? Items.MAGMA_CREAM : Items.SNOWBALL;

        return SpellRegistry.instance().enchantStack(new ItemStack(item), getName());
    }

    /**
     * Projects this spell.
     *
     * Returns the resulting projectile entity for customization (or null if on the client).
     */
    @Nullable
    default Projectile toss(Caster<?> caster) {
        World world = caster.getWorld();

        Entity entity = caster.getOwner();

        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), getThrowSound(caster), SoundCategory.NEUTRAL, 0.7F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (caster.isLocal()) {
            Projectile projectile = new MagicProjectileEntity(UEntities.THROWN_ITEM, world, caster.getOwner());

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

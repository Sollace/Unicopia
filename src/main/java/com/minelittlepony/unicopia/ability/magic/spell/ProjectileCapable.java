package com.minelittlepony.unicopia.ability.magic.spell;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.item.GemstoneItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Magic effects that can be thrown.
 */
public interface ProjectileCapable extends Spell, ProjectileDelegate {

    @Override
    default void onImpact(MagicProjectileEntity projectile, BlockPos pos, BlockState state) {
        if (!projectile.isClient()) {
            tick(projectile, Situation.PROJECTILE);
        }
    }

    @Override
    default void onImpact(MagicProjectileEntity projectile, Entity entity) {

    }

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
        return GemstoneItem.enchanted(UItems.GEMSTONE.getDefaultStack(), getType());
    }

    /**
     * Projects this spell.
     *
     * Returns the resulting projectile entity for customization (or null if on the client).
     */
    @Nullable
    default MagicProjectileEntity toss(Caster<?> caster) {
        World world = caster.getWorld();

        LivingEntity entity = caster.getMaster();

        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), getThrowSound(caster), SoundCategory.NEUTRAL, 0.7F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (!caster.isClient()) {
            MagicProjectileEntity projectile = new MagicProjectileEntity(world, entity);

            projectile.setItem(getCastAppearance(caster));
            projectile.setThrowDamage(getThrowDamage(caster));
            projectile.setSpell(this);
            projectile.setHydrophobic();
            projectile.setProperties(entity, entity.getPitch(), entity.getYaw(), 0, 1.5F, 1);

            world.spawnEntity(projectile);

            return projectile;
        }

        return null;
    }
}

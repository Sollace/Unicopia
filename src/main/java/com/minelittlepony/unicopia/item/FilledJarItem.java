package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.WorldEvents;

public class FilledJarItem extends ProjectileItem implements ProjectileDelegate.HitListener, ChameleonItem {
    public FilledJarItem(Item.Settings settings) {
        super(settings, 0);
    }

    @Override
    public SoundEvent getThrowSound(ItemStack stack) {
        return USounds.ENTITY_JAR_THROW;
    }

    @Override
    public Text getName(ItemStack stack) {
        return ChameleonItem.hasAppearance(stack) ? Text.translatable(getTranslationKey(stack), ChameleonItem.getAppearanceStack(stack).getName()) : UItems.EMPTY_JAR.getName(UItems.EMPTY_JAR.getDefaultStack());
    }

    @Override
    public boolean isFullyDisguised() {
        return false;
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        Entity entity = hit.getEntity();

        if (!entity.isAttackable() || !(projectile instanceof FlyingItemEntity)) {
            return;
        }

        ItemStack stack = ChameleonItem.getAppearanceStack(((FlyingItemEntity)projectile).getStack());

        boolean onFire = false;

        if (projectile.getWorld() instanceof ServerWorld world) {
            DamageSource damageSource = entity.getDamageSources().thrown(projectile, projectile.getOwner());

            float damage = EnchantmentHelper.getDamage(world, stack, entity, damageSource, projectile.getThrowDamage());

            if (projectile.getOwner() instanceof LivingEntity owner) {
                owner.onAttacking(entity);
            }

            if (entity.damage(damageSource, damage)) {

                if (entity instanceof LivingEntity living) {
                    projectile.knockback(living, damageSource, stack);
                    EnchantmentHelper.onTargetDamaged(world, living, damageSource, stack);
                }
            } else {
                if (onFire) {
                    entity.setOnFire(false);
                }
                projectile.deflect(ProjectileDeflection.SIMPLE, entity, projectile.getOwner(), false);
                projectile.setVelocity(projectile.getVelocity().multiply(0.2));
            }
        }
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile) {
        ItemStack stack = ChameleonItem.getAppearanceStack(projectile.getStack());

        if (stack.isOf(UItems.BUTTERFLY)) {
            ButterflyEntity butterfly = UEntities.BUTTERFLY.create(projectile.getWorld());
            butterfly.setVariant(ButterflyItem.getVariant(stack));
            butterfly.updatePosition(projectile.getX(), projectile.getY(), projectile.getZ());
            projectile.getWorld().spawnEntity(butterfly);
        } else {
            if (projectile.getWorld() instanceof ServerWorld sw) {
                stack.damage(1, sw, null, i -> {});
            }
            projectile.dropStack(stack);
        }
        projectile.getWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, projectile.getBlockPos(), Block.getRawIdFromState(Blocks.GLASS.getDefaultState()));
    }

    public ItemStack withContents(ItemStack contents) {
        return ChameleonItem.setAppearance(getDefaultStack(), contents);
    }
}

package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldEvents;

public class FilledJarItem extends ProjectileItem implements ProjectileDelegate.HitListener, ChameleonItem {
    public FilledJarItem(Settings settings) {
        super(settings, 0);
    }

    @Override
    public SoundEvent getThrowSound(ItemStack stack) {
        return USounds.ENTITY_JAR_THROW;
    }

    @Override
    public Text getName(ItemStack stack) {
        return hasAppearance(stack) ? Text.translatable(getTranslationKey(stack), getAppearanceStack(stack).getName()) : UItems.EMPTY_JAR.getName(UItems.EMPTY_JAR.getDefaultStack());
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

        ItemStack stack = getAppearanceStack(((FlyingItemEntity)projectile).getStack());

        boolean onFire = false;

        float prevHealth = 0.0F;
        int fire = EnchantmentHelper.getLevel(Enchantments.FIRE_ASPECT, stack);

        if (entity instanceof LivingEntity) {
            prevHealth = ((LivingEntity)entity).getHealth();

            if (fire > 0 && !entity.isOnFire()) {
                onFire = true;
                entity.setOnFireFor(1);
            }
        }

        float damage = EnchantmentHelper.getAttackDamage(stack, entity instanceof LivingEntity ? ((LivingEntity)entity).getGroup() : EntityGroup.DEFAULT);

        EntityAttributeInstance instance = new EntityAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE, i -> {});

        stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE).forEach(modifier -> {
            instance.addTemporaryModifier(modifier);
        });

        damage += instance.getValue();

        if (entity.damage(entity.getDamageSources().thrown(projectile, projectile.getOwner()), damage)) {

            int knockback = EnchantmentHelper.getLevel(Enchantments.KNOCKBACK, stack);

            final float toRadians = (float)Math.PI / 180F;

            if (entity instanceof LivingEntity living) {
                living.takeKnockback(
                        knockback / 2F,
                        MathHelper.sin(projectile.getYaw() * toRadians),
                       -MathHelper.cos(projectile.getYaw() * toRadians)
               );
                Living.updateVelocity(living);

                if (fire > 0) {
                    entity.setOnFireFor(fire * 4);
                }

                float healthDiff = prevHealth - ((LivingEntity)entity).getHealth();

                if (projectile.getWorld() instanceof ServerWorld && healthDiff > 2) {
                    ((ServerWorld)projectile.getWorld()).spawnParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getBodyY(0.5D), entity.getZ(), (int)(healthDiff / 2F), 0.1, 0, 0.1, 0.2);
                }
            } else {
                entity.addVelocity(
                       -MathHelper.sin(projectile.getYaw() * toRadians) * knockback / 2F, 0.1D,
                        MathHelper.cos(projectile.getYaw() * toRadians) * knockback / 2F
                );
            }
        } else {
            if (onFire) {
                entity.setOnFire(false);
            }
        }
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile) {
        ItemStack stack = getAppearanceStack(projectile.getStack());

        if (stack.isOf(UItems.BUTTERFLY)) {
            ButterflyEntity butterfly = UEntities.BUTTERFLY.create(projectile.getWorld());
            butterfly.setVariant(ButterflyItem.getVariant(stack));
            butterfly.updatePosition(projectile.getX(), projectile.getY(), projectile.getZ());
            projectile.getWorld().spawnEntity(butterfly);
        } else {
            stack.damage(1, projectile.getWorld().random, null);
            projectile.dropStack(stack);
        }
        projectile.getWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, projectile.getBlockPos(), Block.getRawIdFromState(Blocks.GLASS.getDefaultState()));
    }

    public ItemStack withContents(ItemStack contents) {
        return setAppearance(getDefaultStack(), contents);
    }
}

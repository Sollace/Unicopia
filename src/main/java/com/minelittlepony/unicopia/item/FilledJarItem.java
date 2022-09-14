package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.entity.IItemEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;

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
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldEvents;

public class FilledJarItem extends JarItem implements ChameleonItem {

    public FilledJarItem(Settings settings) {
        super(settings, false, false, false);
    }

    @Override
    public Text getName(ItemStack stack) {
        return hasAppearance(stack) ? new TranslatableText(getTranslationKey(stack), getAppearanceStack(stack).getName()) : UItems.EMPTY_JAR.getName(UItems.EMPTY_JAR.getDefaultStack());
    }

    @Override
    public boolean isFullyDisguised() {
        return false;
    }

    @Override
    public ActionResult onGroundTick(IItemEntity item) {
        return ActionResult.PASS;
    }

    @Override
    protected float getProjectileDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, Entity entity) {
        super.onImpact(projectile, entity);

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

        if (entity.damage(DamageSource.thrownProjectile(projectile, projectile.getOwner()), damage)) {

            int knockback = EnchantmentHelper.getLevel(Enchantments.KNOCKBACK, stack);

            final float toRadians = (float)Math.PI / 180F;

            if (entity instanceof LivingEntity) {
                ((LivingEntity)entity).takeKnockback(
                        knockback / 2F,
                        MathHelper.sin(projectile.getYaw() * toRadians),
                       -MathHelper.cos(projectile.getYaw() * toRadians)
               );

                if (fire > 0) {
                    entity.setOnFireFor(fire * 4);
                }

                float healthDiff = prevHealth - ((LivingEntity)entity).getHealth();

                if (projectile.world instanceof ServerWorld && healthDiff > 2) {
                    ((ServerWorld)projectile.world).spawnParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getBodyY(0.5D), entity.getZ(), (int)(healthDiff / 2F), 0.1, 0, 0.1, 0.2);
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
    protected void onImpact(MagicProjectileEntity projectile) {
        ItemStack stack = getAppearanceStack(projectile.getStack());
        stack.damage(1, projectile.world.random, null);
        projectile.dropStack(stack);
        projectile.world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, projectile.getBlockPos(), Block.getRawIdFromState(Blocks.GLASS.getDefaultState()));
    }

    public ItemStack withContents(ItemStack contents) {
        return setAppearance(getDefaultStack(), contents);
    }
}

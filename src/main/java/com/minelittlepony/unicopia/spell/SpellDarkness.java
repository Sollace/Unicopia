package com.minelittlepony.unicopia.spell;

import java.util.Random;

import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.util.MagicalDamageSource;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SpellDarkness extends AbstractSpell.RangedAreaSpell {

    final SoundEvent[] scarySounds = new SoundEvent[] {
            SoundEvents.AMBIENT_CAVE,
            SoundEvents.ENTITY_GHAST_SCREAM,
            SoundEvents.ENTITY_ZOMBIE_AMBIENT,
            SoundEvents.ENTITY_GHAST_AMBIENT,
            SoundEvents.ENTITY_SKELETON_AMBIENT,
            SoundEvents.ENTITY_SKELETON_SHOOT,
            SoundEvents.ENTITY_ENDERDRAGON_GROWL,
            SoundEvents.ENTITY_ENDERMEN_SCREAM,
            SoundEvents.ENTITY_ZOMBIE_VILLAGER_AMBIENT
    };

    @Override
    public String getName() {
        return "darkness";
    }

    @Override
    public int getTint() {
        return 0x000000;
    }

    @Override
    public boolean allowAI() {
        return true;
    }

    @Override
    public void onPlaced(ICaster<?> caster) {
        if (caster.getEntity() instanceof EntitySpell) {
            EntitySpell living = (EntitySpell)caster.getEntity();

            living.tasks.addTask(1, new EntityAIAvoidEntity<>(living, EntityPlayer.class, 3, 4, 4));
            living.height = 1.8F;

            living.setPosition(living.posX, living.posY, living.posZ);
        }
    }

    @Override
    public boolean update(ICaster<?> source) {
        int radius = 7 + (source.getCurrentLevel() * 3);

        if (source.getWorld().rand.nextInt(15) == 0) {
            source.getWorld().playSound(null, source.getOrigin(), getScarySoundEffect(source.getWorld().rand), SoundCategory.AMBIENT,
                    0.2F + source.getWorld().rand.nextFloat(), 0.3F);
        }
        source.findAllEntitiesInRange(radius * 1.5F)
            .filter(e -> e instanceof EntityLivingBase)
            .map(EntityLivingBase.class::cast)
            .forEach(e -> applyDarkness(source, e));

        return false;
    }

    protected void applyDarkness(ICaster<?> source, EntityLivingBase entity) {
        if (!CasterUtils.isHoldingEffect("light", entity)) {
            entity.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 3));

            Vec3d origin = source.getOriginVector();
            Vec3d to = entity.getPositionVector();
            double distance = origin.distanceTo(entity.getPositionVector());

            if (entity.world.rand.nextInt(30) == 0) {
                double appliedForce = distance / 10 + entity.world.rand.nextInt(3);
                Vec3d force = origin.subtract(to).normalize().scale(appliedForce);

                entity.addVelocity(force.x, force.y, force.z);
            }

            if (distance < 3 && entity.world.rand.nextInt(30) == 0) {
                entity.attackEntityFrom(MagicalDamageSource.create("darkness"), 3);
            }

        } else {
            if (entity.isPotionActive(MobEffects.BLINDNESS)) {
                entity.removeActivePotionEffect(MobEffects.BLINDNESS);
            }
        }
    }

    protected SoundEvent getScarySoundEffect(Random rand) {
        return scarySounds[rand.nextInt(scarySounds.length)];
    }

    @Override
    public void render(ICaster<?> source) {
        int radius = 7 + (source.getCurrentLevel() * 3);

        source.spawnParticles(new Sphere(false, radius), radius * 6, pos -> {
            if (!source.getWorld().isAirBlock(new BlockPos(pos).down())) {
                int size = source.getWorld().rand.nextInt(4);

                if (size > 0) {
                    double vX = (source.getWorld().rand.nextFloat() - 0.5) * 2;
                    double vZ = (source.getWorld().rand.nextFloat() - 0.5) * 2;

                    Particles.instance().spawnParticle(UParticles.SPHERE, false, pos, vX, 0, vZ, size, getTint(), 100);
                }
            }
        });
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.BAD;
    }
}

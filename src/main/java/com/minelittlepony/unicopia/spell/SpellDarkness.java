package com.minelittlepony.unicopia.spell;

import java.util.Random;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.util.MagicalDamageSource;
import com.minelittlepony.util.PosHelper;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SpellDarkness extends AbstractAttachableSpell {

    private static final AxisAlignedBB searchArea = new AxisAlignedBB(-5, -5, -5, 5, 5, 5);

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
        super.update(source);

        int soundChance = 15;

        int radius = 7 + (source.getCurrentLevel() * 3);

        if (hasTarget()) {
            soundChance *= 10;

            source.getEntity().motionY -= 0.01;

            applyBlocks(source, radius);
            applyEntities(source, radius, e -> applyLight(source, e));
        } else {
            applyEntities(source, radius, e -> applyDark(source, e));
        }

        if (source.getWorld().rand.nextInt(soundChance) == 0) {
            source.getWorld().playSound(null, source.getOrigin(),
                    getSoundEffect(source.getWorld().rand), SoundCategory.AMBIENT,
                    0.2F + source.getWorld().rand.nextFloat(), 0.3F);
        }


        return !getDead();
    }

    private void applyBlocks(ICaster<?> source, int radius) {
        for (BlockPos pos : PosHelper.getAllInRegionMutable(source.getOrigin(), new Sphere(false, radius))) {
            if (source.getWorld().rand.nextInt(500) == 0) {
                IBlockState state = source.getWorld().getBlockState(pos);

                if (state.getBlock() instanceof IGrowable) {
                    IGrowable growable = (IGrowable)state.getBlock();

                    if (growable.canGrow(source.getWorld(), pos, state, source.getWorld().isRemote)) {
                        growable.grow(source.getWorld(), source.getWorld().rand, pos, state);

                        return;
                    }
                }
            }
        }
    }

    private void applyEntities(ICaster<?> source, int radius, Consumer<EntityLivingBase> consumer) {
        source.findAllEntitiesInRange(radius * 1.5F)
                .filter(e -> e instanceof EntityLivingBase)
                .map(EntityLivingBase.class::cast)
                .forEach(consumer);;
    }

    private void applyLight(ICaster<?> source, EntityLivingBase entity) {
        if (entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1);
        }
    }

    private void applyDark(ICaster<?> source, EntityLivingBase entity) {

        if (isAreaOccupied(source, entity.getPositionVector())) {
            return;
        }

        if (!isLightholder(entity)) {
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

    public SoundEvent getSoundEffect(Random rand) {

        if (hasTarget()) {
            return SoundEvents.BLOCK_NOTE_CHIME;
        }

        return scarySounds[rand.nextInt(scarySounds.length)];
    }

    @Override
    public void render(ICaster<?> source) {
        int radius = 7 + (source.getCurrentLevel() * 3);

        boolean tamed = hasTarget();

        int tint = tamed ? 0xFFFFFF : getTint();

        if (tamed) {
            radius /= 3;
        }

        IShape shape = new Sphere(false, radius);

        source.spawnParticles(shape, radius * 6, pos -> {
            spawnSphere(source, pos, tint, searching ? 4 : 2);
        });

        source.findAllEntitiesInRange(radius * 1.5F).filter(this::isLightholder).forEach(e -> {
            Vec3d pos = shape.computePoint(source.getWorld().rand).add(e.getPositionEyes(1));

            spawnSphere(source, pos, 0xFFFFFF, 1);
        });
    }

    public boolean isLightholder(Entity e) {
        if (e instanceof EntitySpell) {
            return true;
        }

        return e instanceof EntityLivingBase && CasterUtils.isHoldingEffect("light", (EntityLivingBase)e);
    }

    public boolean isAreaOccupied(ICaster<?> source, Vec3d pos) {
        if (source.getWorld().isAirBlock(new BlockPos(pos).down())) {
            return source.findAllSpells().anyMatch(spell -> {
                SpellShield effect = spell.getEffect(SpellShield.class, false);

                if (effect != null) {
                    return pos.distanceTo(spell.getOriginVector()) <= effect.getDrawDropOffRange(spell);
                }

                return false;
            });
        }

        return false;
    }

    public void spawnSphere(ICaster<?> source, Vec3d pos, int tint, int maxSize) {
        if (isAreaOccupied(source, pos)) {
            return;
        }

        float size = source.getWorld().rand.nextFloat() * maxSize;

        float particleSpeed = hasTarget() ? 0.3F : 0.5F;

        if (size > 0) {
            double vX = (source.getWorld().rand.nextFloat() - 0.5) * particleSpeed;
            double vZ = (source.getWorld().rand.nextFloat() - 0.5) * particleSpeed;

            Particles.instance().spawnParticle(UParticles.SPHERE, false, pos, vX, 0, vZ, (int)(size * 1000), tint, 100);
        }
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.BAD;
    }

    @Override
    protected AxisAlignedBB getSearchArea(ICaster<?> source) {
        return searchArea.offset(source.getOriginVector());
    }

    @Override
    protected boolean canTargetEntity(EntitySpell e) {
        return e.hasEffect() && "light".equals(e.getEffect().getName());
    }
}

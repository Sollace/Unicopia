package com.minelittlepony.unicopia.magic.spell;

import java.util.Random;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.CasterUtils;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class DarknessSpell extends AbstractAttachableSpell {

    private static final Box searchArea = new Box(-5, -5, -5, 5, 5, 5);

    final SoundEvent[] scarySounds = new SoundEvent[] {
            SoundEvents.AMBIENT_CAVE,
            SoundEvents.ENTITY_GHAST_SCREAM,
            SoundEvents.ENTITY_ZOMBIE_AMBIENT,
            SoundEvents.ENTITY_GHAST_AMBIENT,
            SoundEvents.ENTITY_SKELETON_AMBIENT,
            SoundEvents.ENTITY_SKELETON_SHOOT,
            SoundEvents.ENTITY_ENDER_DRAGON_GROWL,
            SoundEvents.ENTITY_ENDERMAN_SCREAM,
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
    public void onPlaced(Caster<?> caster) {
        if (caster.getEntity() instanceof SpellcastEntity) {
            SpellcastEntity living = (SpellcastEntity)caster.getEntity();

            living.getGoals().add(1, new FleeEntityGoal<>(living, PlayerEntity.class, 3, 4, 4));
            living.setPosition(living.x, living.y, living.z);
        }
    }

    @Override
    public boolean update(Caster<?> source) {
        super.update(source);

        int soundChance = 15;

        int radius = 7 + (source.getCurrentLevel() * 3);

        if (hasTarget()) {
            soundChance *= 10;

            Vec3d vel = source.getEntity().getVelocity();
            source.getEntity().setVelocity(vel.x, vel.y - 0.01, vel.z);

            applyBlocks(source, radius);
            applyEntities(source, radius, e -> applyLight(source, e));
        } else {
            applyEntities(source, radius, e -> applyDark(source, e));
        }

        if (source.getWorld().random.nextInt(soundChance) == 0) {
            source.getWorld().playSound(null, source.getOrigin(),
                    getSoundEffect(source.getWorld().random), SoundCategory.AMBIENT,
                    0.2F + source.getWorld().random.nextFloat(), 0.3F);
        }


        return !isDead();
    }

    private void applyBlocks(Caster<?> source, int radius) {
        for (BlockPos pos : PosHelper.getAllInRegionMutable(source.getOrigin(), new Sphere(false, radius))) {
            if (source.getWorld().random.nextInt(500) == 0) {
                BlockState state = source.getWorld().getBlockState(pos);

                if (state.getBlock() instanceof Fertilizable) {
                    Fertilizable growable = (Fertilizable)state.getBlock();

                    if (growable.canGrow(source.getWorld(), source.getWorld().random, pos, state)) {
                        growable.grow(source.getWorld(), source.getWorld().random, pos, state);

                        return;
                    }
                }
            }
        }
    }

    private void applyEntities(Caster<?> source, int radius, Consumer<LivingEntity> consumer) {
        source.findAllEntitiesInRange(radius * 1.5F)
                .filter(e -> e instanceof LivingEntity)
                .map(LivingEntity.class::cast)
                .forEach(consumer);
    }

    private void applyLight(Caster<?> source, LivingEntity entity) {
        if (entity.getHealth() < entity.getHealthMaximum()) {
            entity.heal(1);
        }
    }

    private void applyDark(Caster<?> source, LivingEntity entity) {

        if (isAreaOccupied(source, entity.getPosVector())) {
            return;
        }

        if (!isLightholder(entity)) {
            entity.addPotionEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 3));

            Vec3d origin = source.getOriginVector();
            Vec3d to = entity.getPosVector();
            double distance = origin.distanceTo(entity.getPosVector());

            if (entity.world.random.nextInt(30) == 0) {
                double appliedForce = distance / 10 + entity.world.random.nextInt(3);
                Vec3d force = origin.subtract(to).normalize().multiply(appliedForce);

                entity.addVelocity(force.x, force.y, force.z);
            }

            if (distance < 3 && entity.world.random.nextInt(30) == 0) {
                entity.damage(MagicalDamageSource.DARKNESS, 3);
            }

        } else {
            if (entity.hasStatusEffect(StatusEffects.BLINDNESS)) {
                entity.removePotionEffect(StatusEffects.BLINDNESS);
            }
        }
    }

    public SoundEvent getSoundEffect(Random rand) {

        if (hasTarget()) {
            return SoundEvents.BLOCK_NOTE_BLOCK_CHIME;
        }

        return scarySounds[rand.nextInt(scarySounds.length)];
    }

    @Override
    public void render(Caster<?> source) {
        int radius = 7 + (source.getCurrentLevel() * 3);

        boolean tamed = hasTarget();

        int tint = tamed ? 0xFFFFFF : 0x000000;

        if (tamed) {
            radius /= 3;
        }

        Shape shape = new Sphere(false, radius);

        source.spawnParticles(shape, radius * 6, pos -> {
            spawnSphere(source, pos, tint, searching ? 4 : 2);
        });

        source.findAllEntitiesInRange(radius * 1.5F).filter(this::isLightholder).forEach(e -> {
            Vec3d pos = shape.computePoint(source.getWorld().random).add(e.getPosVector().add(0, e.getEyeHeight(e.getPose()), 0));

            spawnSphere(source, pos, 0xFFFFFF, 1);
        });
    }

    public boolean isLightholder(Entity e) {
        if (e instanceof SpellcastEntity) {
            return true;
        }

        return e instanceof LivingEntity && CasterUtils.isHoldingEffect("light", e);
    }

    public boolean isAreaOccupied(Caster<?> source, Vec3d pos) {
        if (source.getWorld().isAir(new BlockPos(pos).down())) {
            return source.findAllSpellsInRange(100).anyMatch(spell -> {
                ShieldSpell effect = spell.getEffect(ShieldSpell.class, false);

                if (effect != null) {
                    return pos.distanceTo(spell.getOriginVector()) <= effect.getDrawDropOffRange(spell);
                }

                return false;
            });
        }

        return false;
    }

    public void spawnSphere(Caster<?> source, Vec3d pos, int tint, int maxSize) {
        if (isAreaOccupied(source, pos)) {
            return;
        }

        float size = source.getWorld().random.nextFloat() * maxSize;

        float particleSpeed = hasTarget() ? 0.3F : 0.5F;

        if (size > 0) {
            double vX = (source.getWorld().random.nextFloat() - 0.5) * particleSpeed;
            double vZ = (source.getWorld().random.nextFloat() - 0.5) * particleSpeed;

            source.getWorld().addParticle(UParticles.SPHERE,
                    pos.x, pos.y, pos.z,
                    vX, 0, vZ);//(int)(size * 1000), tint, 30
        }
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BAD;
    }

    @Override
    protected Box getSearchArea(Caster<?> source) {
        return searchArea.offset(source.getOriginVector());
    }

    @Override
    protected boolean canTargetEntity(SpellcastEntity e) {
        return e.hasEffect() && "light".equals(e.getEffect().getName());
    }
}

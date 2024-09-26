package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.projectile.MagicBeamEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.server.world.UGameRules;
import com.minelittlepony.unicopia.util.Lerp;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World.ExplosionSourceType;

/**
 * More powerful version of the vortex spell which creates a black hole.
 */
public class DarkVortexSpell extends AbstractSpell implements ProjectileDelegate.BlockHitListener {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.CHAOS, 5)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.STRENGTH, 70)
            .with(Trait.DARKNESS, 100)
            .build();

    private final DataTracker.Entry<Float> accumulatedMass = this.dataTracker.startTracking(TrackableDataType.FLOAT, 0F);

    private final TargetSelecter targetSelecter = new TargetSelecter(this).setFilter(this::isValidTarget).setTargetowner(true).setTargetAllies(true);

    private final Lerp radius = new Lerp(0);

    protected DarkVortexSpell(CustomisedSpellType<?> type) {
        super(type);
    }
    // 1. force decreases with distance: distance scale 1 -> 0
    // 2. max force (at dist 0) is taken from accumulated mass
    // 3. force reaches 0 at distance of drawDropOffRange

    private double getMass() {
        return 0.1F + accumulatedMass.get() / 10F;
    }

    public double getEventHorizonRadius() {
        return radius.getValue();
    }

    public double getDrawDropOffRange() {
        return getEventHorizonRadius() * 20;
    }

    private double getAttractiveForce(Caster<?> source, Entity target) {
        return AttractionUtils.getAttractiveForce(getMass(), getOrigin(source), target);
    }

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return method == CastingMethod.STAFF ? toThrowable() : toPlaceable();
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation == Situation.PROJECTILE) {
            return false;
        }

        if (situation == Situation.BODY) {
            return true;
        }

        Vec3d origin = getOrigin(source);
        double mass = getMass() * 0.1;
        double logarithm = 1 - (1D / (1 + (mass * mass)));
        radius.update((float)Math.max(0.01, logarithm * source.asWorld().getGameRules().getInt(UGameRules.MAX_DARK_VORTEX_SIZE)), 200L);

        if (source.asEntity().age % 20 == 0) {
            source.asWorld().playSound(null, source.getOrigin(), USounds.AMBIENT_DARK_VORTEX_ADDITIONS, SoundCategory.AMBIENT, 1, 1);
        }

        double eventHorizon = getEventHorizonRadius();

        if (source.isClient()) {
            if (eventHorizon > 0.3) {
                double range = eventHorizon * 2;
                source.spawnParticles(origin, new Sphere(false, range), 50, p -> {
                    source.addParticle(
                            new FollowingParticleEffect(UParticles.HEALTH_DRAIN, origin, 0.4F)
                            .withChild(source.asWorld().isAir(BlockPos.ofFloored(p)) ? ParticleTypes.SMOKE : ParticleTypes.CAMPFIRE_SIGNAL_SMOKE),
                            p,
                            Vec3d.ZERO
                    );
                });
            }

            if (source.asWorld().random.nextInt(300) == 0) {
                ParticleUtils.spawnParticle(source.asWorld(), LightningBoltParticleEffect.DEFAULT, origin, Vec3d.ZERO);
            }
        } else {
            if (eventHorizon > 2) {
                new Sphere(false, eventHorizon + 3).translate(origin).randomPoints(10, source.asWorld().random).forEach(i -> {
                    BlockPos pos = BlockPos.ofFloored(i);
                    if (!source.asWorld().isAir(pos)) {
                        new Sphere(false, 3).translate(i).getBlockPositions().forEach(p -> {
                            affectBlock(source, p, origin);
                        });
                        ParticleUtils.spawnParticle(source.asWorld(), new LightningBoltParticleEffect(true, 10, 6, 3, Optional.of(i)), getOrigin(source), Vec3d.ZERO);
                    }
                });
            }
        }


        for (Entity insideEntity : source.findAllEntitiesInRange(eventHorizon * 0.5F).toList()) {
            insideEntity.setVelocity(Vec3d.ZERO);
            Living.updateVelocity(insideEntity);

            if (insideEntity instanceof CastSpellEntity s && getType().isOn(insideEntity)) {
                setDead();
                s.getSpellSlot().clear();
                source.asWorld().createExplosion(source.asEntity(), origin.x, origin.y, origin.z, 12, ExplosionSourceType.NONE);
                source.asWorld().createExplosion(source.asEntity(), insideEntity.getX(), insideEntity.getY(), insideEntity.getZ(), 12, ExplosionSourceType.NONE);
                return false;
            }
        }
        targetSelecter.getEntities(source, getDrawDropOffRange()).forEach(i -> {
            try {
                affectEntity(source, i, origin);
            } catch (Throwable e) {
                Unicopia.LOGGER.error("Error updating radial effect", e);
            }
        });

        if (!source.subtractEnergyCost(0.01)) {
            setDead();
            source.asWorld().createExplosion(source.asEntity(), origin.x, origin.y, origin.z, 3, ExplosionSourceType.NONE);
        }

        return true;
    }


    @Override
    public void tickDying(Caster<?> source) {
        float m = accumulatedMass.get() - 0.8F;
        accumulatedMass.set(m);
        double mass = getMass() * 0.1;
        double logarithm = 1 - (1D / (1 + (mass * mass)));
        radius.update((float)Math.max(0.1, logarithm * source.asWorld().getGameRules().getInt(UGameRules.MAX_DARK_VORTEX_SIZE)), 200L);
        if (m < 1) {
            super.tickDying(source);
        }

        Vec3d origin = getOrigin(source);
        ParticleUtils.spawnParticle(source.asWorld(), ParticleTypes.SMOKE, origin, new Vec3d(0, 0.2F, 0));
        ParticleUtils.spawnParticle(source.asWorld(), ParticleTypes.SMOKE, origin, new Vec3d(0, -0.2F, 0));

        if (!source.isClient() && source.asWorld().getRandom().nextInt(10) == 0) {
            Block.dropStack(source.asWorld(), BlockPos.ofFloored(origin), (source.asWorld().getRandom().nextInt(75) == 0 ? Items.ANCIENT_DEBRIS : Items.IRON_NUGGET).getDefaultStack());
        }
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockHitResult hit) {
        if (!projectile.isClient() && projectile instanceof MagicBeamEntity source) {
            Vec3d pos = hit.getPos();
            projectile.getWorld().createExplosion(projectile, pos.x, pos.y, pos.z, 12, ExplosionSourceType.NONE);
            toPlaceable().tick(source, Situation.BODY);
        }
    }

    @Override
    public boolean isFriendlyTogether(Affine other) {
        return accumulatedMass.get() < 4;
    }

    private boolean isValidTarget(Caster<?> source, Entity entity) {
        return EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(entity) && getAttractiveForce(source, entity) > 0;
    }

    public Vec3d getOrigin(Caster<?> source) {
        return source.asEntity().getPos().add(0, getYOffset(), 0);
    }

    public double getYOffset() {
        return 2;
    }

    private boolean canAffect(Caster<?> source, BlockPos pos) {
        return source.canModifyAt(pos)
            && source.asWorld().getBlockState(pos).getHardness(source.asWorld(), pos) >= 0
            && !source.asWorld().getBlockState(pos).isIn(UTags.Blocks.CATAPULT_IMMUNE);
    }

    private void affectBlock(Caster<?> source, BlockPos pos, Vec3d origin) {
        if (!canAffect(source, pos)) {
            if (source.asWorld().getBlockState(pos).isOf(Blocks.BEDROCK)) {
                source.asWorld().setBlockState(pos, Blocks.BARRIER.getDefaultState());
            }
            return;
        }
        if (pos.isWithinDistance(origin, getEventHorizonRadius())) {
            source.asWorld().breakBlock(pos, false);
            updateStatePostRemoval(source, pos);
        } else {
            CatapultSpell.createBlockEntity(source.asWorld(), pos, e -> {
                updateStatePostRemoval(source, pos);
                e.addVelocity(0, 0.1, 0);
            });
        }
    }

    private void updateStatePostRemoval(Caster<?> source, BlockPos pos) {
        if (!source.asWorld().getFluidState(pos).isEmpty()) {
            source.asWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    private void affectEntity(Caster<?> source, Entity target, Vec3d origin) {
        double distance = target.getPos().distanceTo(origin);
        double eventHorizonRadius = getEventHorizonRadius();

        if (distance <= eventHorizonRadius + 0.5) {
            target.setVelocity(target.getVelocity().multiply(distance < 1 ? distance : distance / (2 * eventHorizonRadius)));
            Living.updateVelocity(target);

            @Nullable
            Entity master = source.getMaster();

            if (target instanceof MagicProjectileEntity projectile) {
                Item item = projectile.getStack().getItem();
                if (item instanceof ProjectileDelegate.EntityHitListener p && master != null) {
                    p.onImpact(projectile, new EntityHitResult(master));
                }
            } else if (target instanceof PersistentProjectileEntity) {
                if (master != null) {
                    master.damage(master.getDamageSources().thrown(target, ((PersistentProjectileEntity)target).getOwner()), 4);
                }
                target.discard();
                return;
            }

            double massOfTarget = AttractionUtils.getMass(target);

            if (!source.isClient() && massOfTarget != 0) {
                accumulatedMass.set((float)(accumulatedMass.get() + massOfTarget));
            }

            target.damage(source.damageOf(UDamageTypes.GAVITY_WELL_RECOIL, source), Integer.MAX_VALUE);
            if (!(target instanceof PlayerEntity)) {
                target.discard();
                source.asWorld().playSound(null, target.getBlockPos(), USounds.AMBIENT_DARK_VORTEX_MOOD, SoundCategory.AMBIENT, 2, 0.002F);
            }
            if (target.isAlive()) {
                target.damage(source.asEntity().getDamageSources().outOfWorld(), Integer.MAX_VALUE);
            }

            source.subtractEnergyCost(-massOfTarget * 10);

            if (target instanceof PlayerEntity && distance < eventHorizonRadius + 5) {
                source.asWorld().playSound(null, target.getBlockPos(), USounds.AMBIENT_DARK_VORTEX_MOOD, SoundCategory.AMBIENT, 2, 0.02F);
            }

        } else {
            double force = getAttractiveForce(source, target);

            AttractionUtils.applyForce(origin, target, -force, 0, true);
        }
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        super.toNBT(compound, lookup);
        compound.putFloat("accumulatedMass", accumulatedMass.get());
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        super.fromNBT(compound, lookup);
        accumulatedMass.set(compound.getFloat("accumulatedMass"));
    }
}

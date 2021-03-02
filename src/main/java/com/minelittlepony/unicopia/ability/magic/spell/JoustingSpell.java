package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleHandle;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;

public class JoustingSpell extends AbstractSpell implements Attached {

    private final int rad = 5;
    private final Shape effect_range = new Sphere(false, rad);

    private final ParticleHandle particlEffect = new ParticleHandle();

    private int age;

    protected JoustingSpell(SpellType<?> type, Affinity affinity) {
        super(type, affinity);
    }

    @Override
    public void setDead() {
        super.setDead();
        particlEffect.destroy();
    }

    @Override
    public boolean onBodyTick(Caster<?> source) {
        if (source.isClient()) {
            particlEffect.ifAbsent(source, spawner -> {
                spawner.addParticle(UParticles.RAINBOOM_TRAIL, source.getOriginVector(), Vec3d.ZERO);
                spawner.addParticle(new OrientedBillboardParticleEffect(UParticles.RAINBOOM_RING, source.getPhysics().getMotionAngle()), source.getOriginVector(), Vec3d.ZERO);
            }).ifPresent(p -> p.attach(source));
        }

        LivingEntity owner = source.getMaster();

        source.findAllEntitiesInRange(rad).forEach(e -> {
            e.damage(MagicalDamageSource.create("rainboom", owner), 6);
        });
        PosHelper.getAllInRegionMutable(source.getOrigin(), effect_range).forEach(pos -> {
            BlockState state = source.getWorld().getBlockState(pos);
            if (state.isIn(UTags.FRAGILE) && canBreak(pos, owner)) {
                owner.world.breakBlock(pos, true);
            }
        });

        Vec3d motion = source.getEntity().getRotationVec(1).multiply(1.5);
        Vec3d velocity = source.getEntity().getVelocity().add(motion);

        while (velocity.length() > 3) {
            velocity = velocity.multiply(0.6);
        }

        source.getEntity().setVelocity(velocity);
        if (source instanceof Pony) {
            ((Pony)source).getMagicalReserves().getEnergy().multiply(0.2F);
        }

        return !source.getEntity().removed && age++ < 90 + 7 * (source.getLevel().get() + 1);
    }

    private boolean canBreak(BlockPos pos, LivingEntity entity) {

        if (entity instanceof PlayerEntity) {
            return entity.world.canPlayerModifyAt((PlayerEntity)entity, pos);
        }

        return entity.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);
        compound.putInt("age", age);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        age = compound.getInt("age");
    }
}

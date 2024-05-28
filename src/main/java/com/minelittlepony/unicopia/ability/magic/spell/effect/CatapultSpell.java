package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.SpellAttributes;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.mixin.MixinFallingBlockEntity;
import com.minelittlepony.unicopia.projectile.MagicBeamEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Picks up and throws an entity or block.
 */
public class CatapultSpell extends AbstractSpell implements ProjectileDelegate.BlockHitListener, ProjectileDelegate.EntityHitListener {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 50)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.EARTH, 60)
            .with(Trait.STRENGTH, 50)
            .build();

    private static final float HORIZONTAL_VARIANCE = 0.25F;
    private static final float MAX_STRENGTH = 120;

    static void appendTooltip(CustomisedSpellType<? extends CatapultSpell> type, List<Text> tooltip) {
        float velocity = (float)(0.1F + (type.traits().get(Trait.STRENGTH, -MAX_STRENGTH, MAX_STRENGTH) - 40) / 16D);
        tooltip.add(SpellAttributes.of(SpellAttributes.VERTICAL_VELOCITY, velocity));
        int hoverDuration = 50 + (int)type.traits().get(Trait.AIR, 0, 10) * 20;
        tooltip.add(SpellAttributes.ofTime(Unicopia.id("hang_time"), hoverDuration));
        float power = 1 + type.traits().get(Trait.POWER, 0, 10) / 10F;
        tooltip.add(SpellAttributes.of(Unicopia.id("pushing_power"), power));
    }

    protected CatapultSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockHitResult hit) {
        if (!projectile.isClient() && projectile instanceof MagicBeamEntity source && source.canModifyAt(hit.getBlockPos())) {
            createBlockEntity(projectile.getWorld(), hit.getBlockPos(), e -> {
                e.setOnGround(true);
                apply(source, e);
                e.setOnGround(false);
            });
        }
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        if (!projectile.isClient() && projectile instanceof MagicBeamEntity source) {
            apply(source, hit.getEntity());
        }
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        if (situation == Situation.PROJECTILE) {
            return true;
        }

        getTypeAndTraits().create().toThrowable().throwProjectile(caster);
        setDead();
        return isDead();
    }

    protected void apply(Caster<?> caster, Entity e) {

        float power = 1 + getTraits().get(Trait.POWER, 0, 10) / 10F;

        if (!e.isOnGround()) {
            e.setVelocity(caster.asEntity().getVelocity().multiply(power));
        } else {
            Random rng = caster.asWorld().random;
            double launchSpeed = 0.1F + (getTraits().get(Trait.STRENGTH, -MAX_STRENGTH, MAX_STRENGTH) - 40) / 16D;
            e.addVelocity(
                rng.nextTriangular(0, HORIZONTAL_VARIANCE) * 0.1F,
                launchSpeed,
                rng.nextTriangular(0, HORIZONTAL_VARIANCE) * 0.1F
            );

            if (e instanceof LivingEntity l) {
                int hoverDuration = 50 + (int)getTraits().get(Trait.AIR, 0, 10) * 20;

                if (l.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                    l.removeStatusEffect(StatusEffects.SLOW_FALLING);
                }
                l.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, hoverDuration, 1));
            }
        }

        e.velocityDirty = true;
        e.velocityModified = true;
    }

    static void createBlockEntity(World world, BlockPos bpos, @Nullable Consumer<Entity> apply) {

        if (world.isAir(bpos)) {
            return;
        }

        BlockState state = world.getBlockState(bpos);
        if (state.isIn(UTags.Blocks.CATAPULT_IMMUNE)) {
            return;
        }

        Vec3d pos = Vec3d.ofBottomCenter(bpos);
        FallingBlockEntity e = MixinFallingBlockEntity.createInstance(world, pos.x, pos.y, pos.z, world.getBlockState(bpos));
        world.removeBlock(bpos, true);
        e.setOnGround(false);
        e.timeFalling = Integer.MIN_VALUE;
        e.setHurtEntities(1 + (world.random.nextFloat() * 10), 100);

        if (apply != null) {
            apply.accept(e);
        }
        world.spawnEntity(e);
    }
}

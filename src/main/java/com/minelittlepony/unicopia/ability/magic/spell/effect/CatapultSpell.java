package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.SpellAttributes;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.Affects;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
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
import net.minecraft.util.math.MathHelper;
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

    private static final SpellAttribute<Float> LAUNCH_SPEED = SpellAttribute.create(SpellAttributes.VERTICAL_VELOCITY, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.STRENGTH, strength -> 0.1F + (MathHelper.clamp(strength, -MAX_STRENGTH, MAX_STRENGTH) - 40) / 16F);
    private static final SpellAttribute<Float> HANG_TIME = SpellAttribute.create(SpellAttributes.HANG_TIME, AttributeFormat.TIME, AttributeFormat.PERCENTAGE, Trait.AIR, air -> 50 + (int)MathHelper.clamp(air, 0, 10) * 20F);
    private static final SpellAttribute<Float> PUSHING_POWER = SpellAttribute.create(SpellAttributes.PUSHING_POWER, AttributeFormat.REGULAR, Trait.POWER, power -> 1 + MathHelper.clamp(power, 0, 10) / 10F);
    private static final SpellAttribute<Boolean> CAUSES_LEVITATION = SpellAttribute.createConditional(SpellAttributes.CAUSES_LEVITATION, Trait.FOCUS, focus -> focus > 50);
    private static final SpellAttribute<Affects> AFFECTS = SpellAttribute.createEnumerated(SpellAttributes.AFFECTS, Trait.ORDER, order -> {
        if (order <= 0) {
            return Affects.BOTH;
        } else if (order <= 10) {
            return Affects.ENTITIES;
        }
        return Affects.BLOCKS;
    });
    static final TooltipFactory TOOLTIP = TooltipFactory.of(LAUNCH_SPEED, HANG_TIME, PUSHING_POWER, CAUSES_LEVITATION, AFFECTS);

    static void appendTooltip(CustomisedSpellType<? extends CatapultSpell> type, List<Text> tooltip) {
        TOOLTIP.appendTooltip(type, tooltip);
    }

    protected CatapultSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockHitResult hit) {
        if (!AFFECTS.get(getTraits()).allowsBlocks()) {
            return;
        }

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
            Entity e = hit.getEntity();
            if (!(e instanceof FallingBlockEntity) && !AFFECTS.get(getTraits()).allowsEntities()) {
                return;
            }

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
            e.addVelocity(
                rng.nextTriangular(0, HORIZONTAL_VARIANCE) * 0.1F,
                LAUNCH_SPEED.get(getTraits()),
                rng.nextTriangular(0, HORIZONTAL_VARIANCE) * 0.1F
            );

            int hoverDuration = HANG_TIME.get(getTraits()).intValue();
            boolean noGravity = CAUSES_LEVITATION.get(getTraits());

            if (e instanceof LivingEntity l) {
                if (l.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                    l.removeStatusEffect(StatusEffects.SLOW_FALLING);
                }
                l.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, hoverDuration, 1));
            }

            if (noGravity || e instanceof FallingBlockEntity && (!e.getWorld().getBlockState(e.getBlockPos().up()).isReplaceable())) {
                if (e instanceof LivingEntity l) {
                    l.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 200, 1));
                } else {
                    e.setNoGravity(true);
                }
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

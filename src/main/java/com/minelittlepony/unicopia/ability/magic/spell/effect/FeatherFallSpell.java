package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.List;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FeatherFallSpell extends AbstractSpell implements TimedSpell {
    private static final int MIN_RANGE = 1;
    private static final int MAX_RANGE = 20;
    private static final int MIN_TARGETS = 1;
    private static final int MAX_TARGETS = 19;

    private static final float FOCUS_RANGE_WEIGHT = 0.1F;
    private static final float POWERS_RANGE_WEIGHT = 0.3F;
    private static final float MAX_GENEROSITY_FACTOR = 19F;

    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 80)
            .with(Trait.POWER, 10)
            .with(Trait.AIR, 0.1F)
            .with(Trait.KINDNESS, 90)
            .with(Trait.ORDER, 15)
            .build();

    private final Timer timer;

    protected FeatherFallSpell(CustomisedSpellType<?> type) {
        super(type);
        timer = new Timer(10 + (int)(getTraits().get(Trait.FOCUS, 0, 160)));
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        timer.tick();

        if (timer.getTicksRemaining() <= 0) {
            return false;
        }

        setDirty();

        List<Entity> targets = getTargets(caster).toList();

        if (targets.isEmpty()) {
            return true;
        }

        final float strength = 1F / (getTraits().get(Trait.STRENGTH, 2, 9) / targets.size());
        final float generosity = getTraits().get(Trait.GENEROSITY, 1, MAX_GENEROSITY_FACTOR) / MAX_GENEROSITY_FACTOR;

        Entity entity = caster.getEntity();
        Vec3d masterVelocity = caster.getEntity().getVelocity().multiply(0.1);
        targets.forEach(target -> {
            if (target.getVelocity().y < 0) {

                boolean isSelf = caster.isOwnedBy(target) || target == entity;
                float delta = strength * (isSelf ? (1F - generosity) : generosity);

                if (!isSelf || generosity < 0.5F) {
                    target.verticalCollision = true;
                    target.setOnGround(true);
                    target.fallDistance = 0;
                }
                if (target instanceof PlayerEntity) {
                    ((PlayerEntity)target).getAbilities().flying = false;
                }
                target.setVelocity(target.getVelocity().multiply(1, delta, 1));
                if (situation == Situation.PROJECTILE && target != caster.getEntity()) {
                    target.addVelocity(masterVelocity.x, 0, masterVelocity.z);
                }
            }
            ParticleUtils.spawnParticles(new MagicParticleEffect(getType().getColor()), target, 7);
        });

        return caster.subtractEnergyCost(timer.getTicksRemaining() % 50 == 0 ? getCostPerEntity() * targets.size() : 0);
    }

    protected double getCostPerEntity() {
        float focus = Math.max(getTraits().get(Trait.FOCUS), 80) - 80;
        float power = Math.max(getTraits().get(Trait.POWER), 10) - 10;

        return MathHelper.clamp((power * POWERS_RANGE_WEIGHT) - (focus * FOCUS_RANGE_WEIGHT), 1, 7);
    }

    protected double getEffectRange() {
        float power = getTraits().get(Trait.POWER) - 10;

        return MathHelper.clamp(power * POWERS_RANGE_WEIGHT, MIN_RANGE, MAX_RANGE);
    }

    protected long getMaxTargets() {
        long generosity = (long)getTraits().get(Trait.GENEROSITY) * 2L;
        long focus = (long)getTraits().get(Trait.FOCUS, MIN_TARGETS, MAX_TARGETS) * 2L;
        return generosity + focus;
    }

    protected Stream<Entity> getTargets(Caster<?> caster) {
        Entity owner = caster.getEntity();// , EquinePredicates.IS_PLAYER
        return Stream.concat(Stream.of(owner), caster.findAllEntitiesInRange(getEffectRange()).sorted((a, b) -> {
            return Integer.compare(
                    FriendshipBraceletItem.isComrade(caster, a) ? 1 : 0,
                    FriendshipBraceletItem.isComrade(caster, b) ? 1 : 0
            );
        }).distinct()).limit(getMaxTargets());
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        timer.toNBT(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        timer.fromNBT(compound);
    }
}

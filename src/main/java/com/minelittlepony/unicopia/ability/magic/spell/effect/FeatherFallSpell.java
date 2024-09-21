package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.List;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.AttributeFormat;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
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

    private static final SpellAttribute<Integer> DURATION = SpellAttribute.create(SpellAttributeType.DURATION, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.FOCUS, focus -> 10 + (int)(MathHelper.clamp(focus, 0, 160)));
    private static final SpellAttribute<Float> STRENGTH = SpellAttribute.create(SpellAttributeType.STRENGTH, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.STRENGTH, strength -> MathHelper.clamp(strength, 2, 9));
    private static final SpellAttribute<Float> RANGE = SpellAttribute.create(SpellAttributeType.RANGE, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.POWER, power -> MathHelper.clamp((power - 10) * POWERS_RANGE_WEIGHT, MIN_RANGE, MAX_RANGE));
    private static final SpellAttribute<Long> SIMULTANIOUS_TARGETS = SpellAttribute.create(SpellAttributeType.SIMULTANIOUS_TARGETS, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.GENEROSITY, (traits, generosity) -> {
        return (long)(generosity + traits.get(Trait.FOCUS, MIN_TARGETS, MAX_TARGETS) * 2);
    });
    private static final SpellAttribute<Float> COST_PER_INDIVIDUAL = SpellAttribute.create(SpellAttributeType.COST_PER_INDIVIDUAL, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.POWER, (traits, power) -> {
        return MathHelper.clamp(((Math.max(power, 10) - 10) * POWERS_RANGE_WEIGHT) - ((Math.max(traits.get(Trait.FOCUS), 80) - 80) * FOCUS_RANGE_WEIGHT), 1, 7);
    });
    private static final SpellAttribute<Float> TARGET_PREFERENCE = SpellAttribute.create(SpellAttributeType.TARGET_PREFERENCE, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.GENEROSITY, generosity -> {
        return MathHelper.clamp(generosity, 1, MAX_GENEROSITY_FACTOR) / MAX_GENEROSITY_FACTOR;
    });
    private static final SpellAttribute<Float> CASTER_PREFERENCE = SpellAttribute.create(SpellAttributeType.CASTER_PREFERENCE, AttributeFormat.REGULAR, AttributeFormat.PERCENTAGE, Trait.GENEROSITY, (traits, generosity) -> {
        return 1 - TARGET_PREFERENCE.get(traits);
    });
    private static final SpellAttribute<Boolean> NEGATES_FALL_DAMAGE = SpellAttribute.createConditional(SpellAttributeType.NEGATES_FALL_DAMAGE, Trait.GENEROSITY, (generosity) -> generosity > 0.5F);

    static final TooltipFactory TOOLTIP = TooltipFactory.of(DURATION, STRENGTH, RANGE, SIMULTANIOUS_TARGETS, COST_PER_INDIVIDUAL, TARGET_PREFERENCE, CASTER_PREFERENCE, NEGATES_FALL_DAMAGE);

    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.FOCUS, 80)
            .with(Trait.POWER, 10)
            .with(Trait.AIR, 0.1F)
            .with(Trait.KINDNESS, 90)
            .with(Trait.ORDER, 15)
            .build();

    private final Timer timer = new Timer(DURATION.get(getTraits()));

    protected FeatherFallSpell(CustomisedSpellType<?> type) {
        super(type);
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

        List<Entity> targets = getTargets(caster).toList();

        if (targets.isEmpty()) {
            return true;
        }

        final float strength = 1F / (STRENGTH.get(getTraits()) / targets.size());
        final float targetPreference = TARGET_PREFERENCE.get(getTraits());
        final float casterPreference = 1 - targetPreference;
        final boolean negateFallDamage = NEGATES_FALL_DAMAGE.get(getTraits());

        Entity entity = caster.asEntity();
        Vec3d masterVelocity = entity.getVelocity().multiply(0.1);
        targets.forEach(target -> {
            if (target.getVelocity().y < 0) {
                if (negateFallDamage) {
                    target.verticalCollision = true;
                    target.setOnGround(true);
                    target.fallDistance = 0;
                }
                if (target instanceof PlayerEntity) {
                    ((PlayerEntity)target).getAbilities().flying = false;
                }

                float delta = strength * ((caster.isOwnedBy(target) || target == entity) ? casterPreference : targetPreference);
                target.setVelocity(target.getVelocity().multiply(1, delta, 1));
                if (situation == Situation.PROJECTILE && target != entity) {
                    target.addVelocity(masterVelocity.x, 0, masterVelocity.z);
                }
            }
            ParticleUtils.spawnParticles(new MagicParticleEffect(getType().getColor()), target, 7);
        });

        return caster.subtractEnergyCost(timer.getTicksRemaining() % 50 == 0 ? COST_PER_INDIVIDUAL.get(getTraits()) * targets.size() : 0);
    }

    protected Stream<Entity> getTargets(Caster<?> caster) {
        return Stream.concat(Stream.of(caster.asEntity()), caster.findAllEntitiesInRange(RANGE.get(getTraits())).sorted((a, b) -> {
            return Integer.compare(
                    FriendshipBraceletItem.isComrade(caster, a) ? 1 : 0,
                    FriendshipBraceletItem.isComrade(caster, b) ? 1 : 0
            );
        }).distinct()).limit(SIMULTANIOUS_TARGETS.get(getTraits()));
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

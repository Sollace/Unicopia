package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.*;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation.Recipient;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World.ExplosionSourceType;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

/**
 * Internal.
 * <p>
 * Used by the Rage ability.
 */
public class RageAbilitySpell extends AbstractSpell {
    private int age;
    private int ticksExtenguishing;

    public RageAbilitySpell(CustomisedSpellType<?> type) {
        super(type);
        setHidden(true);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation != Situation.BODY || source.asEntity().isRemoved()) {
            return false;
        }

        if (source.asEntity().isInsideWaterOrBubbleColumn()) {
            ticksExtenguishing++;
            source.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 1);
            source.spawnParticles(ParticleTypes.CLOUD, 12);
            setDirty();
        } else {
            ticksExtenguishing = 0;
        }

        if (ticksExtenguishing > 10) {
            return false;
        }

        BlockPos pos = source.getOrigin();

        if (!source.isClient()) {
            if (age == 0) {
                source.asWorld().createExplosion(source.asEntity(), source.damageOf(DamageTypes.FIREBALL), new ExplosionBehavior(){
                    @Override
                    public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
                        return false;
                    }
                }, source.getOriginVector(), 0, true, ExplosionSourceType.MOB);

                if (source instanceof Pony pony) {
                    pony.setAnimation(Animation.ARMS_UP, Recipient.ANYONE, 12);
                }
                source.playSound(SoundEvents.ENTITY_POLAR_BEAR_WARNING, 2F, 0.1F);
            }

            if (source.asEntity().isOnGround() && source.asWorld().isAir(pos) && age % 10 == 0) {
                source.asWorld().setBlockState(pos, Blocks.FIRE.getDefaultState());
            }

            if (source instanceof Pony pony) {
                if (pony.asEntity().getAttackCooldownProgress(0) == 0) {
                    LivingEntity adversary = pony.asEntity().getPrimeAdversary();
                    if (adversary != null) {
                        adversary.setOnFireFor(10);
                    }
                }
            }
        } else {
            if (age % 5 == 0) {
                source.spawnParticles(ParticleTypes.LAVA, 4);
                source.subtractEnergyCost(Math.min(12, 3 + source.asEntity().getVelocity().length() * 0.1));
            }
        }

        if (source instanceof Pony pony) {
            if (source.isClient() && pony.asEntity().getAttackCooldownProgress(0) == 0) {
                InteractionManager.instance().playLoopingSound(source.asEntity(), InteractionManager.SOUND_KIRIN_RAGE, source.asWorld().random.nextLong());
            }
            Bar energyBar = pony.getMagicalReserves().getEnergy();
            var energy = Math.min(1.01F, 0.5F + (age / 1000F));
            float newEnergy = energyBar.get() + energy;
            energyBar.set(Math.min(1.9F + MathHelper.sin(age / 3F) * 1.7F, newEnergy));
            pony.getMagicalReserves().getMana().add(-1);
            if (pony.getMagicalReserves().getMana().get() <= 0) {
                return false;
            }
        }

        if (source.asWorld().hasRain(pos.up()) && source.asWorld().random.nextInt(15) == 0) {
            source.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.3F);
            source.spawnParticles(ParticleTypes.CLOUD, 3);
        }

        age++;
        source.asEntity().setInvulnerable(age < 25);


        setDirty();
        return true;
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putInt("age", age);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        age = compound.getInt("age");
    }
}

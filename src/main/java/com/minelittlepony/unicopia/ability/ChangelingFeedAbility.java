package com.minelittlepony.unicopia.ability;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.ChangelingFeedingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.TraceHelper;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;

/**
 * Changeling ability to restore health from mobs
 */
public class ChangelingFeedAbility implements Ability<Hit> {
    private static final Predicate<Entity> TARGET_PREDICATE = e -> (e instanceof LivingEntity)
            && (e instanceof CowEntity
            || e instanceof MerchantEntity
            || e instanceof PlayerEntity
            || e instanceof SheepEntity
            || e instanceof PigEntity
            || e instanceof HostileEntity);

    @Override
    public int getWarmupTime(Pony player) {
        return 5;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return !SpellType.FEED.isOn(player) && ChangelingFeedingSpell.canFeed(player) ? 15 : 80;
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.of(ChangelingFeedingSpell.canFeed(player) && !getTargets(player).findAny().isEmpty());
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public boolean apply(Pony iplayer, Hit data) {
        if (!ChangelingFeedingSpell.canFeed(iplayer)) {
            return false;
        }

        PlayerEntity player = iplayer.asEntity();

        float maximumHealthGain = player.getMaxHealth() - player.getHealth();
        int maximumFoodGain = player.canConsume(false) ? (20 - player.getHungerManager().getFoodLevel()) : 0;

        if (maximumHealthGain > 0 || maximumFoodGain > 0) {
            List<LivingEntity> targets = getTargets(iplayer).map(LivingEntity.class::cast).toList();

            if (targets.size() > 0) {
                new ChangelingFeedingSpell(targets, maximumHealthGain, maximumFoodGain).apply(iplayer);

                iplayer.playSound(USounds.ENTITY_PLAYER_CHANGELING_FEED, 0.1F, iplayer.getRandomPitch());
                return true;
            }
        }

        iplayer.playSound(USounds.Vanilla.ENTITY_PLAYER_BURP, 1, (float)player.getWorld().random.nextTriangular(1F, 0.2F));
        return true;
    }

    protected Stream<Entity> getTargets(Pony player) {
        return Stream.concat(
                VecHelper.findInRange(player.asEntity(), player.asWorld(), player.getOriginVector(), 3, TARGET_PREDICATE).stream(),
                TraceHelper.findEntity(player.asEntity(), 17, 1, TARGET_PREDICATE).stream()
        ).distinct();
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(6);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        if (player.asWorld().random.nextInt(10) == 0) {
            player.spawnParticles(ParticleTypes.HEART, 1);
        }
    }
}

package com.minelittlepony.unicopia.ability;

import java.util.Comparator;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UPOIs;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.poi.PointOfInterestStorage.OccupationStatus;

public class SeaponySonarPulseAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 10;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 100;
    }

    @Override
    public boolean canUse(Race race) {
        return race == Race.SEAPONY;
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.INSTANCE;
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 5;
    }

    @Override
    public boolean apply(Pony player, Hit data) {
        player.setAnimation(Animation.ARMS_UP, Animation.Recipient.ANYONE);

        for (Entity target : player.findAllEntitiesInRange(64, e -> {
            return (e instanceof LivingEntity && (e instanceof HostileEntity || ((LivingEntity)e).getGroup() == EntityGroup.AQUATIC)) && e.isSubmergedInWater();
        }).sorted(Comparator.comparing(e -> e.distanceTo(player.asEntity()))).toList()) {
            Vec3d offset = target.getPos().subtract(player.getOriginVector());
            float distance = target.distanceTo(player.asEntity());
            if (distance < 4) {
                float scale = 1 - (distance/10F);
                ((LivingEntity)target).takeKnockback(0.7 * scale, -offset.x, -offset.z);
                target.damage(target.getDamageSources().sonicBoom(player.asEntity()), 10 * scale);
            } else {
                emitPing(player, target.getPos(), 10, 1, 1.3F);
            }
        }
        player.subtractEnergyCost(5);

        if (player.asWorld() instanceof ServerWorld sw) {
            sw.getPointOfInterestStorage().getNearestPosition(
                    type -> type.value() == UPOIs.CHESTS,
                    pos -> player.asWorld().getFluidState(pos).isIn(FluidTags.WATER), player.getOrigin(), 64, OccupationStatus.ANY)
                .ifPresent(chestPos -> {
                    emitPing(player, chestPos.toCenterPos(), 20, 0.5F, 2F);
                });
        }

        player.playSound(USounds.ENTITY_PLAYER_SEAPONY_SONAR, 1);
        player.spawnParticles(UParticles.SHOCKWAVE, 1);
        player.asEntity().emitGameEvent(GameEvent.INSTRUMENT_PLAY);

        return true;
    }

    private void emitPing(Pony player, Vec3d pos, int delay, float volume, float pitch) {
        AwaitTickQueue.scheduleTask(player.asWorld(), w -> {
            ParticleUtils.spawnParticle(w, UParticles.SHOCKWAVE, pos, Vec3d.ZERO);
            float loudness = Math.max(0, 1.4F - (float)Math.log10(player.getOriginVector().distanceTo(pos)));
            w.playSound(null, pos.x, pos.y, pos.z, USounds.ENTITY_PLAYER_SEAPONY_SONAR, SoundCategory.AMBIENT, volume * loudness, pitch);
            w.emitGameEvent(player.asEntity(), GameEvent.INSTRUMENT_PLAY, pos);
        }, delay + (int)player.getOriginVector().distanceTo(pos));
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(6);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}

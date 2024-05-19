package com.minelittlepony.unicopia.entity.ai;

import java.util.Comparator;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public interface TargettingUtil {
    Comparator<PlayerEntity> FLYING_PREFERRED = Comparator.comparing(e -> Pony.of(e).getPhysics().isFlying() ? 0 : 1);

    @SuppressWarnings("unchecked")
    static <T extends LivingEntity> Stream<T> getTargets(Class<T> type, TargetPredicate predicate, LivingEntity subject, Box searchArea) {
        if (type == PlayerEntity.class || type == ServerPlayerEntity.class) {
            return (Stream<T>)subject.getWorld().getPlayers(predicate, subject, searchArea).stream();
        }
        return subject.getWorld().getTargets(type, predicate, subject, searchArea).stream();
    }

    static <T extends Entity> Comparator<T> nearestTo(LivingEntity subject) {
        Vec3d fromPos = subject.getEyePos();
        return Comparator.comparing(e -> fromPos.distanceTo(e.getPos()));
    }

    static Vec3d getProjectedPos(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            Vec3d velocity = Pony.of(player).getPhysics().getClientVelocity();
            System.out.println(velocity);
            return entity.getEyePos().add(velocity.multiply(1.5)).add(0, -1, 0);
        }
        return entity.getEyePos().add(entity.getVelocity());
    }
}

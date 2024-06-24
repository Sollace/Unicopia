package com.minelittlepony.unicopia.entity.effect;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureKeys;

public class SeaponyGraceStatusEffect {

    public static void update(LivingEntity entity) {
        if (entity.getWorld().isClient) {
            return;
        }
        if (EquinePredicates.PLAYER_SEAPONY.test(entity)) {
            if (!entity.hasStatusEffect(UEffects.SEAPONYS_GRACE) && !entity.hasStatusEffect(UEffects.SEAPONYS_IRE)) {
                entity.addStatusEffect(new StatusEffectInstance(UEffects.SEAPONYS_GRACE, StatusEffectInstance.INFINITE, 0, false, true));
            }
        } else if (entity.hasStatusEffect(UEffects.SEAPONYS_GRACE)) {
            entity.removeStatusEffect(UEffects.SEAPONYS_GRACE);
        }
    }

    public static boolean hasIre(LivingEntity entity, MobEntity enemy) {
        return enemy.getAttacker() == entity || hasIre(entity);
    }

    public static boolean hasGrace(LivingEntity entity) {
        boolean isSeapony = EquinePredicates.PLAYER_SEAPONY.test(entity);
        return isSeapony && entity.hasStatusEffect(UEffects.SEAPONYS_GRACE) && !entity.hasStatusEffect(UEffects.SEAPONYS_IRE);
    }

    public static boolean hasIre(LivingEntity entity) {
        return !EquinePredicates.PLAYER_SEAPONY.test(entity) || entity.hasStatusEffect(UEffects.SEAPONYS_IRE);
    }

    public static void processBlockChange(World world, PlayerEntity player, BlockPos pos, BlockState stateBroken, @Nullable BlockEntity blockEntity) {

        if (!(world instanceof ServerWorld sw)) {
            return;
        }

        if (!hasGrace(player)) {
            return;
        }

        if (!stateBroken.isIn(UTags.Blocks.ANGERS_GUARDIANS)) {
            return;
        }
        StructureStart start = sw.getStructureAccessor().getStructureContaining(pos, StructureKeys.MONUMENT);
        if (start.getStructure() == null) {
            return;
        }

        List<GuardianEntity> guardians = sw.getEntitiesByClass(GuardianEntity.class, player.getBoundingBox().expand(10), EntityPredicates.VALID_LIVING_ENTITY);

        if (guardians.size() > 0) {
            guardians.forEach(guardian -> {
                guardian.setTarget(player);
                guardian.playAmbientSound();
            });

            player.removeStatusEffect(UEffects.SEAPONYS_GRACE);
            player.addStatusEffect(new StatusEffectInstance(UEffects.SEAPONYS_IRE, 90000, 0, false, true));
            world.playSound(null, pos, USounds.Vanilla.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.PLAYERS, 1, 1);
        }
    }
}

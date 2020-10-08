package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Multi;
import com.minelittlepony.unicopia.entity.player.PlayerAttributes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.RayTraceHelper;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * A magic casting ability for unicorns.
 * (only shields for now)
 */
public class BatPonyHangAbility implements Ability<Multi> {

    @Override
    public int getWarmupTime(Pony player) {
        return 1;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 0;
    }

    @Override
    public boolean canUse(Race race) {
        return race == Race.BAT;
    }

    @Override
    public Multi tryActivate(Pony player) {

        if (player.isHanging()) {
            return new Multi(BlockPos.ZERO, 0);
        }

        BlockPos poss = RayTraceHelper.doTrace(player.getMaster(), 3, 1, EntityPredicates.EXCEPT_SPECTATOR).getBlockPos().orElse(null);
        if (poss != null) {
            boolean air = player.getWorld().isAir(poss.down()) && player.getWorld().isAir(poss.down(2));

            if (air && player.canHangAt(poss)) {
                return new Multi(poss, 1);
            }
        }

        return RayTraceHelper.doTrace(player.getMaster(), 5, 1, EntityPredicates.EXCEPT_SPECTATOR).getBlockPos()
                .map(BlockPos::down)
                .filter(pos -> player.getWorld().isAir(pos) && player.getWorld().isAir(pos.down()) && player.canHangAt(pos))
                .map(pos -> new Multi(pos, 1))
                .orElse(null);
    }

    @Override
    public Multi.Serializer<Multi> getSerializer() {
        return Multi.SERIALIZER;
    }

    @Override
    public void apply(Pony player, Multi data) {
        EntityAttributeInstance attr = player.getMaster().getAttributeInstance(PlayerAttributes.ENTITY_GRAVTY_MODIFIER);

        if (data.hitType == 0 && attr.hasModifier(PlayerAttributes.BAT_HANGING)) {
            attr.removeModifier(PlayerAttributes.BAT_HANGING);
            return;
        }

        if (data.hitType == 1 && player.canHangAt(data.pos())) {
            player.getMaster().teleport(data.x + 0.5, data.y - 2, data.z + 0.5);
            player.getMaster().setVelocity(Vec3d.ZERO);

            if (!attr.hasModifier(PlayerAttributes.BAT_HANGING)) {
                attr.addPersistentModifier(PlayerAttributes.BAT_HANGING);
            }
        }
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
    }
}

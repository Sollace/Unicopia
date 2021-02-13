package com.minelittlepony.unicopia.ability;

import java.util.stream.Stream;

import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;

/**
 * Unicorn teleport ability with friends
 */
public class UnicornGroupTeleportAbility extends UnicornTeleportAbility {

    @Override
    public double getCostEstimate(Pony player) {
        double cost = super.getCostEstimate(player);

        if (cost == 0) {
            return 0;
        }
        return cost * (1 + getComrades(player).count());
    }

    @Override
    public void apply(Pony player, Pos data) {
        getComrades(player).forEach(teleportee -> teleport(player, teleportee, data));
        super.apply(player, data);
    }

    private Stream<Caster<?>> getComrades(Pony player) {
        return Caster.stream(player.findAllEntitiesInRange(3, e -> FriendshipBraceletItem.isComrade(player, e)));
    }
}

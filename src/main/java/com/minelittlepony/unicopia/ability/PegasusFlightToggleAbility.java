package com.minelittlepony.unicopia.ability;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.util.Identifier;

public class PegasusFlightToggleAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 0;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 0;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canFly();
    }

    @Nullable
    @Override
    public Hit tryActivate(Pony player) {
        return player.getMaster().isCreative() ? null : Hit.INSTANCE;
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public Identifier getIcon(Pony player, boolean swap) {
        Identifier id = Abilities.REGISTRY.getId(this);
        return new Identifier(id.getNamespace(), "textures/gui/ability/" + id.getPath() + (player.getPhysics().isFlying() ? "_land" : "_takeoff") + ".png");
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public void apply(Pony player, Hit data) {
        if (player.getMaster().isCreative()) {
            return;
        }

        player.subtractEnergyCost(1);

        if (!player.getPhysics().isFlying()) {
            player.getEntity().addVelocity(0, player.getPhysics().getGravitySignum() * 0.7F, 0);
            Living.updateVelocity(player.getEntity());
            player.getPhysics().startFlying(true);
        } else {
            player.getPhysics().cancelFlight(true);
        }
        player.setDirty();
        player.setAnimation(Animation.SPREAD_WINGS);
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().add(6);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
    }
}

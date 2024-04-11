package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.util.Identifier;

public class ToggleFlightAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 0;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 0;
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.of(!player.asEntity().isCreative() && !player.getPhysics().getFlightType().isGrounded());
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public Identifier getIcon(Pony player) {
        Identifier id = Abilities.REGISTRY.getId(this);
        Race race = player.getObservedSpecies();
        return new Identifier(id.getNamespace(), "textures/gui/ability/" + id.getPath()
            + (player.getPhysics().isFlying() ? "_land" : "_takeoff")
            + "_" + (race.isHuman() ? Race.EARTH : race).getId().getPath()
            + ".png");
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public boolean apply(Pony player, Hit data) {
        if (prepare(player).isEmpty()) {
            return false;
        }

        player.subtractEnergyCost(1);

        if (!player.getPhysics().isFlying()) {
            player.asEntity().addVelocity(0, player.getPhysics().getGravitySignum() * 0.7F, 0);
            Living.updateVelocity(player.asEntity());
            player.getPhysics().startFlying(true);
        } else {
            player.getPhysics().cancelFlight(true);
        }
        player.setDirty();
        player.setAnimation(Animation.SPREAD_WINGS, Animation.Recipient.ANYONE);
        return true;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(6);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}

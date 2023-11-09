package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class ChangeFormAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 10;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 1000;
    }

    @Override
    public boolean canUse(Race.Composite race) {
        return race.potential() != null;
    }

    @Override
    public boolean canUse(Race race) {
        return true;
    }

    @Override
    public Identifier getIcon(Pony player) {
        Race potential = player.getCompositeRace().potential();
        if (potential == null) {
            return Ability.super.getIcon(player);
        }
        return getId().withPath(p -> "textures/gui/ability/" + p + "_" + potential.getId().getPath() + ".png");
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.of(canUse(player.getCompositeRace()));
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
        if (prepare(player).isEmpty()) {
            return false;
        }

        player.subtractEnergyCost(5);

        Race.Composite composite = player.getCompositeRace();
        Race actualRace = player.getSpecies();
        player.setSpecies(composite.potential());
        player.setSuppressedRace(actualRace.availability().isGrantable() ? actualRace : Race.UNSET);

        return true;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(6);

        if (player.getAbilities().getStat(slot).getWarmup() % 5 == 0) {
            player.asWorld().playSound(null, player.getOrigin(), SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, SoundCategory.PLAYERS);
        }

        if (player.asWorld().random.nextInt(5) == 0) {
            player.asWorld().playSound(null, player.getOrigin(), USounds.Vanilla.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, SoundCategory.PLAYERS);
        }

        player.spawnParticles(ParticleTypes.BUBBLE_COLUMN_UP, 15);
        player.spawnParticles(ParticleTypes.BUBBLE_POP, 15);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}

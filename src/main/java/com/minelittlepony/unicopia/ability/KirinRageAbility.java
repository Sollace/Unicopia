package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;

/**
 * Kirin ability to transform into a nirik
 */
public class KirinRageAbility implements Ability<Hit> {
    @Override
    public int getWarmupTime(Pony player) {
        return 30;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 60;
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
        return 0;
    }

    @Override
    public boolean apply(Pony player, Hit data) {

        if (player.consumeSuperMove()) {
            player.getMagicalReserves().getCharge().set(0);
            SpellType.RAGE.withTraits().apply(player, CastingMethod.INNATE);
        } else {
            int type = 1 + player.asWorld().random.nextInt(4);
            player.asEntity().sendMessage(Text.translatable("ability.unicopia.too_calm." + type), true);
            if (type == 4) {
                player.getMagicalReserves().getCharge().addPercent(1);
            }
            player.asEntity().addExhaustion(1.5F);

            if (StateMaps.BURNABLE.convert(player.asWorld(), player.getOrigin().down())) {
                player.playSound(USounds.SPELL_FIRE_CRACKLE, 1);
            }
        }

        return true;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.spawnParticles(ParticleTypes.LAVA, 4);
        player.getMagicalReserves().getEnergy().addPercent(1.03F);
        if (player.asEntity().age % 15 == 0) {
            player.asWorld().playSound(player.asEntity(), player.getOrigin(), USounds.ENTITY_PLAYER_KIRIN_RAGE, SoundCategory.PLAYERS, 1F, 0.0125F);
        }
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}

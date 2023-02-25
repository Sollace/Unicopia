package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.MagicReserves;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RaceChangeStatusEffect extends StatusEffect {
    public static final int STAGE_DURATION = 200;
    public static final int MAX_DURATION = Stage.VALUES.length * STAGE_DURATION + 1;

    public static final StatusEffect CHANGE_RACE_EARTH = register(0x886F0F, Race.EARTH);
    public static final StatusEffect CHANGE_RACE_UNICORN = register(0x88FFFF, Race.UNICORN);
    public static final StatusEffect CHANGE_RACE_PEGASUS = register(0x00FFFF, Race.PEGASUS);
    public static final StatusEffect CHANGE_RACE_BAT = register(0x0FFF00, Race.BAT);
    public static final StatusEffect CHANGE_RACE_CHANGELING = register(0xFFFF00, Race.CHANGELING);

    private final Race race;

    public static StatusEffect register(int color, Race race) {
        Identifier id = Race.REGISTRY.getId(race);
        return Registry.register(Registry.STATUS_EFFECT,
                new Identifier(id.getNamespace(), "change_race_" + id.getPath().toLowerCase()),
                new RaceChangeStatusEffect(color, race)
        );
    }

    public static boolean hasEffect(PlayerEntity player) {
        if (UItems.ALICORN_AMULET.isApplicable(player)) {
            return true;
        }
        return player.getStatusEffects().stream().anyMatch(effect -> effect.getEffectType() instanceof RaceChangeStatusEffect);
    }

    public RaceChangeStatusEffect(int color, Race race) {
        super(StatusEffectCategory.NEUTRAL, color);
        this.race = race;
    }

    public Race getSpecies() {
        return race;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        StatusEffectInstance state = entity.getStatusEffect(this);

        if (state == null) {
            return;
        }

        Equine<?> eq = Equine.of(entity).orElse(null);

        if (eq == null) {
            return;
        }

        int ticks = Math.max(0, MAX_DURATION - state.getDuration());

        Stage stage = Stage.forDuration(ticks / STAGE_DURATION);

        if (stage == Stage.INITIAL) {
            return;
        }

        int progression = ticks % (stage.ordinal() * STAGE_DURATION);

        if ((eq instanceof Pony pony ? pony.getActualSpecies() : eq.getSpecies()) == race || !race.isPermitted(entity instanceof PlayerEntity player ? player : null)) {
            if (progression == 0 && entity instanceof PlayerEntity player && stage == Stage.CRAWLING) {
                player.sendMessage(Stage.INITIAL.getMessage(race), true);
            }
            return;
        }

        if (progression == 0 && stage != Stage.DEATH && entity instanceof PlayerEntity player) {
            player.sendMessage(stage.getMessage(race), true);
        }

        entity.setHealth(1);

        if (eq instanceof Pony pony) {
            MagicReserves magic = pony.getMagicalReserves();
            pony.setRespawnRace(race);
            magic.getExertion().add(50);
            magic.getEnergy().add(3);
            magic.getExhaustion().add(3);

            if (state.shouldShowParticles()) {
                pony.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, 5);
            }
        }

        if (stage == Stage.DEATH) {
            if (eq instanceof Caster) {
                ((Caster<?>)eq).getSpellSlot().clear();
            }

            if (eq instanceof Pony pony) {
                MagicReserves magic = pony.getMagicalReserves();
                magic.getEnergy().set(0.6F);
                magic.getExhaustion().set(0);
                magic.getExertion().set(0);
                entity.damage(MagicalDamageSource.TRIBE_SWAP, Float.MAX_VALUE);
            } else {
                eq.setSpecies(race);
            }
        }
    }

    @Override
    public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        applyUpdateEffect(target, amplifier);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration > 0;
    }

    enum Stage {
        INITIAL,
        CRAWLING,
        DETERMINATION,
        RESURECTION,
        DEATH;

        static Stage[] VALUES = values();

        public static Stage forDuration(int duration) {
            return VALUES[duration % VALUES.length];
        }

        public String getTranslationKey() {
            return String.format("unicopia.effect.tribe.stage.%s", name().toLowerCase());
        }

        public Text getMessage(Race race) {
            return Text.translatable(getTranslationKey(), race.getDisplayName());
        }
    }
}

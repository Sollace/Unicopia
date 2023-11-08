package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.MagicReserves;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.ExplosionUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World.ExplosionSourceType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;

public class RaceChangeStatusEffect extends StatusEffect {
    public static final int STAGE_DURATION = 200;
    public static final int MAX_DURATION = Stage.VALUES.length * STAGE_DURATION + 1;

    public static final StatusEffect CHANGE_RACE_EARTH = register(0x886F0F, Race.EARTH);
    public static final StatusEffect CHANGE_RACE_UNICORN = register(0x88FFFF, Race.UNICORN);
    public static final StatusEffect CHANGE_RACE_PEGASUS = register(0x00FFFF, Race.PEGASUS);
    public static final StatusEffect CHANGE_RACE_BAT = register(0x0FFF00, Race.BAT);
    public static final StatusEffect CHANGE_RACE_CHANGELING = register(0xFFFF00, Race.CHANGELING);
    public static final StatusEffect CHANGE_RACE_KIRIN = register(0xFF8800, Race.KIRIN);

    private final Race race;

    public static StatusEffect register(int color, Race race) {
        Identifier id = Race.REGISTRY.getId(race);
        return Registry.register(Registries.STATUS_EFFECT,
                new Identifier(id.getNamespace(), "change_race_" + id.getPath().toLowerCase()),
                new RaceChangeStatusEffect(color, race)
        );
    }

    public RaceChangeStatusEffect(int color, Race race) {
        super(StatusEffectCategory.NEUTRAL, color);
        this.race = race;
    }

    public Race getSpecies() {
        return race;
    }

    private void removeEffect(LivingEntity entity) {
        entity.removeStatusEffect(this);
        resetTicks(entity);
    }

    private void resetTicks(LivingEntity entity) {
        Pony.of(entity).ifPresent(pony -> pony.setTicksmetamorphising(0));
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        StatusEffectInstance state = entity.getStatusEffect(this);

        if (state == null || entity.isDead()) {
            resetTicks(entity);
            return;
        }

        Equine<?> eq = Equine.of(entity).orElse(null);

        if (eq == null) {
            return;
        }

        int metaTicks = 0;

        if (eq instanceof Pony pony) {
            metaTicks = pony.getTicksMetamorphising() + 1;
            pony.setTicksmetamorphising(metaTicks);
        }

        int ticks = Math.max(0, MAX_DURATION - state.getDuration());

        Stage stage = Stage.forDuration(ticks / STAGE_DURATION);

        if (stage == Stage.INITIAL) {
            return;
        }

        int progression = ticks % (stage.ordinal() * STAGE_DURATION);

        if (eq.getSpecies() == race || !race.isPermitted(entity instanceof PlayerEntity player ? player : null)) {
            if (progression == 0 && entity instanceof PlayerEntity player && stage == Stage.CRAWLING) {
                player.sendMessage(Stage.INITIAL.getMessage(race), true);
            }
            removeEffect(entity);
            return;
        }

        if (progression == 0 && stage != Stage.DEATH && entity instanceof PlayerEntity player) {
            player.sendMessage(stage.getMessage(race), true);
        }

        if (eq instanceof Pony pony) {
            MagicReserves magic = pony.getMagicalReserves();
            pony.setRespawnRace(race);
            magic.getExertion().add(5);
            if (magic.getEnergy().get() < 1) {
                magic.getEnergy().add(1.1F);
                entity.playSound(USounds.BLOCK_CHITIN_AMBIENCE, 0.1F, 2F);
            }
            magic.getExhaustion().add(3);

            if (state.shouldShowParticles()) {
                pony.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, 1);
            }
        }

        if (stage == Stage.DEATH) {
            removeEffect(entity);

            if (eq instanceof Caster) {
                ((Caster<?>)eq).getSpellSlot().clear();
            }

            if (eq instanceof Pony pony && metaTicks > 200) {
                MagicReserves magic = pony.getMagicalReserves();
                magic.getEnergy().set(0.6F);
                magic.getExhaustion().set(0);
                magic.getExertion().set(0);

                if (pony.asEntity().isCreative() || entity.getWorld().getDifficulty().getId() < 2 || entity.getWorld().getLevelProperties().isHardcore()) {

                    if (!pony.asEntity().isCreative()) {
                        float cost = entity.getWorld().getLevelProperties().isHardcore() ? 0.75F : switch (entity.getWorld().getDifficulty()) {
                            case PEACEFUL -> 0.125F;
                            case EASY -> 0.25F;
                            default -> 0.5F;
                        };

                        entity.setHealth(Math.max(1, entity.getHealth() * cost));
                        HungerManager hunger = pony.asEntity().getHungerManager();
                        int food = hunger.getFoodLevel();
                        pony.asEntity().getHungerManager().setFoodLevel(Math.max(Math.min(1, food), (int)(food * cost)));
                    }
                    pony.setSpecies(race);
                } else if (!pony.asEntity().isCreative()) {
                    if (!entity.damage(Living.living(entity).damageOf(UDamageTypes.TRIBE_SWAP), Float.MAX_VALUE)) {
                        entity.setHealth(0);
                        pony.setRespawnRace(Race.UNSET);
                        pony.setSpecies(race);
                    }
                }
                entity.getWorld().createExplosion(entity, null, ExplosionUtil.NON_DESTRUCTIVE, entity.getPos(), 5, true, ExplosionSourceType.MOB);
            } else {
                eq.setSpecies(race);
                entity.getWorld().createExplosion(entity, null, ExplosionUtil.NON_DESTRUCTIVE, entity.getPos(), 5, true, ExplosionSourceType.MOB);
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

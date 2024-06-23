package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.minelittlepony.api.model.*;
import com.minelittlepony.api.model.fabric.PonyModelPrepareCallback;
import com.minelittlepony.api.model.gear.IGear;
import com.minelittlepony.api.pony.IPony;
import com.minelittlepony.api.pony.IPonyData;
import com.minelittlepony.client.render.MobRenderers;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.AnimationUtil;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Main extends MineLPDelegate implements ClientModInitializer {
    private static final Map<com.minelittlepony.api.pony.meta.Race, Race> PONY_RACE_MAPPING = new HashMap<>();
    private static final Function<com.minelittlepony.api.pony.meta.Race, Race> LOOKUP_CACHE = Util.memoize(race -> {
        return Optional.ofNullable(PONY_RACE_MAPPING.get(race))
                .or(() -> Race.REGISTRY.getOrEmpty(Unicopia.id(race.name().toLowerCase(Locale.ROOT))))
                .orElse(Race.UNSET);
    });

    public static void registerRaceMapping(com.minelittlepony.api.pony.meta.Race minelpRace, Race unicopiaRace) {
        PONY_RACE_MAPPING.put(minelpRace, unicopiaRace);
    }

    private boolean hookErroring;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        PonyModelPrepareCallback.EVENT.register(this::onPonyModelPrepared);
        IGear.register(() -> new BangleGear(TrinketsDelegate.MAIN_GLOVE));
        IGear.register(() -> new BangleGear(TrinketsDelegate.SECONDARY_GLOVE));
        IGear.register(HeldEntityGear::new);
        IGear.register(BodyPartGear::pegasusWings);
        IGear.register(BodyPartGear::batWings);
        IGear.register(BodyPartGear::bugWings);
        IGear.register(BodyPartGear::unicornHorn);
        IGear.register(AmuletGear::new);
        IGear.register(GlassesGear::new);
        IGear.register(SpellEffectGear::new);

        registerRaceMapping(com.minelittlepony.api.pony.meta.Race.CHANGEDLING, Race.CHANGELING);
        registerRaceMapping(com.minelittlepony.api.pony.meta.Race.ZEBRA, Race.EARTH);
        registerRaceMapping(com.minelittlepony.api.pony.meta.Race.GRYPHON, Race.PEGASUS);
        registerRaceMapping(com.minelittlepony.api.pony.meta.Race.HIPPOGRIFF, Race.HIPPOGRIFF);
        registerRaceMapping(com.minelittlepony.api.pony.meta.Race.BATPONY, Race.BAT);
        registerRaceMapping(com.minelittlepony.api.pony.meta.Race.SEAPONY, Race.SEAPONY);
    }

    private void onPonyModelPrepared(Entity entity, IModel model, ModelAttributes.Mode mode) {
        if (hookErroring) return;
        try {
            if (entity instanceof PlayerEntity) {
                if (entity instanceof Owned<?> o && o.getMaster() instanceof PlayerEntity master) {
                    entity = master;
                }
                Pony pony = Pony.of((PlayerEntity)entity);

                if (pony.getMotion().isFlying()) {
                    model.getAttributes().wingAngle = MathHelper.clamp(pony.getMotion().getWingAngle() / 3F - (float)Math.PI * 0.4F, -2, 0);

                    Vec3d motion = pony.getMotion().getClientVelocity();
                    double zMotion = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
                    model.getAttributes().isGoingFast |= zMotion > 0.4F;
                    model.getAttributes().isGoingFast |= pony.getMotion().isDiving();
                }

                model.getAttributes().isGoingFast |= pony.getMotion().isRainbooming();
                model.getAttributes().isGoingFast &= !pony.getEntityInArms().isPresent();

                if (pony.getAnimation().isOf(Animation.SPREAD_WINGS)) {
                    model.getAttributes().wingAngle = -AnimationUtil.seeSitSaw(pony.getAnimationProgress(1), 1.5F) * (float)Math.PI / 1.2F;
                    model.getAttributes().isFlying = true;
                }
            }
        } catch (Throwable t) {
            Unicopia.LOGGER.error("Exception occured in MineLP hook:onPonyModelPrepared", t);
            hookErroring = true;
        }
    }


    @Override
    public int getMagicColor(Entity entity) {
        return IPony.getManager().getPony(entity).map(IPony::metadata).map(IPonyData::getGlowColor).orElse(0);
    }

    @Override
    public Race getPlayerPonyRace(PlayerEntity player) {
        return toUnicopiaRace(IPony.getManager().getPony(player).race());
    }

    @Override
    public Race getRace(Entity entity) {
        if (entity instanceof AllayEntity) {
            return MobRenderers.ALLAY.get() ? Race.PEGASUS : Race.HUMAN;
        }

        return IPony.getManager().getPony(entity).map(IPony::race).map(Main::toUnicopiaRace).orElse(Race.UNSET);
    }

    @Override
    public float getPonyHeight(Entity entity) {
        return super.getPonyHeight(entity) * IPony.getManager().getPony(entity).map(pony -> pony.metadata().getSize().getScaleFactor() + 0.1F).orElse(1F);
    }

    private static Race toUnicopiaRace(com.minelittlepony.api.pony.meta.Race race) {
        return LOOKUP_CACHE.apply(race);
    }
}

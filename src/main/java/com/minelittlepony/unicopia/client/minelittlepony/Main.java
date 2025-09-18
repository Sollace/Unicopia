package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.minelittlepony.api.events.PonyRenderStatePrepareCallback;
import com.minelittlepony.api.model.*;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.api.pony.PonyData;
import com.minelittlepony.client.render.MobRenderers;
import com.minelittlepony.client.render.entity.state.PonyRenderState;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.util.AnimationUtil;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.entity.state.AllayEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class Main extends MineLPDelegate implements ClientModInitializer {
    private static final Map<com.minelittlepony.api.pony.meta.Race, Race> PONY_RACE_MAPPING = new HashMap<>();
    private static final Function<com.minelittlepony.api.pony.meta.Race, Race> LOOKUP_CACHE = Util.memoize(race -> {
        return Optional.ofNullable(PONY_RACE_MAPPING.get(race))
                .or(() -> Race.REGISTRY.getOptionalValue(Unicopia.id(race.name().toLowerCase(Locale.ROOT))))
                .orElse(Race.UNSET);
    });

    /**
     * Registers a mapping from a mine little pony race to a unicopia race.
     * Use this if your race A) has a different name from the minelp one, or B) you want to map it to something other
     * than the automatic mapping permits
     *
     * @param minelpRace
     * @param unicopiaRace
     */
    public static void registerRaceMapping(com.minelittlepony.api.pony.meta.Race minelpRace, Race unicopiaRace) {
        PONY_RACE_MAPPING.put(minelpRace, unicopiaRace);
    }

    static {
        registerRaceMapping(com.minelittlepony.api.pony.meta.Race.CHANGEDLING, Race.CHANGELING);
        registerRaceMapping(com.minelittlepony.api.pony.meta.Race.ZEBRA, Race.EARTH);
        registerRaceMapping(com.minelittlepony.api.pony.meta.Race.GRYPHON, Race.PEGASUS);
        registerRaceMapping(com.minelittlepony.api.pony.meta.Race.BATPONY, Race.BAT);
    }

    private boolean hookErroring;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        PonyRenderStatePrepareCallback.EVENT.register(this::onPonyModelPrepared);
        Gear.register(() -> new BangleGear(TrinketsDelegate.MAIN_GLOVE));
        Gear.register(() -> new BangleGear(TrinketsDelegate.SECONDARY_GLOVE));
        Gear.register(HeldEntityGear::new);
        Gear.register(BodyPartGear::pegasusWings);
        Gear.register(BodyPartGear::batWings);
        Gear.register(BodyPartGear::bugWings);
        Gear.register(BodyPartGear::unicornHorn);
        Gear.register(AmuletGear::new);
        Gear.register(GlassesGear::new);
        Gear.register(SpellEffectGear::new);
    }

    private void onPonyModelPrepared(PonyRenderState state, PonyModel<?> model, ModelAttributes.Mode mode) {
        if (hookErroring) return;
        try {
            CasterState casterState = CasterState.of(state);
            ModelAttributes attributes = state.getAttributes();

            if (casterState.flying) {
                attributes.wingAngle = MathHelper.clamp(casterState.wingsAngle / 3F - (float)Math.PI * 0.4F, -2, 0);
            }
            attributes.isGoingFast |= casterState.dashing;
            attributes.isGoingFast &= casterState.carriedEntity.state == null;

            if (casterState.animation.isOf(Animation.SPREAD_WINGS)) {
                attributes.wingAngle = -AnimationUtil.seeSitSaw(casterState.animationTime, 1.5F) * (float)Math.PI / 1.2F;
                attributes.isFlying = true;
            }
        } catch (Throwable t) {
            Unicopia.LOGGER.error("Exception occured in MineLP hook:onPonyModelPrepared", t);
            hookErroring = true;
        }
    }


    @Override
    public int getMagicColor(Entity entity) {
        return com.minelittlepony.api.pony.Pony.getManager().getPony(entity).map(com.minelittlepony.api.pony.Pony::metadata).map(PonyData::glowColor).orElse(0);
    }

    @Override
    public Race getPlayerPonyRace(PlayerEntity player) {
        return toUnicopiaRace(com.minelittlepony.api.pony.Pony.getManager().getPony(player).race());
    }

    @Override
    public Race getRace(Entity entity) {
        if (entity instanceof AllayEntity) {
            return MobRenderers.ALLAY.option().get() ? Race.PEGASUS : Race.HUMAN;
        }

        return com.minelittlepony.api.pony.Pony.getManager().getPony(entity).map(com.minelittlepony.api.pony.Pony::race).map(Main::toUnicopiaRace).orElse(Race.HUMAN);
    }

    @Override
    public Race getRace(EntityRenderState state) {
        if (state instanceof AllayEntityRenderState) {
            return MobRenderers.ALLAY.option().get() ? Race.PEGASUS : Race.HUMAN;
        }
        return state instanceof PonyRenderState s ? toUnicopiaRace(s.getRace()) : Race.HUMAN;
    }

    @Override
    public float getPonyHeight(Entity entity) {
        return super.getPonyHeight(entity) * com.minelittlepony.api.pony.Pony.getManager().getPony(entity)
                .map(pony -> pony.race().isHuman() ? 1 : pony.metadata().size().scaleFactor() + 0.1F)
                .orElse(1F);
    }

    private static Race toUnicopiaRace(com.minelittlepony.api.pony.meta.Race race) {
        return LOOKUP_CACHE.apply(race);
    }
}

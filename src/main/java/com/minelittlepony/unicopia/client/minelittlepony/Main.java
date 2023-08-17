package com.minelittlepony.unicopia.client.minelittlepony;

import com.minelittlepony.api.model.*;
import com.minelittlepony.api.model.fabric.PonyModelPrepareCallback;
import com.minelittlepony.api.model.gear.IGear;
import com.minelittlepony.api.pony.IPony;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.util.AnimationUtil;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Main extends MineLPDelegate implements ClientModInitializer {

    private boolean hookErroring;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        PonyModelPrepareCallback.EVENT.register(this::onPonyModelPrepared);
        IGear.register(() -> new BangleGear(TrinketsDelegate.MAINHAND));
        IGear.register(() -> new BangleGear(TrinketsDelegate.OFFHAND));
        IGear.register(HeldEntityGear::new);
        IGear.register(BodyPartGear::pegasusWings);
        IGear.register(BodyPartGear::batWings);
        IGear.register(BodyPartGear::unicornHorn);
        IGear.register(AmuletGear::new);
        IGear.register(GlassesGear::new);
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
    public Race getPlayerPonyRace(PlayerEntity player) {
        return toUnicopiaRace(IPony.getManager().getPony(player).race());
    }

    @Override
    public Race getRace(Entity entity) {
        return IPony.getManager().getPony(entity).map(IPony::race).map(Main::toUnicopiaRace).orElse(Race.HUMAN);
    }

    @Override
    public float getPonyHeight(Entity entity) {
        return super.getPonyHeight(entity) * IPony.getManager().getPony(entity).map(pony -> pony.metadata().getSize().getScaleFactor() + 0.1F).orElse(1F);
    }

    private static Race toUnicopiaRace(com.minelittlepony.api.pony.meta.Race race) {
        return switch (race) {
            case ALICORN -> Race.ALICORN;
            case CHANGELING, CHANGEDLING -> Race.CHANGELING;
            case ZEBRA, EARTH -> Race.EARTH;
            case GRYPHON, HIPPOGRIFF, PEGASUS -> Race.PEGASUS;
            case BATPONY -> Race.BAT;
            case SEAPONY, UNICORN, KIRIN -> Race.UNICORN;
            default -> Race.HUMAN;
        };
    }
}

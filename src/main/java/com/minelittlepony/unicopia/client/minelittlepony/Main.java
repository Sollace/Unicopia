package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.Optional;

import com.minelittlepony.api.model.IModel;
import com.minelittlepony.api.model.ModelAttributes;
import com.minelittlepony.api.model.fabric.PonyModelPrepareCallback;
import com.minelittlepony.api.model.gear.IGear;
import com.minelittlepony.client.MineLittlePony;
import com.minelittlepony.client.render.LevitatingItemRenderer;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.util.AnimationUtil;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class Main extends MineLPDelegate implements ClientModInitializer {

    private boolean hookErroring;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        PonyModelPrepareCallback.EVENT.register(this::onPonyModelPrepared);
        IGear.register(() -> new BangleGear(TrinketsDelegate.MAINHAND));
        IGear.register(() -> new BangleGear(TrinketsDelegate.OFFHAND));
        IGear.register(AmuletGear::new);
    }

    @SuppressWarnings("unchecked")
    private void onPonyModelPrepared(Entity entity, IModel model, ModelAttributes.Mode mode) {
        if (hookErroring) return;
        try {
            if (entity instanceof PlayerEntity) {
                if (entity instanceof Owned) {
                    entity = ((Owned<PlayerEntity>)entity).getMaster();
                }
                Pony pony = Pony.of((PlayerEntity)entity);

                if (pony.getMotion().isFlying()) {
                    model.getAttributes().wingAngle = MathHelper.clamp(pony.getMotion().getWingAngle() / 3 - (float)Math.PI * 0.7F, -3, 0);
                    model.getAttributes().isHorizontal = true;
                }
                model.getAttributes().isGoingFast |= pony.getMotion().isRainbooming();

                if (pony.getAnimation() == Animation.SPREAD_WINGS) {
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
        switch (MineLittlePony.getInstance().getManager().getPony(player).getRace(false)) {
            case ALICORN:
                return Race.ALICORN;
            case CHANGELING:
            case CHANGEDLING:
                return Race.CHANGELING;
            case ZEBRA:
            case EARTH:
                return Race.EARTH;
            case GRYPHON:
            case HIPPOGRIFF:
            case PEGASUS:
                return Race.PEGASUS;
            case BATPONY:
                return Race.BAT;
            case SEAPONY:
            case UNICORN:
                return Race.UNICORN;
            default:
                return Race.HUMAN;
        }
    }

    @Override
    public Optional<VertexConsumer> getItemBuffer(VertexConsumerProvider vertexConsumers, Identifier texture) {
        if (LevitatingItemRenderer.isEnabled()) {
            return Optional.of(vertexConsumers.getBuffer(LevitatingItemRenderer.getRenderLayer(texture)));
        }

        return Optional.empty();
    }
}

package com.minelittlepony.unicopia.client.minelittlepony;

import com.minelittlepony.client.render.EquineRenderManager;
import com.minelittlepony.model.IModel;
import com.minelittlepony.model.capabilities.fabric.PonyModelPrepareCallback;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class Main implements ClientModInitializer {

    private boolean hookErroring;

    @Override
    public void onInitializeClient() {
        PonyModelPrepareCallback.EVENT.register(this::onPonyModelPrepared);
    }

    private void onPonyModelPrepared(Entity entity, IModel model, EquineRenderManager.Mode mode) {
        if (hookErroring) return;
        try {
            if (entity instanceof PlayerEntity) {
                Pony pony = Pony.of((PlayerEntity)entity);

                if (pony.getMotion().isFlying()) {
                    model.getAttributes().wingAngle = MathHelper.clamp(pony.getMotion().getWingAngle() / 3 - (float)Math.PI * 0.7F, -3, 0);
                    model.getAttributes().isHorizontal = true;
                }
                model.getAttributes().isGoingFast |= pony.getMotion().isRainbooming();
            }
        } catch (Throwable t) {
            Unicopia.LOGGER.error("Exception occured in MineLP hook:onPonyModelPrepared", t);
            hookErroring = true;
        }
    }
}

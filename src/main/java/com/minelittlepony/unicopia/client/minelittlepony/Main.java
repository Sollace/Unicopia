package com.minelittlepony.unicopia.client.minelittlepony;

import com.minelittlepony.client.render.EquineRenderManager;
import com.minelittlepony.model.IModel;
import com.minelittlepony.model.capabilities.fabric.PonyModelPrepareCallback;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class Main implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PonyModelPrepareCallback.EVENT.register(this::onPonyModelPrepared);
    }

    private void onPonyModelPrepared(Entity entity, IModel model, EquineRenderManager.Mode mode) {
        if (entity instanceof PlayerEntity) {
            Pony pony = Pony.of((PlayerEntity)entity);

            model.getAttributes().isSwimming |= pony.getMotion().isFlying();
            //model.getAttributes().isSwimmingRotated |= pony.getMotion().isFlying();
            model.getAttributes().isGoingFast |= pony.getMotion().isRainbooming();
        }
    }
}

package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.EquinePredicates;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class BatEyesApplicator {

    public static final BatEyesApplicator INSTANCE = new BatEyesApplicator();

    private boolean batEyesApplied;

    private final MinecraftClient client = MinecraftClient.getInstance();

    public float getWorldBrightness(float initial, LivingEntity entity, float tickDelta) {
        if (!EquinePredicates.PLAYER_BAT.test(entity)) {
            return initial;
        }
        return 0.6F;
    }

    public void enable() {
        if (client.world != null && client.player != null) {
            if (!client.player.hasStatusEffect(StatusEffects.NIGHT_VISION) && EquinePredicates.PLAYER_BAT.test(client.player)) {
                client.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 1, false, false));
                batEyesApplied = true;
            }
        }
    }

    public void disable() {
        if (batEyesApplied) {
            client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            batEyesApplied = false;
        }
    }

}

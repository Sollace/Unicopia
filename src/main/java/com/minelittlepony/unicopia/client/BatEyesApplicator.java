package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.EquinePredicates;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

public class BatEyesApplicator {

    public static final BatEyesApplicator INSTANCE = new BatEyesApplicator();

    private boolean batEyesApplied;

    private final MinecraftClient client = MinecraftClient.getInstance();

    public static float getWorldBrightness(float initial, LivingEntity entity, float tickDelta) {
        if (EquinePredicates.PLAYER_BAT.test(entity)) {
            return 0.6F;
        }
        return initial;
    }


    // TODO: Do we need this?
    public void enable() {
        if (client.world != null) {
            PlayerEntity player = client.player;
            if (!player.hasStatusEffect(StatusEffects.NIGHT_VISION) && EquinePredicates.PLAYER_BAT.test(player)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1, 1, false, false));
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

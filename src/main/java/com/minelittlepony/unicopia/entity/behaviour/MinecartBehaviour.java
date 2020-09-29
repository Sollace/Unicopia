package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingMinecartSoundInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;

public class MinecartBehaviour extends EntityBehaviour<AbstractMinecartEntity> {

    @Override
    public AbstractMinecartEntity onCreate(AbstractMinecartEntity entity, Disguise context, boolean replaceOld) {
        super.onCreate(entity, context, replaceOld);
        if (replaceOld && entity.world.isClient) {
            MinecraftClient.getInstance().getSoundManager().play(new MovingMinecartSoundInstance(entity));
        }
        return entity;
    }

    @Override
    public void update(Caster<?> source, AbstractMinecartEntity entity, DisguiseSpell spell) {
        entity.yaw -= 90;
        entity.prevYaw -= 90;

        entity.pitch = 0;
        entity.prevPitch = 0;

        if (source.getEntity() instanceof LivingEntity) {
            int hurt = ((LivingEntity)source.getEntity()).hurtTime;

            if (hurt > 0) {
                entity.setDamageWobbleTicks(hurt);
                entity.setDamageWobbleStrength(1);
                entity.setDamageWobbleSide(20 + (int)source.getEntity().fallDistance / 10);
            }
        }
    }
}

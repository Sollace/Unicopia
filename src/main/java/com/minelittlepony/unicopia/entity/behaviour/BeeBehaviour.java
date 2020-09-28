package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AggressiveBeeSoundInstance;
import net.minecraft.client.sound.PassiveBeeSoundInstance;
import net.minecraft.entity.passive.BeeEntity;

public class BeeBehaviour extends EntityBehaviour<BeeEntity> {
    @Override
    public BeeEntity onCreate(BeeEntity entity, Disguise context, boolean replaceOld) {
        super.onCreate(entity, context, replaceOld);
        if (replaceOld && entity.world.isClient) {
            MinecraftClient.getInstance().getSoundManager().playNextTick(
                    entity.hasAngerTime() ? new AggressiveBeeSoundInstance(entity) : new PassiveBeeSoundInstance(entity)
            );
        }
        return entity;
    }

    @Override
    public void update(Caster<?> source, BeeEntity entity, Spell spell) {

        if (source.getOwner().isSneaking()) {
            entity.setAngerTime(10);
        } else {
            entity.setAngerTime(0);
        }
    }
}

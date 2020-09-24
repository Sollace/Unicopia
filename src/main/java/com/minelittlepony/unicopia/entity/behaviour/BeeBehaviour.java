package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AggressiveBeeSoundInstance;
import net.minecraft.client.sound.PassiveBeeSoundInstance;
import net.minecraft.entity.passive.BeeEntity;

public class BeeBehaviour extends EntityBehaviour<BeeEntity> {
    @Override
    public void onCreate(BeeEntity entity) {
        super.onCreate(entity);
        if (entity.world.isClient) {
            MinecraftClient.getInstance().getSoundManager().playNextTick(
                    entity.hasAngerTime() ? new AggressiveBeeSoundInstance(entity) : new PassiveBeeSoundInstance(entity)
            );
        }
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

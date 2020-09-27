package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class LlamaBehaviour extends EntityBehaviour<LlamaEntity> {

    @Override
    public void update(Caster<?> source, LlamaEntity entity, Spell spell) {

        if (source instanceof Pony) {
            Pony player = (Pony)source;

            if (player.sneakingChanged() && isSneakingOnGround(player)) {

                LlamaSpitEntity spit = new LlamaSpitEntity(entity.world, entity);

                Vec3d rot = source.getEntity().getRotationVec(1);

                spit.setVelocity(rot.getX(), rot.getY(), rot.getZ(), 1.5F, 3);
                spit.setOwner(source.getOwner());

                if (!entity.isSilent()) {
                    entity.world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.ENTITY_LLAMA_SPIT, entity.getSoundCategory(), 1,
                            1 + (entity.world.random.nextFloat() - entity.world.random.nextFloat()) * 0.2F);
                }

                entity.world.spawnEntity(spit);
            }
        }

    }
}

package com.minelittlepony.unicopia.entity.behaviour;

import java.util.function.BiFunction;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RangedAttackBehaviour<T extends Entity & RangedAttackMob> extends EntityBehaviour<T> {

    private final BiFunction<World, T, ProjectileEntity> projectileSupplier;

    private final SoundEvent sound;

    public RangedAttackBehaviour(SoundEvent sound, BiFunction<World, T, ProjectileEntity> projectileSupplier) {
        this.sound = sound;
        this.projectileSupplier = projectileSupplier;
    }

    @Override
    public void update(Caster<?> source, T entity, Spell spell) {

        if (source instanceof Pony) {
            Pony player = (Pony)source;

            if (player.sneakingChanged() && isSneakingOnGround(player)) {

                ProjectileEntity spit = projectileSupplier.apply(entity.world, entity);

                Vec3d rot = source.getEntity().getRotationVec(1);

                spit.setVelocity(rot.getX(), rot.getY(), rot.getZ(), 1.5F, 3);
                spit.setOwner(source.getOwner());

                if (!entity.isSilent()) {
                    entity.world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            sound, entity.getSoundCategory(), 1,
                            1 + (entity.world.random.nextFloat() - entity.world.random.nextFloat()) * 0.2F);
                }

                entity.world.spawnEntity(spit);
            }
        }
    }
}

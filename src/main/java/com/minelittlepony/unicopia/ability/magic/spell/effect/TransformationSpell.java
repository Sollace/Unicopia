package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.RegistryUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

/**
 * Transforms whatever entity it strikes into a random other entity.
 */
public class TransformationSpell extends AbstractSpell implements ProjectileDelegate.EntityHitListener {
    protected TransformationSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {
        return situation == Situation.PROJECTILE;
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        if (projectile.getWorld().isClient) {
            return;
        }
        Entity entity = hit.getEntity();
        pickType(entity.getType(), entity.getWorld()).flatMap(type -> convert(entity, type)).ifPresentOrElse(e -> {
            entity.playSound(USounds.SPELL_TRANSFORM_TRANSMUTE_ENTITY, 1, 1);
        }, () -> {
            ParticleUtils.spawnParticles(ParticleTypes.SMOKE, entity, 20);
            entity.playSound(USounds.GUI_ABILITY_FAIL, 1, 1);
        });
    }

    private Optional<? extends MobEntity> convert(Entity entity, EntityType<? extends MobEntity> type) {
        if (entity instanceof MobEntity) {
            MobEntity mob = (MobEntity)entity;
            try {
                return Optional.ofNullable(mob.convertTo(type, true));
            } catch (Exception e) {
                return Optional.ofNullable(mob.convertTo(UEntities.BUTTERFLY, true));
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <T extends MobEntity> Optional<EntityType<T>> pickType(EntityType<?> except, World world) {
        Set<EntityType<?>> options = RegistryUtils.valuesForTag(world, UTags.Entities.TRANSFORMABLE).collect(Collectors.toSet());
        if (except.getSpawnGroup() == SpawnGroup.MONSTER) {
            options.removeIf(t -> t.getSpawnGroup() == SpawnGroup.MONSTER);
        } else {
            options.remove(except);
        }
        if (options.size() <= 1) {
            return options.stream().findFirst().map(t -> (EntityType<T>)t);
        }
        return Optional.ofNullable((EntityType<T>)Util.getRandom(new ArrayList<>(options), world.random));
    }
}

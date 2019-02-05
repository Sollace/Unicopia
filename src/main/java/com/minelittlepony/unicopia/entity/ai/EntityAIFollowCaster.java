package com.minelittlepony.unicopia.entity.ai;

import java.lang.reflect.Field;

import com.google.common.base.Predicate;

import com.minelittlepony.unicopia.forgebullshit.RegistryLockSpinner;
import com.minelittlepony.unicopia.spell.ICaster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIFollow;

public class EntityAIFollowCaster extends EntityAIFollow {

    protected final ICaster<?> entity;

    private static Field __followPredicate;

    static {
        try {
            Field f = EntityAIFollow.class.getDeclaredFields()[1];
            f.setAccessible(true);
            __followPredicate = RegistryLockSpinner.makeNonFinal(f);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            __followPredicate = null;
        }
    }

    public EntityAIFollowCaster(ICaster<?> caster, double walkSpeed, float maxDistance, float area) {
        super((EntityLiving)caster.getEntity(), walkSpeed, maxDistance, area);

        entity = caster;

        if (__followPredicate != null) {
            try {
                __followPredicate.set(this, (Predicate<EntityLivingBase>)(e -> {
                    Entity owner = caster.getOwner();
                    return e != null && (e == owner || (owner != null && owner.getUniqueID().equals(e.getUniqueID())));
                }));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.mixin.client.MixinAnimalModel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PigEntity;

public class AnimalPoser {
    public static final AnimalPoser INSTANCE = new AnimalPoser();

    public void applyPosing(MatrixStack matrices, MobEntity entity, EntityModel<?> model) {

        if (entity instanceof PigEntity && model instanceof QuadrupedEntityModel<?> quad) {
            Equine.of((LivingEntity)entity)
                .filter(eq -> eq instanceof Creature)
                .map(Creature.class::cast)
                .ifPresent(creature -> {
                    float tickDelta = MinecraftClient.getInstance().getTickDelta();
                    float headAngle = creature.getHeadAngle(tickDelta);
                    float neckAngle = 12;

                    ((MixinAnimalModel)quad).invokeGetHeadParts().forEach(part -> {
                        part.pivotY = neckAngle;
                        part.pitch = headAngle;
                    });
                });
        }
    }
}

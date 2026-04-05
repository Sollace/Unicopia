package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;

public class AnimalPoser {
    public static final AnimalPoser INSTANCE = new AnimalPoser();

    public void applyPosing(MatrixStack matrices, EntityRenderState entity, EntityModel<?> model) {
        CasterState state = CasterState.of(entity);

        if (state.type == EntityType.PIG) {
            model.getPart(EntityModelPartNames.HEAD).ifPresent(part -> {
                part.originY = 12;
                part.pitch = state.eatingHeadAngle;
            });
        }
    }
}

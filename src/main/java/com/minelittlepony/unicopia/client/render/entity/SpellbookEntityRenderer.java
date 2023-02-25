package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.SpellbookEntity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

public class SpellbookEntityRenderer extends LivingEntityRenderer<SpellbookEntity, SpellbookModel> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/spellbook/normal.png");

    public SpellbookEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new SpellbookModel(SpellbookModel.getTexturedModelData().createModel()), 0);
    }

    @Override
    public Identifier getTexture(SpellbookEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getLyingAngle(SpellbookEntity entity) {
        return 0;
    }

    @Override
    protected void setupTransforms(SpellbookEntity entity, MatrixStack matrices, float f, float g, float partialTicks) {
        super.setupTransforms(entity, matrices, f, g + 90, partialTicks);

        if (entity.isOpen()) {
            matrices.translate(-1.25F, -0.35F, 0);

            float floatPosition = MathHelper.sin((entity.age + partialTicks + entity.getId()) / 20) * 0.04F;

            matrices.translate(0, floatPosition, 0);
            matrices.multiply(Vec3f.NEGATIVE_Z.getDegreesQuaternion(60));
        } else {
            matrices.translate(-1.5F, 0.1F, 0.2F);
            matrices.multiply(Vec3f.NEGATIVE_Z.getDegreesQuaternion(90));
            matrices.multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(90));
        }
    }

    @Override
    protected boolean hasLabel(SpellbookEntity targetEntity) {
        return super.hasLabel(targetEntity)
                && (targetEntity.isCustomNameVisible()
                        || targetEntity.hasCustomName()
                        && targetEntity == dispatcher.targetedEntity);
    }
}
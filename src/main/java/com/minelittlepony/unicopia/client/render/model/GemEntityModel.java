package com.minelittlepony.unicopia.client.render.model;

import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.util.Color;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EntityPose;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class GemEntityModel extends EntityModel<SpellcastEntity> {

    private ModelPart body;

    private int tint;

    public GemEntityModel() {
        textureWidth = 256;
        textureHeight = 256;

        body = new ModelPart(this);
        body.pivotY = 1.2f;
    }

    @Override
    public void setAngles(SpellcastEntity entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
        tint = entity.hasEffect() ? entity.getEffect().getTint() : -1;

        float floatOffset = MathHelper.sin((entity.age + customAngle) / 10 + entity.hoverStart) / 10 + 0.1F;

        boolean unstable = entity.overLevelCap();

        body.pivotX = unstable ? (float)Math.sin(customAngle) / 5F : 0;
        body.pivotY = 1.2F + floatOffset - entity.getEyeHeight(EntityPose.STANDING);
        body.pivotZ = unstable ? (float)Math.cos(customAngle) / 5F : 0;
        body.pitch = unstable ? (float)Math.sin(customAngle) : 0;

        floatOffset = (entity.age + customAngle) / 20;
        if (entity.getCurrentLevel() > 0) {
            floatOffset *= entity.getCurrentLevel() + 1;
        }

        floatOffset += entity.hoverStart;
        floatOffset *= 180 / (float)Math.PI;

        body.yaw = floatOffset;

    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        if (tint != -1) {
            red = Color.r(tint);
            green = Color.g(tint);
            blue = Color.b(tint);
        }
        matrices.push();
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(body.yaw));
        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(body.pitch));
        matrices.translate(body.pivotX, body.pivotY, body.pivotZ);

        MinecraftClient.getInstance().getItemRenderer().renderItem(
                new ItemStack(UItems.GEM),
                ModelTransformation.Mode.GROUND, light,
                overlay,
                matrices,
                MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers()
        );

        matrices.pop();
        //body.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }

}

package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.entity.mob.IgnimeousBulbEntity;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class IgnimeousBulbEntityModel extends EntityModel<IgnimeousBulbEntity> {

    private final ModelPart part;

    private final ModelPart head;
    private final ModelPart leaves;

    public IgnimeousBulbEntityModel(ModelPart root) {
        super(RenderLayer::getEntityTranslucent);
        this.part = root;
        head = root.getChild("head");
        leaves = root.getChild("leaves");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData head = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-24.0432F, -0.9905F, -48.1305F, 48, 23, 48, Dilation.NONE), ModelTransform.of(0, 7, 24, -0.1308F, 0.0057F, 0.0433F));
        head.addChild("jaw", ModelPartBuilder.create().uv(112, 0).cuboid(-16, -27, 5, 32, 0, 32, Dilation.NONE), ModelTransform.of(1.3731F, -21.6152F, -11.61F, -1.9802F, 0.1003F, 0.0006F));
        head.addChild("head", ModelPartBuilder.create().uv(0, 71).cuboid(-23, -16, -46, 46, 23, 46, Dilation.NONE), ModelTransform.of(-0.0432F, -0.9905F, -0.1305F, -0.1309F, 0, 0));

        ModelPartData leaves = root.addChild("leaves", ModelPartBuilder.create(), ModelTransform.pivot(0, 27, -1));
        leaves.addChild("leaf_1", ModelPartBuilder.create().uv(112, 0).cuboid(-16, 3, -55, 32, 0, 32, Dilation.NONE), ModelTransform.rotation(2.8316F, -1.0127F, -3.0858F));
        leaves.addChild("leaf_2", ModelPartBuilder.create().uv(112, 0).cuboid(-14, 12, -60, 32, 0, 32, Dilation.NONE), ModelTransform.rotation(2.6063F, -0.075F, -3.1196F));
        leaves.addChild("leaf_3", ModelPartBuilder.create().uv(112, 0).cuboid(-20, 1, -51, 32, 0, 32, Dilation.NONE), ModelTransform.rotation(-0.7079F, -1.4622F, 0.4842F));
        leaves.addChild("leaf_4", ModelPartBuilder.create().uv(112, 0).cuboid(-16, 6, -58, 32, 0, 32, Dilation.NONE), ModelTransform.rotation(-0.4967F, 0.1003F, 0.0006F));
        leaves.addChild("leaf_5", ModelPartBuilder.create().uv(-64, 140).cuboid(-30, 7, -83, 64, 0, 64, Dilation.NONE), ModelTransform.rotation(2.8708F, -0.1388F, -3.0651F));
        leaves.addChild("leaf_6", ModelPartBuilder.create().uv(-64, 140).cuboid(-30, 7, -83, 64, 0, 64, Dilation.NONE), ModelTransform.rotation(-1.084F, -1.4961F, 0.807F));
        leaves.addChild("leaf_7", ModelPartBuilder.create().uv(-64, 140).cuboid(-30, 3, -83, 64, 0, 64, Dilation.NONE), ModelTransform.rotation(-0.2651F, -0.035F, -0.0332F));
        leaves.addChild("leaf_8", ModelPartBuilder.create().uv(-64, 140).cuboid(-30, 7, -83, 64, 0, 64, Dilation.NONE), ModelTransform.rotation(-1.95F, 1.4932F, -1.6857F));
        leaves.addChild("leaf_9", ModelPartBuilder.create().uv(112, 0).cuboid(-16, 10, -57, 32, 0, 32, Dilation.NONE), ModelTransform.rotation(-2.5197F, 1.466F, -2.0202F));
        leaves.addChild("leaf_10", ModelPartBuilder.create().uv(112, 0).cuboid(-8, 3, -61, 32, 0, 32, Dilation.NONE), ModelTransform.rotation(2.9628F, 0.6133F, -2.978F));
        leaves.addChild("leaf_11", ModelPartBuilder.create().uv(112, 0).cuboid(-13, 6, -60, 32, 0, 32, Dilation.NONE), ModelTransform.rotation(-0.4768F, 0.8786F, -0.1186F));
        leaves.addChild("leaf_12", ModelPartBuilder.create().uv(112, 0).cuboid(-16, 0, -59, 32, 0, 32, Dilation.NONE), ModelTransform.rotation(-0.2911F, -0.5857F, 0.0605F));

        return TexturedModelData.of(data, 256, 256);
    }

    @Override
    public void setAngles(IgnimeousBulbEntity entity, float limbSwing, float limbSwingAmount, float tickDelta, float yaw, float pitch) {

        float age = entity.age + tickDelta;

        head.yScale = 1 - MathHelper.sin(age * 0.05F) * 0.02F;

        float hScale = 1 + MathHelper.cos(age * 0.06F) * 0.02F;
        head.xScale = hScale;
        head.zScale = hScale;

        head.pitch = MathHelper.sin(age * 0.02F) * 0.02F;
        head.yaw = MathHelper.cos(age * 0.02F) * 0.02F;

        leaves.yScale = 1 + MathHelper.sin(age * 0.05F) * 0.12F;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        part.render(matrices, vertices, light, overlay);
    }
}

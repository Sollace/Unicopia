package com.minelittlepony.unicopia.client.render.entity;

import java.util.List;

import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;

public class AirBalloonEntityModel extends EntityModel<AirBalloonEntity> {

    private final ModelPart root;
    private ModelPart main;

    private float inflation;

    private boolean isBurner;
    private boolean isBalloon;
    private boolean isSandbags;

    private final List<ModelPart> ropes;
    private final List<ModelPart> sandbags;

    public AirBalloonEntityModel(ModelPart root) {
        this.root = root;
        isBurner = root.hasChild("burner");
        isSandbags = root.hasChild("sandbag_ne");
        isBalloon = root.hasChild("canopy");

        if (isBurner || isBalloon) {
            main = root.getChild(isBalloon ? "canopy" : "burner");
            ropes = List.of(
                    (isBurner ? root : main).getChild("rope_a"), (isBurner ? root : main).getChild("rope_b"),
                    (isBurner ? root : main).getChild("rope_c"), (isBurner ? root : main).getChild("rope_d")
            );
        } else {
            ropes = List.of();
        }

        sandbags = isSandbags ? List.of(
                root.getChild("sandbag_nw"), root.getChild("sandbag_sw"),
                root.getChild("sandbag_ne"), root.getChild("sandbag_se")
        ) : List.of();
    }

    public static TexturedModelData getBasketModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartData basket = root.addChild("basket", ModelPartBuilder.create().uv(0, 0).cuboid(-16, -1, -16, 32, 2, 30, Dilation.NONE), ModelTransform.pivot(0, 24, 0));
        basket.addChild("walls", ModelPartBuilder.create().uv(0, 66).cuboid(-17, -12, -16, 2, 11, 30, Dilation.NONE)
                .uv(64, 68).cuboid(15, -12, -16, 2, 11, 30, Dilation.NONE)
                .uv(80, 38).cuboid(-16, -12, -17, 32, 11, 2, Dilation.NONE)
                .uv(0, 32).cuboid(8, -12, 13, 8, 11, 2, Dilation.NONE)
                .uv(0, 6).cuboid(-16, -12, 13, 8, 11, 2, Dilation.NONE), ModelTransform.NONE);
        basket.addChild("rim", ModelPartBuilder.create().uv(40, 34).cuboid(-18, -13, -17, 4, 2, 32, Dilation.NONE)
                .uv(0, 32).cuboid(14, -13, -17, 4, 2, 32, Dilation.NONE)
                .uv(80, 32).cuboid(-17, -13, -18, 34, 2, 4, Dilation.NONE)
                .uv(0, 19).cuboid(7, -13, 12, 10, 2, 4, Dilation.NONE)
                .uv(0, 0).cuboid(-17, -13, 12, 10, 2, 4, Dilation.NONE), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 256, 128);
    }

    public static TexturedModelData getBurnerModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        root.addChild("burner", ModelPartBuilder.create().uv(8, 0).cuboid(-5.5F, -47, -5.5F, 11, 15, 11, Dilation.NONE), ModelTransform.pivot(0, 24, 0));
        float angle = 0.37854F;
        float half = MathHelper.HALF_PI;
        root.addChild("rope_d", ModelPartBuilder.create().cuboid(0, -68, 0, 2, 66, 2, Dilation.NONE), ModelTransform.of(-0, -20, -0, angle, 0, -angle));
        root.addChild("rope_c", ModelPartBuilder.create().cuboid(0, -68, 0, 2, 66, 2, Dilation.NONE), ModelTransform.of(-0, -20, -0, -angle, 0, -angle));
        root.addChild("rope_b", ModelPartBuilder.create().cuboid(0, -68, 0, 2, 66, 2, Dilation.NONE), ModelTransform.of( 0, -20,  0, -angle, 0,  angle));
        root.addChild("rope_a", ModelPartBuilder.create().cuboid(0, -68, 0, 2, 66, 2, Dilation.NONE), ModelTransform.of( 0, -20,  0,  angle, 0,  angle));

        root.addChild("strut_a", ModelPartBuilder.create()
                .cuboid(-27, -40, -30, 2, 40, 2, Dilation.NONE)
                .cuboid(-27,  0, -30, 2, 40, 2, Dilation.NONE)
                .cuboid( 27, -40, -30, 2, 40, 2, Dilation.NONE)
                .cuboid( 27,  0, -30, 2, 40, 2, Dilation.NONE)

                .cuboid(-27, -40, 26, 2, 40, 2, Dilation.NONE)
                .cuboid(-27,  0, 26, 2, 40, 2, Dilation.NONE)
                .cuboid( 27, -40, 26, 2, 40, 2, Dilation.NONE)
                .cuboid( 27,  0, 26, 2, 40, 2, Dilation.NONE), ModelTransform.of(0, -80, 0, half, 0,  0));
        root.addChild("strut_b", ModelPartBuilder.create()
                .cuboid(-27, -40, -20, 2, 40, 2, Dilation.NONE)
                .cuboid(-27,  0, -20, 2, 40, 2, Dilation.NONE)
                .cuboid( 27, -40, -20, 2, 40, 2, Dilation.NONE)
                .cuboid( 27,  0, -20, 2, 40, 2, Dilation.NONE)

                .cuboid(-27, -40, 30, 2, 40, 2, Dilation.NONE)
                .cuboid(-27,  0, 30, 2, 40, 2, Dilation.NONE)
                .cuboid( 27, -40, 30, 2, 40, 2, Dilation.NONE)
                .cuboid( 27,  0, 30, 2, 40, 2, Dilation.NONE), ModelTransform.of(0, -80, 0, half, half, 0));
        return TexturedModelData.of(modelData, 64, 128);
    }

    public static TexturedModelData getCanopyModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartData balloon = root.addChild("canopy", ModelPartBuilder.create().cuboid(-54, -178, -59, 112, 120, 112, Dilation.NONE), ModelTransform.pivot(0, 24, 0));
        balloon.addChild("rope_d", ModelPartBuilder.create().cuboid(-2, -68, 0, 2, 68, 2, Dilation.NONE), ModelTransform.of(-14, -11, -16, 0.4363F, 0, -0.4363F));
        balloon.addChild("rope_c", ModelPartBuilder.create().cuboid(-2, -68, 0, 2, 68, 2, Dilation.NONE), ModelTransform.of(-14, -11, 11, -0.4363F, 0, -0.4363F));
        balloon.addChild("rope_b", ModelPartBuilder.create().cuboid(-2, -68, 0, 2, 68, 2, Dilation.NONE), ModelTransform.of(17, -11, 11, -0.4363F, 0, 0.4363F));
        balloon.addChild("rope_a", ModelPartBuilder.create().cuboid(-2, -68, 0, 2, 68, 2, Dilation.NONE), ModelTransform.of(17, -11, -16, 0.4363F, 0, 0.4363F));
        return TexturedModelData.of(modelData, 512, 256);
    }

    public static TexturedModelData getSandbagsModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        float offset = 40;
        getHangingBagModelData("sandbag_ne", root, -offset, -offset);
        getHangingBagModelData("sandbag_nw", root, -offset,  offset);
        getHangingBagModelData("sandbag_se", root,  offset, -offset);
        getHangingBagModelData("sandbag_sw", root,  offset,  offset);
        return TexturedModelData.of(modelData, 32, 32);
    }

    public static void getHangingBagModelData(String name, ModelPartData root, float x, float z) {
        ModelPartData bag = root.addChild(name, ModelPartBuilder.create()
                .uv(16, 19).cuboid(-0.5F, 0, -0.5F, 1, 9, 1, Dilation.NONE), ModelTransform.pivot(x, -35, z));
        ModelPartData knot = bag.addChild("knot", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-3, 1, -3, 6, 7, 6, Dilation.NONE)
                .uv(12, 14).cuboid(-2, 0, -2, 4, 1, 4, Dilation.NONE)
                .uv(0, 13).cuboid(-2, 8, -2, 4, 1, 4, Dilation.NONE), ModelTransform.pivot(0, 9, 0));
        knot.addChild("cube_r1", ModelPartBuilder.create().uv(8, 14).cuboid(0, 8, -2, 0, 4, 4, Dilation.NONE), ModelTransform.of(0, 1, 0, 0, -0.7854F, 0));
        knot.addChild("cube_r2", ModelPartBuilder.create().uv(8, 14).cuboid(0, 8, -2, 0, 4, 4, Dilation.NONE), ModelTransform.of(0, 1, 0, 0, 0.7854F, 0));
    }

    @Override
    public void setAngles(AirBalloonEntity entity, float limbDistance, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float tickDelta = MinecraftClient.getInstance().getTickDelta();
        inflation = entity.getInflation(tickDelta);

        root.yaw = MathHelper.PI;

        float burnerWiggleProgress = entity.getBurner().getPullProgress(tickDelta);

        if (isBurner || isBalloon || isSandbags) {
            root.roll = MathHelper.clamp(entity.getXVelocity(tickDelta), -0.5F, 0.5F);
            root.pitch = MathHelper.clamp(entity.getZVelocity(tickDelta), -0.5F, 0.5F);
            if (entity.isLeashed()) {
                root.roll *= -1;
                root.pitch *= -1;
            }
        } else {
            root.pitch = 0;
            root.roll = 0;
        }

        ropes.forEach(ModelPart::resetTransform);

        if (isBurner) {
            boolean lifted = inflation > 0.8F;
            root.pivotY = 32 * (1 - inflation) - (9 * inflation);
            root.pivotX = inflation * MathHelper.sin(limbSwingAmount + entity.age / 5F) / 4F;
            ropes.forEach(rope -> {
                rope.visible = lifted;
            });

            root.pivotX += burnerWiggleProgress * MathHelper.sin((entity.age + tickDelta)) * 2.5F;
            root.pivotX += burnerWiggleProgress * MathHelper.cos((entity.age + tickDelta)) * 2.5F;
            root.pivotY += burnerWiggleProgress * 7;
        }
        if (isBalloon || isSandbags) {
            root.pivotY = burnerWiggleProgress * 3;
            root.pivotX = inflation * MathHelper.cos(limbSwingAmount + entity.age / 5F) / 4F;
            if (entity.getBasketType().isOf(BoatEntity.Type.BAMBOO)) {
                ropes.forEach(rope -> rope.pivotY = 0);
            }
        }

        if (isSandbags) {
            float cosWiggle = MathHelper.cos(limbSwingAmount + entity.age / 5F) / 80F;
            float sinWiggle = MathHelper.sin(limbSwingAmount + entity.age / 5F) / 80F;
            for (int i = 0; i < sandbags.size(); i++) {
                ModelPart bag = sandbags.get(i);
                float pullProgress = entity.getSandbag(i).getPullProgress(tickDelta);
                bag.resetTransform();
                bag.pitch -= root.pitch * 2.5F * (1 + pullProgress) + cosWiggle;
                bag.roll -= root.roll * 2.5F * (1 + pullProgress) + sinWiggle;
                if (entity.isLeashed()) {
                    bag.roll *= -1;
                    bag.pitch *= -1;
                }
                float pullAmount = 2 + (2 * pullProgress);
                bag.yScale = pullAmount;
                bag.getChild("knot").yScale = 1/pullAmount;
            }
        }

        for (int i = 0; i < ropes.size(); i++) {
            ModelPart rope = ropes.get(i);
            float rollRatio = root.roll / rope.roll;
            float pitchRatio = root.pitch / rope.pitch;

            rope.pivotY -= 5F * rollRatio;
            rope.pivotY -= 5F * pitchRatio;

            if (i == 0 || i == 3) {
                rope.pivotZ -= 5 * pitchRatio;
            }
            if (i == 2 || i == 1) {
                rope.pivotZ += 5 * pitchRatio;
            }

            if (i == 2 || i == 3) {
                rope.pivotX -= 5 * rollRatio;
            }
            if (i == 0 || i == 1) {
                rope.pivotX += 5 * rollRatio;
            }

            if (isBalloon) {
                double speed = Math.abs(entity.getVelocity().getY()) * 3F;

                rope.zScale = MathHelper.clamp((float)speed, 0.25F, 1F);
                rope.xScale = 0.001F;
            } else {
                rope.xScale = 0.3F;
                rope.zScale = 0.3F;
            }
        }

    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float r, float g, float b, float a) {
        if (isBalloon) {
            matrices.push();
            matrices.translate(0, 1 * (1 - inflation), 0);
            matrices.scale(1, MathHelper.lerp(inflation, -0.05F, 1), 1);
            root.render(matrices, vertexConsumer, light, overlay, r, g, b, a);
            matrices.pop();
        } else {
            root.render(matrices, vertexConsumer, light, overlay, r, g, b, a);
        }
    }
}
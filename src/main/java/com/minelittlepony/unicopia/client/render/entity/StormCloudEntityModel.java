package com.minelittlepony.unicopia.client.render.entity;

import java.util.List;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.entity.StormCloudEntity;

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
import net.minecraft.util.math.random.Random;

public class StormCloudEntityModel extends EntityModel<StormCloudEntity> {
	private final ModelPart part;

	private final ModelPart smallPuffs;
	private final List<ModelPart> smallPuffCubes;

	private final ModelPart anvilHeads;
	private final List<ModelPart> anvilHeadCubes;

	private final ModelPart puff;

	private final Random rng = Random.create(0);
	private final Vector3f puffLocation = new Vector3f();

	public StormCloudEntityModel(ModelPart root) {
	    super(RenderLayer::getEntityTranslucent);
		this.part = root;
		this.smallPuffs = part.getChild("small_puffs");
		this.smallPuffCubes = smallPuffs.traverse().toList();
		this.anvilHeads = part.getChild("anvil_heads");
		this.anvilHeadCubes = anvilHeads.traverse().toList();
		this.puff = part.getChild("puff");
	}

	public static TexturedModelData getTexturedModelData() {
	    ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        ModelPartData small_puffs = root.addChild("small_puffs", ModelPartBuilder.create(), ModelTransform.pivot(0, 24, 0));

        small_puffs.addChild("cube_r1", ModelPartBuilder.create()
                .uv(0, 37).cuboid(-2, -20, -20, 7, 17, 20, Dilation.NONE)
                .uv(0, 37).cuboid(-2, -13, -40, 7, 17, 20, Dilation.NONE), ModelTransform.of(0, -6, 0, 0, 0, 1.5708F));

        small_puffs.addChild("cube_r2", ModelPartBuilder.create().uv(0, 37).cuboid(-2, -7, -25, 7, 17, 20, Dilation.NONE), ModelTransform.of(0, -6, 0, 0.1745F, 0, 1.5708F));
        small_puffs.addChild("cube_r3", ModelPartBuilder.create().uv(0, 37).cuboid(-7, -7, -7, 7, 17, 20, Dilation.NONE), ModelTransform.of(-15, -1, 15, 0.2182F, 0, 1.5708F));
        small_puffs.addChild("cube_r4", ModelPartBuilder.create().cuboid(-13, -14, -31, 7, 17, 20, Dilation.NONE), ModelTransform.of(2, -2, 19, 0, 0, 1.6581F));
        small_puffs.addChild("cube_r5", ModelPartBuilder.create().uv(0, 37).cuboid(-6, -17, -6, 7, 17, 20, Dilation.NONE), ModelTransform.of(2, -2, 19, -0.3491F, 0, 1.5708F));
        small_puffs.addChild("cube_r6", ModelPartBuilder.create().cuboid(-3.5F, -4.5F, -23, 7, 17, 20, Dilation.NONE), ModelTransform.of(0.4471F, -12.1925F, 36, 0, 0, -3.1416F));
        small_puffs.addChild("cube_r7", ModelPartBuilder.create().uv(0, 37).cuboid(-6, -17, -12, 7, 17, 20, Dilation.NONE), ModelTransform.of(-15, -2, 26, -0.829F, 0, 1.5708F));
        small_puffs.addChild("cube_r8", ModelPartBuilder.create()
                .uv(0, 37).cuboid(-6, -20, -21, 7, 17, 20, Dilation.NONE)
                .cuboid(-13, -20, -40, 7, 17, 20, Dilation.NONE), ModelTransform.of(-15, -2, 26, 0, 0, 1.5708F));

        small_puffs.addChild("cube_r9", ModelPartBuilder.create().cuboid(-15, -13, -18, 7, 17, 20, Dilation.NONE), ModelTransform.of(-15, -1, 15, 0, 0, 1.4835F));
        small_puffs.addChild("cube_r10", ModelPartBuilder.create().uv(0, 37).cuboid(-7, -3, -25, 7, 17, 20, Dilation.NONE), ModelTransform.of(-15, -1, 15, 0, 0, 1.5708F));
        small_puffs.addChild("cube_r11", ModelPartBuilder.create().uv(0, 37).cuboid(-2, -4, -7, 7, 17, 20, Dilation.NONE), ModelTransform.of(0, -6, 0, -0.0873F, 0, 1.5708F));
        small_puffs.addChild("cube_r12", ModelPartBuilder.create().cuboid(-9.5F, -8.5F, -10, 7, 17, 20, Dilation.NONE), ModelTransform.of(8.842F, -13.6476F, -20.7083F, 0.1739F, 0.0151F, -0.0423F));
        small_puffs.addChild("cube_r13", ModelPartBuilder.create().uv(0, 37).cuboid(-2, -19, -6, 7, 17, 20, Dilation.NONE), ModelTransform.of(0, -6, 0, -0.3043F, 0, 1.5708F));

        ModelPartData anvil_heads = root.addChild("anvil_heads", ModelPartBuilder.create(), ModelTransform.pivot(-2, 1, 0));
        anvil_heads.addChild("cube_r14", ModelPartBuilder.create().cuboid(-3.5F, -1.5F, -4, 7, 17, 20, new Dilation(2)), ModelTransform.of(-5.5F, -4.5F, 16, -0.3927F, 0, 0));
        anvil_heads.addChild("cube_r15", ModelPartBuilder.create().cuboid(-2, -18, 4, 7, 17, 20, new Dilation(5)), ModelTransform.of(0, -6, 0, -0.1309F, 0, 0));
        anvil_heads.addChild("cube_r16", ModelPartBuilder.create().cuboid(0, -1.5F, -12.5F, 7, 17, 20, new Dilation(5)), ModelTransform.of(0, -6, 0, 0.3491F, 0, 0));
        anvil_heads.addChild("cube_r17", ModelPartBuilder.create().cuboid(7, 9, 0, 7, 17, 20, new Dilation(2)), ModelTransform.of(0, -6, 0, 0.2618F, 0, 0));
        anvil_heads.addChild("cube_r18", ModelPartBuilder.create().cuboid(-12, 3, -9, 7, 17, 20, new Dilation(2)), ModelTransform.of(0, -6, 0, 0.3927F, 0, 0));

        root.addChild("puff", ModelPartBuilder.create()
                .uv(0, 77).cuboid(-6, -11, -5, 18, 5, 9, Dilation.NONE)
                .uv(0, 80).cuboid(-8, -10, 4, 14, 4, 6, Dilation.NONE)
                .uv(0, 74).cuboid(-2, -10, -11, 12, 5, 11, Dilation.NONE)
                .uv(7, 77).cuboid(-14, -11, -10, 12, 5, 9, Dilation.NONE)
                .uv(0, 79).cuboid(-2, -13, 0, 8, 3, 6, Dilation.NONE), ModelTransform.of(-5, 14, -20, 0, 1.5272F, 0));

        return TexturedModelData.of(data, 64, 128);
	}

	@Override
	public void setAngles(StormCloudEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	    float dir = 1;
	    float globalScale = 0.012F;

	    float roll = MathHelper.cos(entity.age / 10F) * globalScale;
	    float flop = MathHelper.sin(entity.age / 30F) * 0.02F * globalScale;

	    for (ModelPart swirl : smallPuffCubes) {
            swirl.resetTransform();
            dir = -dir;
            swirl.yaw += roll * 0.01F * dir;
        }

	    for (ModelPart swirl : anvilHeadCubes) {
	        swirl.resetTransform();
	        dir = -dir;
	        swirl.pitch += flop + roll * -0.01F * dir;
	        swirl.yaw += roll * 0.01F * dir;
	        swirl.roll += -flop + roll * 0.01F * dir;
	    }

	    part.pivotY = MathHelper.sin(entity.age * 0.25F) * 0.03F * globalScale;
	    part.pivotX = MathHelper.cos(entity.age * 0.05125F) * 0.7F * globalScale;
	    part.pivotZ = MathHelper.sin(entity.age * 0.05125F) * 0.7F * globalScale;

	    rng.setSeed(entity.getId());
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
	    matrices.push();
	    part.rotate(matrices);
	    smallPuffs.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	    anvilHeads.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);


	    int puffCount = rng.nextInt(7);
	    for (int i = 0; i < puffCount; i++) {
	        puff.resetTransform();
	        puff.translate(puffLocation.set(
	                rng.nextGaussian(),
	                rng.nextGaussian(),
	                rng.nextGaussian()
            ).mul(16));
	        puff.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	    }
	    matrices.pop();
	}
}
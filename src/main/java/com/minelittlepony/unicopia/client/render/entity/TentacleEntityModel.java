package com.minelittlepony.unicopia.client.render.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.minelittlepony.unicopia.entity.mob.TentacleEntity;

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

public class TentacleEntityModel extends EntityModel<TentacleEntity> {
	private final ModelPart part;

	private final Map<String, ModelPart> parts = new HashMap<>();
	private final List<ModelPart> bones = new ArrayList<>();
	private final List<ModelPart> brushes;

	private final ModelPart tip;

	public TentacleEntityModel(ModelPart root) {
	    super(RenderLayer::getEntityTranslucent);
		this.part = root;
		for (String key : List.of("bone_a", "bone_b", "bone_c", "bone_d", "bone_e", "bone_f", "bone_g", "bone_h")) {
    		parts.put(key, root = root.getChild(key));
    		bones.add(root);
		}
		var bone_a = parts.get("bone_a");
		brushes = List.of(
		        bone_a.getChild("brush_1"),
		        bone_a.getChild("brush_2"),
		        bone_a.getChild("brush_3"),
		        bone_a.getChild("brush_4"),
		        parts.get("bone_c").getChild("flower_1"),
		        parts.get("bone_d").getChild("flower_2"),
		        parts.get("bone_f").getChild("flower_3"));
		tip = parts.get("bone_h").getChild("tip");
	}

	public static TexturedModelData getTexturedModelData() {
	    ModelData data = new ModelData();
        data.getRoot()
                .addChild("bone_a", ModelPartBuilder.create().uv(0, 0).cuboid(-7, -10, -7, 14, 16, 14, Dilation.NONE), ModelTransform.pivot(0, 24, 0))
                .addChild("bone_b", ModelPartBuilder.create().uv(0, 30).cuboid(-6, -18, -6, 12, 19, 12, Dilation.NONE), ModelTransform.of(0, -9, 0, -0.2618F, 0, 0))
                .addChild("bone_c", ModelPartBuilder.create().uv(48, 20).cuboid(-5, -23, -5, 10, 23, 10, Dilation.NONE), ModelTransform.of(0, -16, 0, 0, 0, 0))
                .addChild("bone_d", ModelPartBuilder.create().uv(40, 53).cuboid(-4, -23, -4, 8, 21, 8, Dilation.NONE), ModelTransform.of(0, -18, 0, -0.1745F, 0, 0))
                .addChild("bone_e", ModelPartBuilder.create().uv(0, 61).cuboid(-3, -25, -3, 6, 22, 6, Dilation.NONE), ModelTransform.of(0, -18, 0, 0.1745F, 0, 0))
                .addChild("bone_f", ModelPartBuilder.create().uv(72, 53).cuboid(-3, -17, -3, 6, 15, 6, Dilation.NONE), ModelTransform.of(0, -22, 0, 0.1745F, 0, 0))
                .addChild("bone_g", ModelPartBuilder.create().uv(56, 0).cuboid(-2.6F, -16, -2.6F, 5.2F, 15, 5.2F, Dilation.NONE), ModelTransform.of(0, -15, 0, 0.3054F, 0, 0))
                .addChild("bone_h", ModelPartBuilder.create().uv(24, 61).cuboid(-2.1F, -17, -2.1F, 4.2F, 15, 4.2F, Dilation.NONE), ModelTransform.of(0, -13, 0, 0.2618F, 0, 0));
        return TexturedModelData.of(addFlowers(data), 128, 128);
	}

	private static ModelData addFlowers(ModelData data) {
        ModelPartData bone_a = data.getRoot().getChild("bone_a");
        createFlowerSinglet(createFlowerSinglet(bone_a.addChild("brush_1", ModelPartBuilder.create(), ModelTransform.of(-3, 0.6703F, -7.7725F,  0.6103F, -0.0535F, -0.5864F))).addChild("bundle", ModelPartBuilder.create(), ModelTransform.rotation(0.6103F, -0.0535F, -0.5864F)));
        createFlowerSinglet(createFlowerSinglet(bone_a.addChild("brush_2", ModelPartBuilder.create(), ModelTransform.of(-5, 0.6703F,  4.2275F, -1.2698F,  0.9678F, -1.7981F))).addChild("bundle", ModelPartBuilder.create(), ModelTransform.rotation(-1.2698F, 0.9678F, -1.7981F)));
        createFlowerSinglet(createFlowerSinglet(bone_a.addChild("brush_3", ModelPartBuilder.create(), ModelTransform.of( 6, 0.6703F,  4.2275F, -2.4079F,  0.2344F, -2.5374F))).addChild("bundle", ModelPartBuilder.create(), ModelTransform.rotation(-2.4079F, 0.2344F, -2.5374F)));
        createFlowerSinglet(createFlowerSinglet(bone_a.addChild("brush_4", ModelPartBuilder.create(), ModelTransform.of( 6, 0.6703F, -6.7725F,  1.5929F, -0.909F,  -1.1179F))).addChild("bundle", ModelPartBuilder.create(), ModelTransform.rotation(2.9259F, -0.8201F, -2.1974F)));

        ModelPartData bone_c = bone_a.getChild("bone_b").getChild("bone_c");
        createFlowerSinglet(bone_c.addChild("flower_1", ModelPartBuilder.create(), ModelTransform.of( 4, -15.6242F, -3.3435F,  2.9259F, -0.8201F, -2.4592F)));

        ModelPartData bone_d = bone_c.getChild("bone_d");
        createFlowerSinglet(bone_d.addChild("flower_2", ModelPartBuilder.create(), ModelTransform.of(-2, -17.1355F,  3.0055F, -2.8606F, -0.5942F,  2.4482F)));
        ModelPartData bone_f = bone_d.getChild("bone_e").getChild("bone_f");
        createFlowerSinglet(bone_f.addChild("flower_3", ModelPartBuilder.create(), ModelTransform.of(-2, -6.0251F,   0.478F,   2.7587F,  0.3479F,  2.5432F)));

        ModelPartData flower_12 = createFlowerBunch(bone_f.getChild("bone_g").getChild("bone_h").addChild("tip", ModelPartBuilder.create(), ModelTransform.of(0, -15.1462F, 0.6365F, 2.7587F, 0.3479F, 2.9795F)));
        createFlowerBunch(flower_12.addChild("bundle_1", ModelPartBuilder.create(), ModelTransform.rotation( 2.7587F,  0.3479F, 2.5868F)));
        createFlowerBunch(flower_12.addChild("bundle_2", ModelPartBuilder.create(), ModelTransform.rotation(-1.9412F, -1.0444F, 1.7406F)));
        return data;
	}

	private static ModelPartData createFlowerSinglet(ModelPartData parent) {
        parent.addChild("flower_1", ModelPartBuilder.create().uv(86, 0).cuboid(-14, 0, 0, 14, 0, 14, Dilation.NONE), ModelTransform.rotation(1.5708F, 0, 0.7854F));
        parent.addChild("flower_2", ModelPartBuilder.create().uv(86, 0).cuboid(-14, 0, 0, 14, 0, 14, Dilation.NONE), ModelTransform.rotation(3.1416F, 0.7854F, 1.5708F));
        return parent;
	}

	private static ModelPartData createFlowerBunch(ModelPartData parent) {
	    parent.addChild("flower_1", ModelPartBuilder.create().uv(86, 0).cuboid(-50.2096F, 2.6718F, -13.6647F, 14, 0, 14, Dilation.NONE), ModelTransform.of(36.0432F, 15.5243F, -3.1436F, 1.5708F, 0, 0.7854F));
	    parent.addChild("flower_2", ModelPartBuilder.create().uv(86, 0).cuboid(-27.1617F, -35.2665F, 9.3832F, 14, 0, 14, Dilation.NONE), ModelTransform.of(36.0432F, 15.5243F, -3.1436F, 3.1416F, 0.7854F, 1.5708F));
	    return parent;
	}

	@Override
	public void setAngles(TentacleEntity entity, float limbSwing, float limbSwingAmount, float tickDelta, float yaw, float pitch) {

	    boolean growing = entity.getGrowth(tickDelta) < 1;

	    float age = entity.age + tickDelta + (entity.getUuid().getMostSignificantBits() % 100);
	    float idleWaveTimer = entity.getAnimationTimer(tickDelta);

	    float attackProgress = entity.isAttacking() ? Math.abs(MathHelper.sin(entity.getAttackProgress(tickDelta) * MathHelper.PI)) : 0;
	    float attackCurve = attackProgress * -0.5F;
	    float sweepDirection = 1;

	    float bendIntentisty = 1 + entity.getAttackProgress(tickDelta) / 2F;

        part.yaw = (yaw * MathHelper.RADIANS_PER_DEGREE) + MathHelper.HALF_PI * attackProgress;

	    for (ModelPart bone : bones) {
	        float idlePitch = MathHelper.sin(idleWaveTimer) * 0.0226F * bendIntentisty;
	        float idleYaw = MathHelper.cos(idleWaveTimer + 0.53F) * 0.07F;
	        float idleRoll = MathHelper.sin(idleWaveTimer * 0.2F) * 0.0226F * bendIntentisty;
	        idleWaveTimer += 1.5F;
	        bendIntentisty += 3F;
	        bone.resetTransform();

	        if (!growing) {
    	        bone.pitch = MathHelper.lerp(attackProgress, idlePitch, bone.pitch + attackCurve);
    	        bone.yaw = MathHelper.lerp(attackProgress, idleYaw, bone.yaw + sweepDirection * attackCurve);
    	        bone.roll = MathHelper.lerp(attackProgress, idleRoll, bone.roll);
	        }
	        attackCurve *= 1.04F;
	    }

	    float direction = 1;

	    for (ModelPart brush : brushes) {
	        brush.resetTransform();
	        brush.pitch += MathHelper.sin(age * 0.003F) * 0.03F;
	        brush.yaw += MathHelper.cos(age * 0.003F) * 0.1F * (direction *= -1);
	    }

	    tip.resetTransform();
	    tip.pitch += MathHelper.sin(age * 0.003F) * 0.3F;
	    tip.yaw += MathHelper.sin(age * 0.03F) * 0.3F;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
	    part.render(matrices, vertices, light, overlay, color);
	}
}
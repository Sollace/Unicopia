package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.projectile.MagicBeamEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MagicBeamEntityRenderer extends EntityRenderer<MagicBeamEntity> {
    private final Model model;

    public MagicBeamEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new Model(Model.getData().createModel());
    }

    @Override
    public Identifier getTexture(MagicBeamEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    protected int getBlockLight(MagicBeamEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(MagicBeamEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (entity.age < 2 && dispatcher.camera.getFocusedEntity().squaredDistanceTo(entity) < 8) {
            return;
        }

        float scale = 1 - tickDelta/30F;

        matrices.push();
        matrices.scale(scale, scale, scale);

        model.setAngles(entity, 0, 0, tickDelta,
                entity.getYaw(tickDelta) * MathHelper.RADIANS_PER_DEGREE,
                -entity.getPitch(tickDelta) * MathHelper.RADIANS_PER_DEGREE
        );

        RenderLayer layer = entity.getSpellSlot().get()
                .map(spell -> (0x99 << 24) | spell.getTypeAndTraits().type().getColor())
                .map(RenderLayers::getMagicColored)
                .orElseGet(RenderLayers::getMagicColored);

        model.render(matrices, vertexConsumers.getBuffer(layer), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    public class Model extends EntityModel<MagicBeamEntity> {

        private final ModelPart part;

        public Model(ModelPart part) {
            super(texture -> RenderLayers.getMagicColored());
            this.part = part;
        }

        static TexturedModelData getData() {
            ModelData data = new ModelData();
            ModelPartData tree = data.getRoot();

            tree.addChild("beam", ModelPartBuilder.create()
                    .cuboid(0, 0, 0, 1, 1, 17)
                    .cuboid(0, 0, 0, 1, 1, 17, new Dilation(0.25F)), ModelTransform.NONE);

            return TexturedModelData.of(data, 64, 64);
        }

        @Override
        public void setAngles(MagicBeamEntity entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
            part.pitch = headPitch;
            part.yaw = headYaw;
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
            part.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        }
    }
}

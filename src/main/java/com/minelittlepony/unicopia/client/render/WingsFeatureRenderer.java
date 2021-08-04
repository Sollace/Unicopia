package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.Model;
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
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class WingsFeatureRenderer<E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<E> {

    private static final Identifier ICARUS_WINGS = new Identifier("unicopia", "textures/models/wings/icarus.png");
    private static final Identifier ICARUS_WINGS_CORRUPTED = new Identifier("unicopia", "textures/models/wings/icarus_corrupted.png");
    private static final Identifier PEGASUS_WINGS = new Identifier("unicopia", "textures/models/wings/pegasus.png");

    private final WingsModel model;

    private final FeatureRendererContext<E, ? extends BipedEntityModel<E>> context;

    public WingsFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {
        this.context = context;
        this.model = new WingsModel(WingsModel.getData(Dilation.NONE).createModel());
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider renderContext, int lightUv, E entity, float limbDistance, float limbAngle, float tickDelta, float age, float headYaw, float headPitch) {

        boolean pegasus = (entity instanceof PlayerEntity && Pony.of((PlayerEntity)entity).getSpecies().canInteractWithClouds());
        boolean icarus = UItems.PEGASUS_AMULET.isApplicable(entity);

        if (icarus || pegasus) {
            Identifier texture = pegasus ? PEGASUS_WINGS : entity.world.getDimension().isUltrawarm() ? ICARUS_WINGS_CORRUPTED : ICARUS_WINGS;
            VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getEntityTranslucent(texture), false, false);

            model.setAngles(entity, context.getModel());
            model.render(matrices, consumer, lightUv, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        }
    }

    static class WingsModel extends Model {

        private final ModelPart root;
        private final Wing[] wings;

        public WingsModel(ModelPart tree) {
            super(RenderLayer::getEntityTranslucent);
            root = tree;
            wings = new Wing[] {
                    new Wing(tree.getChild("left_wing"), -1),
                    new Wing(tree.getChild("right_wing"), 1)
            };
        }

        static TexturedModelData getData(Dilation dilation) {
            ModelData data = new ModelData();
            Wing.getData("left_wing", data.getRoot(), dilation, -1);
            Wing.getData("right_wing", data.getRoot(), dilation, 1);
            return TexturedModelData.of(data, 24, 23);
        }

        public void setAngles(LivingEntity entity, BipedEntityModel<?> biped) {
            root.copyTransform(biped.body);
            for (Wing wing : wings) {
                wing.setAngles(entity);
            }
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
            root.render(matrices, vertexConsumer, i, j, f, g, h, k);
        }

        static class Wing {
            final ModelPart base;

            final ModelPart[] feathers = new ModelPart[8];

            final int k;

            Wing(ModelPart tree, int k) {
                this.k = k;
                base = tree;
                for (int i = 0; i < feathers.length; i++) {
                    feathers[i] = base.getChild("feather_" + i);
                }
            }

            static void getData(String name, ModelPartData parent, Dilation dilation, int k) {
                ModelPartData base = parent.addChild(name,
                        ModelPartBuilder.create().cuboid(0, 0, 0, 2, 10, 2, dilation),
                        ModelTransform.pivot(k * 2, 2, 2 + k * 0.5F));

                int featherCount = 8;

                for (int i = 0; i < featherCount; i++) {
                    int texX = (i % 2) * 8;
                    int featherLength = 21 - i * 2;
                    base.addChild("feather_" + i,
                            ModelPartBuilder.create()
                                .uv(8 + texX, 0)
                                .cuboid(-k * (i % 2) / 90F, 0, 0, 2, featherLength, 2, dilation),
                            ModelTransform.pivot(0, 9, 0));
                }
            }

            void setAngles(LivingEntity entity) {
                float spreadAmount = entity instanceof PlayerEntity ? Pony.of((PlayerEntity)entity).getMotion().getWingAngle() : 0;

                base.pitch = 1.5F + 0.8F - spreadAmount / 9F;
                base.yaw = k * (0.8F + spreadAmount / 3F);

                spreadAmount /= 7F;

                final float ratio = 4F;

                for (int i = 0; i < feathers.length; i++) {

                    float spread = i/ratio + 1.5F;
                    spread -= spreadAmount * ratio;
                    spread += spreadAmount * i / ratio;

                    feathers[i].pitch = -spread;
                    feathers[i].yaw = k * 0.3F;
                }
            }
        }
    }
}

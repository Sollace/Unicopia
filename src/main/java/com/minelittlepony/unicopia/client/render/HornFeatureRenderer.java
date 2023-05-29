package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.ability.AbilityDispatcher.Stat;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;

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
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;

public class HornFeatureRenderer<E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<E> {

    public static final Identifier TEXTURE = Unicopia.id("textures/models/horn/unicorn.png");

    private final HornModel model;

    private final FeatureRendererContext<E, ? extends BipedEntityModel<E>> context;

    public HornFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {
        this.context = context;
        model = new HornModel(HornModel.getData(Dilation.NONE).createModel());
    }

    protected boolean canRender(E entity) {
        return entity instanceof PlayerEntity player && Pony.of(player).getObservedSpecies().canCast();
    }

    @Override
    public void render(MatrixStack stack, VertexConsumerProvider renderContext, int lightUv, E entity, float limbDistance, float limbAngle, float tickDelta, float age, float headYaw, float headPitch) {
        if (canRender(entity)) {
            model.setAngles(context.getModel());
            model.setState(false);
            model.render(stack, ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getArmorCutoutNoCull(TEXTURE), false, false), lightUv, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);

            Pony.of(entity).flatMap(pony -> {
                return pony.getAbilities().getActiveStat()
                        .flatMap(Stat::getActiveAbility)
                        .map(ability -> ability.getColor(pony))
                        .filter(i -> i != -1).or(() -> pony.getSpellSlot().get(SpellPredicate.IS_NOT_PLACED, false).map(spell -> spell.getType().getColor()));
            }).ifPresent(color -> {
                model.setState(true);
                model.render(stack, ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayers.getMagicColored(color), false, false), lightUv, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
            });
        }
    }

    public static class HornModel extends Model {

        private final ModelPart part;

        public HornModel(ModelPart tree) {
            super(RenderLayer::getEntityTranslucent);
            part = tree.getChild(EntityModelPartNames.HEAD);
        }

        public static TexturedModelData getData(Dilation dilation) {
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();

            ModelPartData head = root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-4, -8, -4, 8, 8, 8, dilation), ModelTransform.NONE);
            head.addChild("horn", ModelPartBuilder.create()
                        .uv(0, 3)
                        .cuboid(-0.5F, -11, -3.5F, 1, 4, 1, dilation), ModelTransform.rotation(29 * MathHelper.RADIANS_PER_DEGREE, 0, 0));
            head.addChild("magic", ModelPartBuilder.create()
                    .uv(0, 3)
                    .cuboid(-0.5F, -11, -3.5F, 1, 4, 1, dilation.add(0.5F)), ModelTransform.rotation(29 * MathHelper.RADIANS_PER_DEGREE, 0, 0));

            return TexturedModelData.of(data, 64, 64);
        }

        public void setAngles(BipedEntityModel<?> biped) {
            part.copyTransform(biped.getHead());
        }

        public void setState(boolean magic) {
            part.hidden = true;
            part.getChild("horn").visible = !magic;
            part.getChild("magic").visible = magic;
        }

        @Override
        public void render(MatrixStack matrixStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
            part.render(matrixStack, vertexConsumer, i, j, f, g, h, k);
        }
    }

}

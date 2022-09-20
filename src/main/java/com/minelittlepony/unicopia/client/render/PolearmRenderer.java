package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.client.minelittlepony.MineLPConnector;

import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.TridentEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class PolearmRenderer implements DynamicItemRenderer, UnclampedModelPredicateProvider {

    private static final PolearmRenderer INSTANCE = new PolearmRenderer();
    private static final Identifier THROWING = new Identifier("throwing");

    private final TridentEntityModel model = new TridentEntityModel(getTexturedModelData().createModel());

    public static void register(Item item) {
        BuiltinItemRendererRegistry.INSTANCE.register(item, INSTANCE);
        ModelPredicateProviderRegistry.register(item, THROWING, INSTANCE);
        ModelLoadingRegistry.INSTANCE.registerModelProvider((renderer, out) -> out.accept(getModelId(item)));
    }

    static ModelIdentifier getModelId(ItemConvertible item) {
        Identifier id = Registry.ITEM.getId(item.asItem());
        return new ModelIdentifier(id.toString() + "_in_inventory#inventory");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        int y = -9;

        ModelPartData pole = root.addChild("pole", ModelPartBuilder.create().uv(0, 6).cuboid(-0.5f, y, -0.5f, 1, 25, 1), ModelTransform.NONE);
        pole.addChild("base", ModelPartBuilder.create().uv(4, 0).cuboid(-1.5f, y - 2, -0.5f, 3, 2, 1), ModelTransform.NONE);
        pole.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-0.5f, y - 6, -0.5f, 1, 4, 1), ModelTransform.NONE);
        return TexturedModelData.of(data, 32, 32);
    }

    @Override
    public void render(ItemStack stack, Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        if (mode == Mode.GUI || mode == Mode.GROUND || mode == Mode.FIXED) {
            // render as normal sprite
            ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();

            BakedModel model = renderer.getModels().getModelManager().getModel(getModelId(stack.getItem()));
            matrices.pop();
            matrices.push();
            renderer.renderItem(stack, mode, false, matrices, vertexConsumers, light, overlay, model);
            matrices.pop();
            matrices.push();
        } else {
            matrices.push();
            matrices.scale(1, -1, -1);
            Identifier id = Registry.ITEM.getId(stack.getItem());
            Identifier texture = new Identifier(id.getNamespace(), "textures/entity/polearm/" + id.getPath() + ".png");
            model.render(matrices, MineLPConnector.getItemBuffer(vertexConsumers, texture).orElseGet(() -> {
                return ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, model.getLayer(texture), false, stack.hasGlint());
            }), light, overlay, 1, 1, 1, 1);
            matrices.pop();
        }
    }

    @Override
    public float unclampedCall(ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
        return entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1 : 0;
    }
}

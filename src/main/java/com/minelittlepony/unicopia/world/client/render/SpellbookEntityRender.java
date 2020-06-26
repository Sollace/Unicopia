package com.minelittlepony.unicopia.world.client.render;

import com.minelittlepony.unicopia.world.client.render.model.SpellbookModel;
import com.minelittlepony.unicopia.world.entity.SpellbookEntity;

import net.minecraft.util.math.MathHelper;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;

public class SpellbookEntityRender extends LivingEntityRenderer<SpellbookEntity, SpellbookModel> {

    private static final Identifier BLUE = new Identifier("unicopia", "textures/entity/enchanting_table_book_blue.png");
    private static final Identifier NORMAL = new Identifier("unicopia", "textures/entity/enchanting_table_book.png");

    public SpellbookEntityRender(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager, new SpellbookModel(), 0);
    }

    @Override
    public Identifier getTexture(SpellbookEntity entity) {
        return entity.getIsAltered() ? BLUE : NORMAL;
    }

    @Override
    protected float getLyingAngle(SpellbookEntity entity) {
        return 0;
    }

    @Override
    protected void setupTransforms(SpellbookEntity entity, MatrixStack matrices, float f, float g, float partialTicks) {
        super.setupTransforms(entity, matrices, f, g + 90, partialTicks);

        if (entity.getIsOpen()) {
            matrices.translate(-1.25F, -0.35F, 0);

            float floatPosition = MathHelper.sin((entity.age + partialTicks + entity.getEntityId()) / 20) * 0.04F;

            matrices.translate(0, floatPosition, 0);
            matrices.multiply(Vector3f.NEGATIVE_Z.getDegreesQuaternion(60));
        } else {
            matrices.translate(-1.5F, 0.1F, 0.2F);
            matrices.multiply(Vector3f.NEGATIVE_Z.getDegreesQuaternion(90));
            matrices.multiply(Vector3f.NEGATIVE_Y.getDegreesQuaternion(90));
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
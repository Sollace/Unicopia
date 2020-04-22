package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.client.render.model.SpellbookModel;
import com.minelittlepony.unicopia.entity.SpellbookEntity;
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
    protected void setupTransforms(SpellbookEntity entity, MatrixStack matrices, float p_77043_2_, float p_77043_3_, float partialTicks) {

        if (!entity.getIsOpen()) {
            matrices.translate(0, 1.44f, 0);
        } else {
            matrices.translate(0, 1.2f + MathHelper.sin((entity.age + partialTicks) / 20) * 0.01F + 0.1F, 0);
        }

        if (!entity.getIsOpen()) {
            matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90));
            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90));
            matrices.translate(-0.25f, 0, 0);
        } else {
            matrices.multiply(Vector3f.NEGATIVE_Z.getDegreesQuaternion(60));
        }

        matrices.multiply(Vector3f.NEGATIVE_Y.getDegreesQuaternion(MathHelper.lerp(entity.prevYaw, entity.yaw, partialTicks)));
    }

    @Override
    protected boolean hasLabel(SpellbookEntity targetEntity) {
        return super.hasLabel(targetEntity)
                && (targetEntity.isCustomNameVisible()
                        || targetEntity.hasCustomName()
                        && targetEntity == renderManager.targetedEntity);
    }
}
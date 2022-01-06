package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.IModel;
import com.minelittlepony.api.model.gear.IGear;
import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.client.render.BraceletFeatureRenderer;
import com.minelittlepony.unicopia.client.render.BraceletFeatureRenderer.BraceletModel;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.GlowableItem;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

class BangleGear implements IGear {
    private boolean glowing;
    private int color;
    private boolean alex;

    private final BraceletModel steveModel;
    private final BraceletModel alexModel;

    private IModel model;

    public BangleGear() {
        Dilation dilation = new Dilation(0.3F);
        steveModel = new BraceletModel(BraceletModel.getData(dilation, false, -1, 4, 0).createModel());
        alexModel = new BraceletModel(BraceletModel.getData(dilation, true, -1, 4, 0).createModel());
    }

    @Override
    public boolean canRender(IModel model, Entity entity) {
        return entity instanceof LivingEntity living
                && living.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof FriendshipBraceletItem;
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.LEGS;
    }

    @Override
    public <T extends Entity> Identifier getTexture(T entity, Context<T, ?> context) {
        return BraceletFeatureRenderer.TEXTURE;
    }

    @Override
    public void setModelAttributes(IModel model, Entity entity) {
        ItemStack item = ((LivingEntity)entity).getEquippedStack(EquipmentSlot.CHEST);

        color = ((DyeableItem)item.getItem()).getColor(item);
        glowing = ((GlowableItem)item.getItem()).isGlowing(item);
        alex = entity instanceof ClientPlayerEntity && ((ClientPlayerEntity)entity).getModel().startsWith("slim");
        this.model = model;

        BraceletModel m = alex ? alexModel : steveModel;

        if (model instanceof BipedEntityModel<?> biped) {
            m.setAngles(biped);
        }
        m.setVisible(((LivingEntity)entity).getMainArm());
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha, UUID interpolatorId) {
        popAndApply(model, BodyPart.LEGS, stack);

        BraceletModel m = alex ? alexModel : steveModel;

        m.render(stack, consumer, glowing ? 0x0F00F0 : light, overlay, Color.r(color), Color.g(color), Color.b(color), 1);
    }

    /**
     * Discards and applies default transformations without body part rotations.
     * <p>
     * TODO: this is a workaround to undo the {@code model.getBodyPart(part).rotate(stack)} in GearFeature.
     * That's useful for things that render on the head or body, but not so much if you're on the legs or tail,
     * since the default implementation falls to body rotation, which we don't want
     */
    static void popAndApply(IModel model, BodyPart part, MatrixStack matrices) {
        matrices.pop();
        matrices.push();
        // re-apply leg transformation
        model.transform(part, matrices);
    }
}

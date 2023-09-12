package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.IModel;
import com.minelittlepony.api.model.gear.IGear;
import com.minelittlepony.client.model.IPonyModel;
import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.client.render.BraceletFeatureRenderer;
import com.minelittlepony.unicopia.client.render.BraceletFeatureRenderer.BraceletModel;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.GlowableItem;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

class BangleGear implements IGear {
    private boolean glowing;
    private int color;
    private boolean alex;

    private final BraceletModel steveModel;
    private final BraceletModel alexModel;

    private final Identifier slot;

    public BangleGear(Identifier slot) {
        this.slot = slot;
        Dilation dilation = new Dilation(0.3F);
        steveModel = new BraceletModel(BraceletModel.getData(dilation, false, -1, 4, 0).createModel());
        alexModel = new BraceletModel(BraceletModel.getData(dilation, true, -1, 4, 0).createModel());
    }

    @Override
    public boolean canRender(IModel model, Entity entity) {
        return entity instanceof LivingEntity living && FriendshipBraceletItem.getWornBangles(living, slot).findFirst().isPresent();
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
    public <M extends EntityModel<?> & IPonyModel<?>> void transform(M model, MatrixStack matrices) {
        BodyPart part = getGearLocation();
        model.transform(part, matrices);
    }

    @Override
    public void pose(IModel model, Entity entity, boolean rainboom, UUID interpolatorId, float move, float swing, float bodySwing, float ticks) {
        alex = entity instanceof ClientPlayerEntity && ((ClientPlayerEntity)entity).getModel().startsWith("slim");
        FriendshipBraceletItem.getWornBangles((LivingEntity)entity, slot).findFirst().ifPresent(bracelet -> {
            color = ((DyeableItem)bracelet.getItem()).getColor(bracelet);
            glowing = ((GlowableItem)bracelet.getItem()).isGlowing(bracelet);
        });
        BraceletModel m = alex ? alexModel : steveModel;

        if (model instanceof BipedEntityModel<?> biped) {
            m.setAngles(biped);
        }
        Arm mainArm = ((LivingEntity)entity).getMainArm();
        m.setVisible(slot == TrinketsDelegate.MAINHAND ? mainArm : mainArm.getOpposite());
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha, UUID interpolatorId) {
        BraceletModel m = alex ? alexModel : steveModel;
        m.render(stack, consumer, glowing ? 0x0F00F0 : light, overlay, Color.r(color), Color.g(color), Color.b(color), 1);
    }
}

package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
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
import net.minecraft.client.util.SkinTextures.Model;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

class BangleGear implements Gear {
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
    public boolean canRender(PonyModel<?> model, Entity entity) {
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
    public <M extends EntityModel<?> & PonyModel<?>> void transform(M model, MatrixStack matrices) {
        BodyPart part = getGearLocation();
        model.transform(part, matrices);
    }

    @Override
    public void pose(PonyModel<?> model, Entity entity, boolean rainboom, UUID interpolatorId, float move, float swing, float bodySwing, float ticks) {
        alex = entity instanceof ClientPlayerEntity && ((ClientPlayerEntity)entity).getSkinTextures().model() == Model.SLIM;
        color = Colors.WHITE;
        glowing = false;
        FriendshipBraceletItem.getWornBangles((LivingEntity)entity, slot).findFirst().ifPresent(bracelet -> {
            color = DyedColorComponent.getColor(bracelet.stack(), Colors.WHITE);
            glowing = GlowableItem.isGlowing(bracelet.stack());
        });
        BraceletModel m = alex ? alexModel : steveModel;

        if (model instanceof BipedEntityModel<?> biped) {
            m.setAngles(biped);
        }
        Arm mainArm = ((LivingEntity)entity).getMainArm();
        m.setVisible(slot == TrinketsDelegate.MAIN_GLOVE ? mainArm : mainArm.getOpposite());
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, int color, UUID interpolatorId) {
        BraceletModel m = alex ? alexModel : steveModel;
        m.render(stack, consumer, glowing ? 0x0F00F0 : light, overlay, this.color);
    }
}

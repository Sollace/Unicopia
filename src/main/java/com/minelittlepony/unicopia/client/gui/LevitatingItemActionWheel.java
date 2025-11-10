package com.minelittlepony.unicopia.client.gui;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector4f;

import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgPlayerTargetEntity;
import com.minelittlepony.unicopia.util.Trace;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.Input;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class LevitatingItemActionWheel {

    @Nullable
    public LevitatingItemEntity targetEntity;
    private Vec3d targetPosition = Vec3d.ZERO;

    @Nullable
    public LevitatingItemEntity.Action currentAction;

    private int unfocusDelay;

    private final MinecraftClient client = MinecraftClient.getInstance();

    public void tick(MinecraftClient client, Pony pony) {
        if (currentAction == LevitatingItemEntity.Action.MOVE && targetEntity != null && unfocusDelay > 0) {
            unfocusDelay--;
            pony.setLookedEntity(targetEntity);
            return;
        }
        if (client.targetedEntity instanceof LevitatingItemEntity target) {
            targetEntity = target;
        } else if (pony.asEntity().getRootVehicle() instanceof LevitatingItemEntity target) {
            targetEntity = target;
        } else {
            targetEntity = Trace.create(pony.asEntity(), 100, client.getRenderTickCounter().getTickDelta(false), e -> {
                return e instanceof LevitatingItemEntity;
            }).<LevitatingItemEntity>getEntity().orElse(null);
        }
        if (targetEntity != null) {
            targetPosition = targetEntity.getPos();
        }
        pony.setLookedEntity(targetEntity);
    }


    public boolean handleInput(Input input) {
        if (currentAction == LevitatingItemEntity.Action.MOVE && targetEntity != null) {
            Vec2f horizontalMovement = input.getMovementInput();
            int verticalMovement = input.jumping ? 1 : input.sneaking ? -1 : 0;

            if (horizontalMovement.length() > 1.0E-5F || verticalMovement != 0) {
                unfocusDelay = 30;
                input.sneaking = false;

                Channel.CLIENT_PLAYER_LOOK_AT_ENTITY.sendToServer(new MsgPlayerTargetEntity(
                        Optional.of(targetEntity.getId()),
                        Optional.of(UHud.INSTANCE.levitatingItemActions.currentAction),
                        Optional.of(new Vec3d(horizontalMovement.x, verticalMovement, horizontalMovement.y)
                                .rotateY(-client.gameRenderer.getCamera().getYaw() * MathHelper.RADIANS_PER_DEGREE))
                ));
            }
        }
        return currentAction == LevitatingItemEntity.Action.MOVE;
    }

    private Vector2i getCrosshairPos() {
        var client = MinecraftClient.getInstance();
        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();

        Camera cam = client.gameRenderer.getCamera();
        Vec3d offset = (currentAction == LevitatingItemEntity.Action.MOVE ? targetPosition : targetEntity.getPos()).add(0, targetEntity.getHeight() * 0.5F, 0).subtract(cam.getPos());
        Quaternionf rotation = cam.getRotation().conjugate(new Quaternionf());
        Matrix4f matrix = new Matrix4f().identity().rotate(rotation);
        Vector4f projectedPosition = matrix.transform(new Vector4f((float)offset.x, (float)offset.y, (float)offset.z, 0));

        int crosshairX = -(int)(projectedPosition.x * scaledWidth);
        int crosshairY =  (int)(projectedPosition.y * scaledHeight);

        return new Vector2i(crosshairX, crosshairY);
    }

    public void render(DrawContext context, float tickDelta) {
        if (targetEntity == null) {
            currentAction = null;
            return;
        }
        Pony.of(client.player).setLookedEntity(targetEntity);

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();


        Vector2i crosshair = getCrosshairPos();

        MatrixStack matrices = context.getMatrices();

        matrices.push();
        matrices.translate(scaledWidth * 0.5, scaledHeight * 0.5, 0);
        DrawableUtil.drawLine(matrices, crosshair.x, crosshair.y - 5, crosshair.x, crosshair.y + 5, Colors.WHITE);
        DrawableUtil.drawLine(matrices, crosshair.x - 5, crosshair.y, crosshair.x + 5, crosshair.y, Colors.WHITE);

        drawOptionsWheel(context, crosshair, targetEntity.getDefaultAction(), targetEntity.getValidActions());

        matrices.pop();
    }

    private void drawOptionsWheel(DrawContext context, Vector2i crosshair, LevitatingItemEntity.Action defaultAction, List<LevitatingItemEntity.Action> options) {
        int ringInnerDiameter = 30;
        int ringOuterDiameter = 55;
        int ringMarkerWidth = 3;

        int iconSize = 17;
        int segmenticonRadius = ringInnerDiameter + ringInnerDiameter / 2;
        int segmentCount = options.size();
        double segmentAngle = DrawableUtil.TAU / segmentCount;
        double segmentsStartAngle = DrawableUtil.PI - segmentAngle / 2;

        double theta = Math.atan2(crosshair.x, -crosshair.y) + Math.PI;
        double rad = Math.max(crosshair.y / Math.cos(theta), -crosshair.x / Math.sin(theta));

        int selection = getSelection(theta, rad, segmentsStartAngle, segmentAngle);

        currentAction = selection < 0 || selection >= options.size() ? defaultAction : options.get(selection);

        DrawableUtil.drawArc(context.getMatrices(), ringInnerDiameter, ringInnerDiameter + 1, 0, DrawableUtil.TAU, 0xFFFFFF22);
        DrawableUtil.drawArc(context.getMatrices(), ringInnerDiameter, ringOuterDiameter, 0, DrawableUtil.TAU, 0x00000055);
        DrawableUtil.drawArc(context.getMatrices(), ringOuterDiameter, ringOuterDiameter + ringMarkerWidth, theta - segmentAngle / 2, segmentAngle, 0xFFFFFF45);

        Text selectedItemLabel = currentAction.getLabel();
        context.drawText(client.textRenderer, selectedItemLabel, -client.textRenderer.getWidth(selectedItemLabel) / 2, -client.textRenderer.fontHeight / 2, Colors.WHITE, true);

        for (int i = 0; i < segmentCount; i++) {
            double segmentMinAngle = (i * segmentAngle + segmentsStartAngle) % DrawableUtil.TAU;

            if (i == selection) {
                DrawableUtil.drawArc(context.getMatrices(), ringInnerDiameter, ringOuterDiameter, segmentMinAngle, segmentAngle, 0xFFFFFF25);
            }

            LevitatingItemEntity.Action action = options.get(i);
            float iconPositionAngle = (float)(-segmentMinAngle - segmentAngle * 0.5F);
            int x = (int)(MathHelper.sin(iconPositionAngle) * segmenticonRadius);
            int y = (int)(MathHelper.cos(iconPositionAngle) * segmenticonRadius);

            context.drawTexture(UHud.HUD_TEXTURE, x - iconSize / 2, y - iconSize / 2, 144 + action.getU() * iconSize, action.getV() * iconSize, iconSize, iconSize, 256, 256);
        }
    }

    private int getSelection(double theta, double rad, double firstSegmentStart, double segmentSize) {
        if (rad <= 0) {
            return -1;
        }

        theta -= firstSegmentStart;
        if (theta < 0) {
            theta += DrawableUtil.TAU;
        }
        theta %= DrawableUtil.TAU;
        theta /= segmentSize;


        return (int)theta;
    }
}

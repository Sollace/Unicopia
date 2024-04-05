package com.minelittlepony.unicopia.client.render;

import java.util.Optional;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.command.CommandArgumentEnum;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.GlassesItem;
import com.minelittlepony.unicopia.util.AnimationUtil;
import com.mojang.serialization.Codec;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.*;

public class PlayerPoser {
    public static final PlayerPoser INSTANCE = new PlayerPoser();

    private static final float HEAD_NOD_DURATION = 15F;
    private static final float HEAD_NOD_GAP = HEAD_NOD_DURATION / 3F;

    public void applyPosing(MatrixStack matrices, PlayerEntity player, BipedEntityModel<?> model, Context context) {
        Pony pony = Pony.of(player);
        float tickDelta = MinecraftClient.getInstance().getTickDelta();
        float progress = pony.getAnimationProgress(MinecraftClient.getInstance().getTickDelta());
        AnimationInstance animation = pony.getAnimation();
        Race ponyRace = MineLPDelegate.getInstance().getPlayerPonyRace(player);
        Arm mainArm = player.getMainArm();

        boolean liftLeftArm = mainArm == Arm.LEFT || !ponyRace.isEquine();
        boolean liftRightArm = mainArm == Arm.RIGHT || !ponyRace.isEquine();

        ItemStack glasses = GlassesItem.getForEntity(player);
        ModelPart head = model.getHead();

        if (context == Context.THIRD_PERSON && !player.isSneaking()) {
            Hand leftHand = mainArm == Arm.LEFT ? Hand.MAIN_HAND : Hand.OFF_HAND;
            Hand rightHand = mainArm == Arm.LEFT ? Hand.OFF_HAND : Hand.MAIN_HAND;

            float pitchChange = -0.5F;
            float yawChange = 0.8F;

            if (player.getStackInHand(rightHand).isIn(UTags.Items.POLEARMS) && (!ponyRace.isEquine() || model.rightArm.pitch != 0)) {
                model.rightArm.pitch += pitchChange;
                model.rightArm.yaw += yawChange;
                if (player.handSwingTicks > 0 && rightHand == Hand.MAIN_HAND) {
                    model.rightArm.yaw -= 0.5F;
                    model.rightArm.pitch += 1.5F;
                }
            }

            if (player.getStackInHand(leftHand).isIn(UTags.Items.POLEARMS) && (!ponyRace.isEquine() || model.leftArm.pitch != 0)) {
                model.leftArm.pitch += pitchChange;
                model.leftArm.yaw -= yawChange;
                if (player.handSwingTicks > 0 && leftHand == Hand.MAIN_HAND) {
                    model.leftArm.yaw -= 0.5F;
                    model.leftArm.pitch += 1.5F;
                }
            }
        }

        if (glasses.hasCustomName() && "Cool Shades".equals(glasses.getName().getString())) {
            final float bop = AnimationUtil.beat(player.age, HEAD_NOD_DURATION, HEAD_NOD_GAP) * 3F;
            head.pitch += bop / 10F;

            float beat30 = bop / 30F;

            if (ponyRace.isEquine()) {
                float beat50 = bop / 50F;
                float beat20 = bop / 20F;

                model.leftArm.roll -= beat50;
                model.rightArm.roll += beat50;

                model.leftLeg.roll -= beat30;
                model.leftLeg.pitch -= beat20;
                model.rightLeg.roll += beat30;
                model.rightLeg.pitch += beat20;
            } else {
                model.leftArm.roll -= beat30;
                model.rightArm.roll += beat30;
            }
        }

        if (animation.canPlay(ponyRace.isEquine())) {
            switch (animation.animation()) {
                case NECK_SNAP: {
                    head.yaw += 3F;
                    head.pitch *= -1;
                    break;
                }
                case WOLOLO: {
                    float roll = MathHelper.sin(player.age / 10F);
                    float yaw = MathHelper.cos(player.age / 10F);

                    if (liftLeftArm) {
                        rotateArm(model.leftArm, 1, yaw, -roll);
                    }

                    if (liftRightArm) {
                        rotateArm(model.leftArm, 1, yaw, roll);
                    }

                    break;
                }
                case ARMS_FORWARD: {
                    float roll = (progress + 1) / 2F;

                    float pitch = 1.5F * roll;
                    float yaw = -0.5F * roll;

                    if (liftLeftArm) {
                        rotateArm(model.leftArm, pitch, -yaw, 0);
                    }

                    if (liftRightArm) {
                        rotateArm(model.rightArm, pitch, yaw, 0);
                    }

                    break;
                }
                case HANG: {
                    float saw = MathHelper.sin(player.limbAnimator.getPos());

                    float pitch = 0.8F * saw;

                    float basePitch = 3.25F;

                    if (context == Context.THIRD_PERSON) {

                        if (ponyRace.isEquine()) {
                            rearUp(matrices, model, 1);
                            model.head.pitch += 0.8F;
                            basePitch = 2.25F;
                        }

                        rotateArm(model.leftArm, basePitch - pitch, -0.3F, 0);
                        rotateArm(model.rightArm, basePitch + pitch, 0.3F, 0);

                        rotateArm(model.leftLeg, 0, 0.1F, 0);
                        rotateArm(model.rightLeg, 0, -0.1F, 0);
                    } else {
                        pitch *= 0.5F;

                        float x = ponyRace.isEquine() ? 9 : 10;
                        float y = ponyRace.isEquine() ? -3 : -1;
                        float z = ponyRace.isEquine() ? -8 : -6;

                        float cameraPitch = player.getPitch(tickDelta) * MathHelper.RADIANS_PER_DEGREE;

                        rotateArm(model.leftArm, 0, 0, -0.4F + pitch);
                        rotateArm(model.rightArm, 0, 0, 0.4F + pitch);

                        model.leftArm.pivotX += x;
                        model.leftArm.pivotY += y;
                        model.leftArm.pivotZ += z;
                        model.leftArm.roll -= cameraPitch;


                        model.rightArm.pivotX -= x;
                        model.rightArm.pivotY += y;
                        model.rightArm.pivotZ += z;
                        model.rightArm.roll += cameraPitch;
                    }
                    break;
                }
                case ARMS_UP: {
                    float saw = AnimationUtil.seesaw(progress);

                    float pitch = 3F * saw;
                    float yaw = 0.5F * saw;

                    if (ponyRace.isEquine()) {
                        rearUp(matrices, model, saw);
                        model.head.pitch += saw * 0.5F;
                        pitch = saw * 2F;
                    }

                    if (liftLeftArm) {
                        rotateArm(model.leftArm, pitch, -yaw, yaw);
                    } else if (ponyRace.isEquine()) {
                        model.leftArm.pitch -= saw / 4F;
                        model.leftArm.roll -= saw / 4F;
                    }

                    if (liftRightArm) {
                        rotateArm(model.rightArm, pitch, yaw, -yaw);
                    } else if (ponyRace.isEquine()) {
                        model.rightArm.pitch += saw / 4F;
                        model.rightArm.roll += saw / 4F;
                    }

                    break;
                }
                case CLIMB: {
                    float saw = AnimationUtil.seesaw(progress);

                    float pitch = MathHelper.clamp(3F * saw, 1, 2);
                    float yaw = 0.5F * saw;

                    if (context == Context.THIRD_PERSON) {
                        if (ponyRace.isEquine()) {
                            rearUp(matrices, model, 1);
                            pitch = saw * 2F;
                            model.head.pitch += saw * 0.5F;

                            rotateArm(model.leftArm, pitch, -yaw, yaw / 2F);
                            rotateArm(model.rightLeg, pitch / 2F, yaw, 0);

                            saw = AnimationUtil.seesaw(progress + 0.5F);
                            yaw = 0.5F * -saw;

                            rotateArm(model.rightArm, pitch, yaw, -yaw / 2F);
                            rotateArm(model.leftLeg, pitch / 2F, -yaw, 0);

                        } else {
                            rotateArm(model.leftArm, pitch, -yaw, yaw / 2F);
                            rotateArm(model.rightLeg, pitch / 2F, yaw, 0);

                            saw = AnimationUtil.seesaw((progress + 0.5F) % 1);

                            pitch = MathHelper.clamp(3F * saw, 1, 2) * (ponyRace.isEquine() ? 2 : 1);
                            yaw = 0.5F * saw;

                            rotateArm(model.rightArm, pitch, yaw, -yaw / 2F);
                            rotateArm(model.leftLeg, pitch / 2F, -yaw, 0);
                        }
                    } else {
                        //saw = MathHelper.sin(progress * 2);
                        float x = ponyRace.isEquine() ? 9 : 0;
                        float y = ponyRace.isEquine() ? -3 : 0;
                        float z = ponyRace.isEquine() ? -8 : -2;

                        float cameraPitch = player.getPitch(tickDelta) * MathHelper.RADIANS_PER_DEGREE;
                        pitch = MathHelper.sin((progress * 2) * MathHelper.PI) * 0.6F;

                        rotateArm(model.leftArm, 0, 0, pitch);
                        rotateArm(model.rightArm, 0, 0, pitch);

                        model.leftArm.pivotX += x;
                        model.leftArm.pivotY += y;
                        model.leftArm.pivotZ += z;
                        model.leftArm.roll -= cameraPitch;

                        model.rightArm.pivotX -= x;
                        model.rightArm.pivotY += y;
                        model.rightArm.pivotZ += z;
                        model.rightArm.roll += cameraPitch;
                    }

                    break;
                }
                case WAVE_ONE:
                case WAVE_TWO: {
                    progress = AnimationUtil.seesaw(progress);

                    if (animation.isOf(Animation.WAVE_TWO) && ponyRace.isEquine()) {
                        rearUp(matrices, model, progress);
                        model.head.pitch += progress * 0.5F;
                    }

                    float wave = 2.5F + progress * MathHelper.sin(player.age / 3F);

                    if (animation.isOf(Animation.WAVE_TWO) || mainArm == Arm.LEFT) {
                        model.leftArm.roll = -wave;
                    }
                    if (animation.isOf(Animation.WAVE_TWO) || mainArm == Arm.RIGHT) {
                        model.rightArm.roll = wave;
                    }
                    break;
                }
                case KICK: {
                    if (ponyRace.isEquine()) {
                        float roll = (progress + 1) / 2F;

                        model.rightLeg.pitch = roll * 1.5F;
                        model.rightLeg.yaw = -roll / 7F;

                        model.leftLeg.pitch = roll * 1.5F;
                        model.leftLeg.yaw = roll / 7F;

                        model.leftArm.pitch = 0;
                        model.leftArm.roll = MathHelper.lerp(progress, model.leftArm.roll, 0);
                        model.rightArm.pitch = 0;
                        model.rightArm.roll = MathHelper.lerp(progress, model.rightArm.roll, 0);
                        break;
                    }

                    float roll = (progress + 1) / 2F;

                    model.rightArm.pitch += roll / 5F;
                    model.leftArm.roll -= roll / 5F;
                    model.rightArm.roll += roll / 5F;

                    if (player.getMainArm() == Arm.LEFT) {
                        model.rightLeg.pitch = -roll * 1.5F;
                        model.rightLeg.roll = roll / 10F;
                    } else {
                        model.leftLeg.pitch = -roll * 1.5F;
                        model.leftLeg.roll = -roll / 10F;
                    }
                    break;
                }
                case STOMP: {
                    progress = AnimationUtil.seesaw(progress);

                    if (!ponyRace.isEquine()) {
                        if (mainArm == Arm.LEFT) {
                            model.rightLeg.roll = -progress / 9F;
                            model.rightLeg.pivotY -= progress * 5;
                        } else {
                            model.leftLeg.roll = -progress / 9F;
                            model.leftLeg.pivotY -= progress * 5;
                        }
                        break;
                    }

                    rearUp(matrices, model, progress);
                    model.head.pitch += progress * 0.5F;
                    model.leftArm.pitch += progress / 2F;
                    model.rightArm.pitch += progress / 2F;
                    break;
                }
                case WIGGLE_NOSE: {
                    if (!ponyRace.isEquine()) {
                        break;
                    }

                    progress = AnimationUtil.seesaw(progress) * MathHelper.sin(player.age) / 7F;

                    model.getHead().getChild("mare").pivotY = progress;
                    model.getHead().getChild("stallion").pivotY = progress;
                    break;
                }
                default:
            }
        }

        if (pony.getEntityInArms().isPresent()) {
            if (ponyRace.isEquine() && pony.getPhysics().isFlying()) {
                model.leftLeg.pitch = 1;
                model.rightLeg.pitch = 1;
                model.leftLeg.yaw = 0.3F;
                model.rightLeg.yaw = -0.3F;
            } else {
                model.leftArm.pitch = -1;
                model.rightArm.pitch = -1;
            }
            model.leftArm.yaw = -0.3F;
            model.rightArm.yaw = 0.3F;
            model.leftArm.roll = 0;
            model.rightArm.roll = 0;
        }

        if (model instanceof PlayerEntityModel<?> m) {
            m.leftSleeve.copyTransform(m.leftArm);
            m.rightSleeve.copyTransform(m.rightArm);
            m.leftPants.copyTransform(m.leftLeg);
            m.rightPants.copyTransform(m.rightLeg);
        }
        model.hat.copyTransform(model.head);
    }

    private void rotateArm(ModelPart arm, float pitch, float yaw, float roll) {
        arm.pitch -= pitch;
        arm.roll = roll;
        arm.yaw = yaw;
    }

    private void rearUp(MatrixStack matrices, BipedEntityModel<?> model, float progress) {
        matrices.translate(0, 0, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-45 * progress));
        matrices.translate(0, 0, -0.5);

        float roll = progress / 2F;

        model.rightLeg.pitch = roll * 1.5F;
        model.rightLeg.yaw = -roll / 7F;

        model.leftLeg.pitch = roll * 1.5F;
        model.leftLeg.yaw = roll / 7F;
    }

    public record AnimationInstance(Animation animation, Animation.Recipient recipient) {
        public static final AnimationInstance NONE = new AnimationInstance(Animation.NONE, Animation.Recipient.ANYONE);

        public boolean isOf(Animation animation) {
            return animation() == animation;
        }

        public boolean canPlay(boolean isPony) {
            return !isOf(Animation.NONE) && (recipient == Animation.Recipient.ANYONE || isPony == (recipient == Animation.Recipient.PONY));
        }

        public boolean renderBothArms() {
            return isOf(Animation.HANG) || isOf(Animation.CLIMB);
        }
    }

    public enum Animation implements CommandArgumentEnum<Animation> {
        NONE(0),
        WOLOLO(USounds.ENTITY_PLAYER_WOLOLO, 40),
        ARMS_FORWARD(5),
        ARMS_UP(5),
        WAVE_ONE(USounds.ENTITY_PLAYER_WHISTLE, 20),
        WAVE_TWO(USounds.ENTITY_PLAYER_WHISTLE, 20),
        KICK(USounds.ENTITY_PLAYER_KICK, 5),
        CLIMB(20),
        HANG(20),
        STOMP(5),
        WIGGLE_NOSE(6),
        SPREAD_WINGS(6),
        NECK_SNAP(50);

        private final int duration;
        private final Optional<SoundEvent> sound;

        Animation(int duration) {
            this.duration = duration;
            this.sound = Optional.empty();
        }

        Animation(SoundEvent sound, int duration) {
            this.duration = duration;
            this.sound = Optional.of(sound);
        }

        public int getDuration() {
            return duration;
        }

        public Optional<SoundEvent> getSound() {
            return sound;
        }

        public static EnumArgumentType<Animation> argument() {
            return new ArgumentType();
        }

        public static final class ArgumentType extends EnumArgumentType<Animation> {
            static final Codec<Animation> CODEC = StringIdentifiable.createCodec(Animation::values);

            protected ArgumentType() {
                super(CODEC, Animation::values);
            }
        }

        public enum Recipient implements CommandArgumentEnum<Recipient> {
            HUMAN,
            PONY,
            ANYONE;

            public static EnumArgumentType<Recipient> argument() {
                return new ArgumentType();
            }

            public static final class ArgumentType extends EnumArgumentType<Recipient> {
                static final Codec<Recipient> CODEC = StringIdentifiable.createCodec(Recipient::values);

                protected ArgumentType() {
                    super(CODEC, Recipient::values);
                }
            }
        }
    }

    public enum Context {
        FIRST_PERSON_LEFT,
        FIRST_PERSON_RIGHT,
        THIRD_PERSON
    }
}

package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.mixin.MixinShulkerEntity;

import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ShulkerBehaviour extends EntityBehaviour<ShulkerEntity> {
    @Override
    public void update(Living<?> source, ShulkerEntity shulker, Disguise spell) {
        shulker.setYaw(0);
        shulker.prevBodyYaw = 0;
        shulker.bodyYaw = 0;

        super.update(source, shulker, spell);

        Direction attachmentFace = shulker.getAttachedFace();
        BlockPos pos = shulker.getBlockPos().offset(attachmentFace);

        boolean noGravity = !shulker.isOnGround() && !shulker.world.isAir(pos)
                && (attachmentFace == Direction.UP || attachmentFace.getAxis() != Axis.Y);

        source.asEntity().setNoGravity(noGravity);
        if (noGravity && source.asEntity().isSneaking()) {
            Vec3d vel = source.asEntity().getVelocity();
            if (vel.y > 0) {
                source.asEntity().setVelocity(vel.multiply(1, 0.8, 1));
            }
        }
    }

    @Override
    protected void update(Pony player, ShulkerEntity shulker, Disguise spell) {
        float peekAmount = 30;

        double speed = !player.asEntity().isSneaking() ? 0.29 : 0;
        speed += Math.sqrt(player.asEntity().getVelocity().horizontalLengthSquared()) * 2;

        peekAmount = (float)MathHelper.clamp(speed, 0, 1);
        peekAmount = player.getInterpolator().interpolate("peek", peekAmount, 5);

        MixinShulkerEntity mx = (MixinShulkerEntity)shulker;

        mx.setPrevOpenProgress(mx.getOpenProgress());
        mx.setOpenProgress(peekAmount);

        if (player.sneakingChanged()) {
            mx.callSetPeekAmount((int)(peekAmount / 0.01F));
        } else if (peekAmount > 0.2 && mx.callGetPeekAmount() == 0) {
            if (shulker.isAlive() && shulker.world.random.nextInt(1000) < shulker.ambientSoundChance++) {
                shulker.ambientSoundChance = -shulker.getMinAmbientSoundDelay();
                shulker.playSound(SoundEvents.ENTITY_SHULKER_AMBIENT, 1, 1);
             }
        }
    }
}

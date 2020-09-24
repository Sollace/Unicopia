package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.mixin.MixinShulkerEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class ShulkerBehaviour extends EntityBehaviour<ShulkerEntity> {
    @Override
    public void update(Caster<?> source, ShulkerEntity shulker) {
        shulker.yaw = 0;
        shulker.prevBodyYaw = 0;
        shulker.bodyYaw = 0;

        shulker.setAttachedBlock(null);

        if (source instanceof Pony) {
            Pony player = (Pony)source;

            float peekAmount = 30;

            double speed = !source.getEntity().isSneaking() ? 0.29 : 0;
            speed += Math.sqrt(Entity.squaredHorizontalLength(source.getEntity().getVelocity())) * 2;

            peekAmount = (float)MathHelper.clamp(speed, 0, 1);
            peekAmount = ((Pony)source).getInterpolator().interpolate("peek", peekAmount, 5);

            MixinShulkerEntity mx = (MixinShulkerEntity)shulker;

            mx.setPrevOpenProgress(mx.getOpenProgress());
            mx.setOpenProgress(peekAmount);

            if (player.sneakingChanged()) {
                shulker.setPeekAmount((int)(peekAmount / 0.01F));
            } else if (peekAmount > 0.2 && shulker.getPeekAmount() == 0) {
                if (shulker.isAlive() && shulker.world.random.nextInt(1000) < shulker.ambientSoundChance++) {
                    shulker.ambientSoundChance = -shulker.getMinAmbientSoundDelay();
                    shulker.playSound(SoundEvents.ENTITY_SHULKER_AMBIENT, 1, 1);
                 }
            }
        }
    }
}

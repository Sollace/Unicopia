package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.entity.duck.Hoverable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.nbt.NbtCompound;

@Mixin(FallingBlockEntity.class)
abstract class MixinFallingBlockEntity extends Entity implements Hoverable {
    private MixinFallingBlockEntity() { super(null, null); }

    private int maxTicksHovering;
    private int ticksHovering;

    @Override
    public void setTicksHovering(int ticks) {
        maxTicksHovering = ticks;
        ticksHovering = 0;
    }

    @Override
    public int getTicksHovering() {
        return ticksHovering;
    }

    @Override
    public int getMaxTicksHovering() {
        return maxTicksHovering;
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        if (!getWorld().isClient && maxTicksHovering > 0 && ticksHovering++ > maxTicksHovering) {
            Entity self = this;
            self.setNoGravity(false);
        }
    }

    @Unique
    private boolean isHovering() {
        return maxTicksHovering > 0 && ticksHovering < maxTicksHovering;
    }

    @Inject(method = "writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("HEAD"))
    private void onWriteNbt(NbtCompound tag, CallbackInfo info) {
        tag.putInt("unicopia_maxTicksHovering", maxTicksHovering);
        tag.putInt("unicopia_ticksHovering", ticksHovering);
        if (isHovering()) {
            // in case the world is loaded without us, reset it to have gravity again
            tag.putBoolean("NoGravity", false);
        }
    }

    @Inject(method = "readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("HEAD"))
    private void onReadNbt(NbtCompound tag, CallbackInfo info) {
        maxTicksHovering = tag.getInt("unicopia_maxTicksHovering", 0);
        ticksHovering = tag.getInt("unicopia_ticksHovering", 0);
        if (isHovering()) {
            setNoGravity(true);
        }
    }
}

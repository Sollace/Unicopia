package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.player.PlayerAbilities;

@Mixin(PlayerAbilities.class)
public interface IWalker {
    @Accessor("walkSpeed")
    void setWalkSpeed(float speed);
}

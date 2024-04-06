package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.client.sound.MagicAuraSoundInstance;
import com.minelittlepony.unicopia.entity.Living;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class GemFindingEnchantment extends SimpleEnchantment {

    protected GemFindingEnchantment(Options options) {
        super(options);
    }

    @Override
    public void onUserTick(Living<?> user, int level) {
        int radius = 2 + (level * 2);

        BlockPos origin = user.getOrigin();

        double volume = BlockPos.findClosest(origin, radius, radius, pos -> user.asWorld().getBlockState(pos).isIn(UTags.Blocks.INTERESTING))
            .map(p -> user.getOriginVector().squaredDistanceTo(p.getX(), p.getY(), p.getZ()))
            .map(find -> (1 - (Math.sqrt(find) / radius)))
            .orElse(-1D);

        volume = Math.max(volume, 0.04F);

        user.getEnchants().computeIfAbsent(this, Data::new).level = (float)volume * (1.3F + level);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void onEquipped(Living<?> user) {
        if (user.isClient()) {
            MinecraftClient.getInstance().getSoundManager().play(new MagicAuraSoundInstance(user.asEntity().getSoundCategory(), user, user.asWorld().getRandom()));
        }
    }

    @Override
    public void onUnequipped(Living<?> user) {
        user.getEnchants().remove(this).level = 0;
    }
}

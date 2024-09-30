package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.client.sound.MagicAuraSoundInstance;
import com.minelittlepony.unicopia.entity.Enchantments;
import com.minelittlepony.unicopia.entity.Living;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class GemFindingEnchantment {

    public void onUserTick(Living<?> user, int level) {
        int radius = 2 + (level * 2);

        BlockPos origin = user.getOrigin();

        double volume = BlockPos.findClosest(origin, radius, radius, pos -> user.asWorld().getBlockState(pos).isIn(UTags.Blocks.INTERESTING))
            .map(p -> user.getOriginVector().squaredDistanceTo(p.getX(), p.getY(), p.getZ()))
            .map(find -> (1 - (Math.sqrt(find) / radius)))
            .orElse(-1D);

        volume = Math.max(volume, 0.04F);

        user.getEnchants().computeIfAbsent(UEnchantments.GEM_FINDER, Enchantments.Data::new).level = (float)volume * (1.3F + level);
    }

    public void onEquipped(Living<?> user) {
        if (user.isClient()) {
            MinecraftClient.getInstance().getSoundManager().play(new MagicAuraSoundInstance(user.asEntity().getSoundCategory(), user, user.asWorld().getRandom()));
        }
    }

    public void onUnequipped(Living<?> user) {
        user.getEnchants().remove(UEnchantments.GEM_FINDER).level = 0;
    }
}

package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.client.sound.MagicAuraSoundInstance;
import com.minelittlepony.unicopia.entity.Living;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class GemFindingEnchantment extends SimpleEnchantment {

    protected GemFindingEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.DIGGER, false, 3, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND);
    }

    @Override
    public void onUserTick(Living<?> user, int level) {
        int radius = 2 + (level * 2);

        BlockPos origin = user.getOrigin();

        float volume = BlockPos.findClosest(origin, radius, radius, pos -> user.getWorld().getBlockState(pos).isIn(UTags.INTERESTING))
            .map(p -> user.getOriginVector().squaredDistanceTo(p.getX(), p.getY(), p.getZ()))
            .map(find -> (1 - (MathHelper.sqrt(find) / radius)))
            .orElse(-1F);

        volume = Math.max(volume, 0.04F);

        user.getEnchants().computeIfAbsent(this, Data::new).level = volume * (1.3F + level * 0.3F);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void onEquipped(Living<?> user) {
        if (user.isClient()) {
            MinecraftClient.getInstance().getSoundManager().play(new MagicAuraSoundInstance(user.getEntity().getSoundCategory(), user));
        }
    }

    @Override
    public void onUnequipped(Living<?> user) {
        user.getEnchants().remove(this).level = 0;
    }
}

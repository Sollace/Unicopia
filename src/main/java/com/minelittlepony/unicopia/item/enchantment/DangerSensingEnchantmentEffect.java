package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.mojang.serialization.MapCodec;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class DangerSensingEnchantmentEffect implements EnchantmentEntityEffect {
    public static final DangerSensingEnchantmentEffect INSTANCE = new DangerSensingEnchantmentEffect();
    public static final MapCodec<DangerSensingEnchantmentEffect> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<DangerSensingEnchantmentEffect> getCodec() {
        return CODEC;
    }

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity entity, Vec3d pos) {
        Pony.of(entity).ifPresent(pony -> {
            if (pony.asEntity().age % 10 == 0) {
                int range = (level + 1) * 3;
                if (pony.asWorld().getEntitiesByClass(HostileEntity.class, pony.asEntity().getBoundingBox().expand(range, 0, range), enemy -> {
                    return enemy != null
                            && enemy.canTarget(pony.asEntity())
                            && enemy.canSee(pony.asEntity())
                            && enemy.getTarget() == pony.asEntity();
                }).isEmpty()) {
                    return;
                }

                Bar bar = pony.getMagicalReserves().getEnergy();
                float targetPercent = (level / (float)pony.entryFor(UEnchantments.STRESSED).value().definition().maxLevel()) * 0.05125F;
                float increase = 1F + (level * level)/100F;
                if (bar.getPercentFill() < targetPercent) {
                    bar.add(increase);
                }
            }
        });

    }
}

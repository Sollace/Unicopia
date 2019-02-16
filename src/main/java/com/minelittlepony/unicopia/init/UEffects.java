package com.minelittlepony.unicopia.init;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.potion.UPotion;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.registries.IForgeRegistry;

public class UEffects {

    public static final DamageSource food_poisoning = new DamageSource("food_poisoning").setDamageBypassesArmor();

    public static final Potion FOOD_POISONING = new UPotion(Unicopia.MODID, "food_poisoning", true, 3484199)
            .setIconIndex(3, 1)
            .setSilent()
            .setEffectiveness(0.25)
            .setApplicator((p, e, i) -> {

                PotionEffect nausea = e.getActivePotionEffect(MobEffects.NAUSEA);
                if (nausea == null) {
                    PotionEffect foodEffect = e.getActivePotionEffect(p);
                    nausea = new PotionEffect(MobEffects.NAUSEA, foodEffect.getDuration(), foodEffect.getAmplifier(), foodEffect.getIsAmbient(), foodEffect.doesShowParticles());

                    e.addPotionEffect(nausea);
                }

                e.attackEntityFrom(food_poisoning, i);
            });

    static void init(IForgeRegistry<Potion> registry) {
        registry.registerAll(
                FOOD_POISONING
        );
    }
}

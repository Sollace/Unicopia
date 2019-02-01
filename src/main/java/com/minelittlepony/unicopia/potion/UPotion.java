package com.minelittlepony.unicopia.potion;

import javax.annotation.Nonnull;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class UPotion extends Potion {

    private boolean isSilent;
    private int tickDelay = 40;

    @Nonnull
    private IEffectApplicator applicator = IEffectApplicator.NONE;

    public UPotion(String domain, String name, boolean isNegative, int tint) {
        super(isNegative, tint);

        setRegistryName(domain, name);
        setPotionName("effect." + name);
    }

    public UPotion setSilent() {
        isSilent = true;

        return this;
    }

    public UPotion setApplicator(@Nonnull IEffectApplicator applicator) {
        this.applicator = applicator;

        return this;
    }

    public UPotion setTickDelay(int delay) {
        tickDelay = delay;

        return this;
    }

    @Override
    public UPotion setIconIndex(int u, int v) {
        super.setIconIndex(u, v);

        return this;
    }

    @Override
    public UPotion setEffectiveness(double effectiveness) {
        super.setEffectiveness(effectiveness);

        return this;
    }

    @Override
    public boolean shouldRender(PotionEffect effect) {
        return !isSilent;
    }

    @Override
    public boolean shouldRenderInvText(PotionEffect effect) {
        return !isSilent;
    }

    @Override
    public boolean shouldRenderHUD(PotionEffect effect) {
        return !isSilent;
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        applicator.performEffect(this, entity, amplifier);
    }

    @Override
    public boolean isInstant() {
        return tickDelay > 0;
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        if (!isInstant()) {
            int i = tickDelay >> amplifier;

            if (i > 0) {
                return duration % i == 0;
            }
        }

        return duration > 0;
    }

    @FunctionalInterface
    public interface IEffectApplicator {
        IEffectApplicator NONE = (p, e, i) -> {};

        void performEffect(Potion effect, EntityLivingBase target, int amplifier);
    }
}

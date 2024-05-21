package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.entity.ItemTracker;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.random.Random;

public class CorruptionHandler implements Tickable {

    private final Pony pony;

    public CorruptionHandler(Pony pony) {
        this.pony = pony;
    }

    public boolean hasCorruptingMagic() {
        return pony.getSpellSlot().get(SpellPredicate.IS_CORRUPTING).isPresent() || UItems.ALICORN_AMULET.isApplicable(pony.asEntity());
    }

    @Override
    public void tick() {
        if (pony.isClient() || pony.asEntity().age % 5 != 0) {
            return;
        }

        PlayerEntity entity = pony.asEntity();
        Random random = pony.asEntity().getRandom();

        if (!UItems.ALICORN_AMULET.isApplicable(entity)) {
            if (entity.age % (10 * ItemTracker.SECONDS) == 0) {
                if (random.nextInt(100) == 0) {
                    pony.getCorruption().add(-1);
                    pony.setDirty();
                }

                if (entity.getHealth() >= entity.getMaxHealth() - 1 && !entity.getHungerManager().isNotFull()) {
                    pony.getCorruption().add(-random.nextInt(4));
                    pony.setDirty();
                }
            }
        }

        if (pony.asEntity().age % 100 == 0 && hasCorruptingMagic()) {
            pony.getCorruption().add(random.nextInt(4));
        }

        float corruptionPercentage = pony.getCorruption().getScaled(1);

        if (corruptionPercentage > 0.5F && random.nextFloat() < corruptionPercentage - 0.25F) {
            pony.findAllEntitiesInRange(10, e -> e instanceof LivingEntity && !((LivingEntity)e).hasStatusEffect(UEffects.CORRUPT_INFLUENCE)).forEach(e -> {
                ((LivingEntity)e).addStatusEffect(new StatusEffectInstance(UEffects.CORRUPT_INFLUENCE, 100, 1));
                recover(10);
            });
        }

        if (corruptionPercentage > 0.25F && random.nextInt(200) == 0) {
            if (!pony.asEntity().hasStatusEffect(UEffects.BUTTER_FINGERS)) {
                pony.asEntity().addStatusEffect(new StatusEffectInstance(UEffects.BUTTER_FINGERS, 2100, 1));
                recover(25);
            }
        }

        if (random.nextFloat() < corruptionPercentage) {
            pony.spawnParticles(ParticleTypes.ASH, 10);
        }
    }

    private void recover(float percentage) {
        pony.getCorruption().set((int)(pony.getCorruption().get() * (1 - percentage)));
        InteractionManager.getInstance().playLoopingSound(pony.asEntity(), InteractionManager.SOUND_HEART_BEAT, 0);
        MagicReserves reserves = pony.getMagicalReserves();
        reserves.getExertion().addPercent(10);
        reserves.getEnergy().add(10);
        pony.setDirty();
    }
}

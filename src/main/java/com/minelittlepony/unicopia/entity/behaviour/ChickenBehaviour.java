package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class ChickenBehaviour extends EntityBehaviour<ChickenEntity> {

    @Override
    protected boolean skipSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.OFFHAND;
    }

    @Override
    public void update(Caster<?> source, ChickenEntity entity, DisguiseSpell spell) {
        entity.eggLayTime = Integer.MAX_VALUE;

        if (source instanceof Pony) {
            Pony player = (Pony)source;


            if (player.sneakingChanged()) {
                ItemStack egg = entity.getEquippedStack(EquipmentSlot.OFFHAND);

                if (player.getOwner().isSneaking()) {
                    if (egg.isEmpty()) {
                        egg = new ItemStack(Items.EGG);

                        int slot = player.getOwner().inventory.method_7371(egg);
                        if (slot > -1) {
                            player.getOwner().inventory.removeStack(slot, 1);
                            entity.playSound(SoundEvents.ENTITY_CHICKEN_EGG,
                                    1,
                                    (entity.world.random.nextFloat() - entity.world.random.nextFloat()) * 0.2F + 4
                            );
                            entity.equipStack(EquipmentSlot.OFFHAND, egg);
                        }
                    }
                } else if (egg.getItem() == Items.EGG) {
                    entity.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    entity.eggLayTime = 0;
                }
            }
        }

        Entity src = source.getEntity();

        if (src.isOnGround() || src instanceof PlayerEntity && ((PlayerEntity)src).abilities.flying) {
            return;
        }

        Vec3d vel = src.getVelocity();

        if (vel.y < 0) {
            src.setVelocity(vel.multiply(1, 0.6, 1));
        }

    }
}

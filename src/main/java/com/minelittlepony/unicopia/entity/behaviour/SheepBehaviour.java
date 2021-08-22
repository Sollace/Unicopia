package com.minelittlepony.unicopia.entity.behaviour;

import java.util.Random;

import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.mixin.MixinSheepEntity;
import com.minelittlepony.unicopia.util.WorldEvent;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class SheepBehaviour extends EntityBehaviour<SheepEntity> {
    @Override
    public void update(Pony player, SheepEntity entity, DisguiseSpell spell) {

        if (player.sneakingChanged()) {

            BlockPos pos = entity.getBlockPos().down();
            BlockState state = entity.world.getBlockState(pos);
            boolean grass = state.isOf(Blocks.GRASS_BLOCK);

            if (player.getMaster().isSneaking()) {
                if (grass && entity.world.isClient && entity.isSheared()) {
                    entity.handleStatus((byte)10);
                }
            } else {
                if (entity.isSheared() && grass) {
                    WorldEvent.play(WorldEvent.DESTROY_BLOCK, entity.world, pos, state);
                    entity.world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 2);

                    entity.onEatingGrass();
                } else if (!entity.isSheared()) {
                    ItemStack dropType = new ItemStack(MixinSheepEntity.getDrops().get(entity.getColor()).asItem());

                    player.getMaster().playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1, 1);
                    entity.setSheared(true);

                    Random rng = entity.world.random;
                    PlayerInventory inv = player.getMaster().getInventory();

                    int dropAmount = rng.nextInt(3);
                    int slot;

                    do {
                        slot = inv.indexOf(dropType);

                        if (slot < 0) {
                            break;
                        }
                        inv.removeStack(slot, 1);
                        ItemEntity itemEntity = entity.dropItem(dropType.getItem(), 1);
                        if (itemEntity != null) {
                           itemEntity.setVelocity(itemEntity.getVelocity().add(
                                   (rng.nextFloat() - rng.nextFloat()) * 0.1F,
                                   rng.nextFloat() * 0.05F,
                                   (rng.nextFloat() - rng.nextFloat()) * 0.1F
                           ));
                           itemEntity.setPickupDelay(40);
                        }
                    } while (dropAmount-- > 0);
                }
                spell.setDirty();
            }
        }
    }
}

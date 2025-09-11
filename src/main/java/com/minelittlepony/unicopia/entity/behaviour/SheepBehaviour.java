package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldEvents;

public class SheepBehaviour extends EntityBehaviour<SheepEntity> {
    @Override
    public void update(Pony player, SheepEntity entity, Disguise spell) {

        if (player.sneakingChanged()) {

            BlockPos pos = entity.getBlockPos().down();
            BlockState state = entity.getWorld().getBlockState(pos);
            boolean grass = state.isOf(Blocks.GRASS_BLOCK);

            if (player.asEntity().isSneaking()) {
                if (grass && entity.getWorld().isClient && entity.isSheared()) {
                    entity.handleStatus((byte)10);
                }
            } else {
                if (entity.isSheared() && grass) {
                    entity.getWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
                    entity.getWorld().setBlockState(pos, Blocks.DIRT.getDefaultState(), 2);

                    entity.onEatingGrass();
                } else if (!entity.isSheared()) {
                    DyeColor color = entity.getColor();
                    Item wool = Registries.ITEM.get(Identifier.ofVanilla(color.asString() + "_wool"));
                    if (wool == null) {
                        return;
                    }

                    ItemStack dropType = new ItemStack(wool);

                    player.asEntity().playSound(USounds.Vanilla.ENTITY_SHEEP_SHEAR, 1, 1);
                    entity.setSheared(true);

                    Random rng = entity.getWorld().random;
                    PlayerInventory inv = player.asEntity().getInventory();

                    int dropAmount = rng.nextInt(3);
                    int slot;

                    do {
                        slot = inv.getMatchingSlot(dropType.getRegistryEntry());

                        if (slot < 0) {
                            break;
                        }
                        inv.removeStack(slot, 1);
                        ItemEntity itemEntity = entity.dropItem(player.asServerWorld(), dropType.getItem(), 1);
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
                spell.getAppearance().ifPresent(EntityAppearance::markDirty);
            }
        }
    }
}

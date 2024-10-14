package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.TraceHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

/**
 * Hippogriff ability to use their beak as a weapon
 */
public class PeckAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 1;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 10;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public boolean activateOnEarlyRelease() {
        return true;
    }

    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.INSTANCE;
    }

    @Override
    public PacketCodec<? super RegistryByteBuf, Hit> getSerializer() {
        return Hit.CODEC;
    }

    protected LivingEntity findTarget(PlayerEntity player, World w) {
        return TraceHelper.<LivingEntity>findEntity(player, 5, 1, hit -> {
            return EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(hit) && !player.isConnectedThroughVehicle(hit);
        }).orElse(null);
    }

    @Override
    public boolean apply(Pony player, Hit hit) {
        LivingEntity target = findTarget(player.asEntity(), player.asWorld());

        playSounds(player, player.asWorld().getRandom(), 1);

        if (target != null) {
            spookMob(player, target, 1);
        } else {
            BlockPos pos = TraceHelper.findBlock(player.asEntity(), 5, 1).orElse(BlockPos.ORIGIN);
            if (pos != BlockPos.ORIGIN) {
                BlockState state = player.asWorld().getBlockState(pos);
                if (state.isReplaceable()) {
                    player.asWorld().breakBlock(pos, true);
                } else if (state.isIn(BlockTags.DIRT) || player.asWorld().random.nextInt(40000) == 0) {
                    player.asWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
                    pos = pos.up();
                    World world = player.asWorld();

                    if (world instanceof ServerWorld sw) {
                        for (ItemStack stack : Block.getDroppedStacks(state, sw, pos, null)) {
                            if (Block.getBlockFromItem(stack.getItem()) == Blocks.AIR) {
                                Block.dropStack(world, pos, stack);
                            }
                        }
                        state.onStacksDropped(sw, pos, ItemStack.EMPTY, true);

                        if (world.random.nextInt(20) == 0) {
                            world.breakBlock(pos.down(), false);
                            player.asEntity().sendMessage(Text.translatable("ability.unicopia.peck.block.fled"), true);
                        }
                    }
                } else {
                    player.asEntity().sendMessage(Text.translatable("ability.unicopia.peck.block.unfased"), true);
                }
            }
        }

        return true;
    }

    protected void playSounds(Pony player, Random rng, float strength) {
        player.getMagicalReserves().getExertion().addPercent(100);
        player.getMagicalReserves().getEnergy().addPercent(10);
        player.playSound(USounds.Vanilla.ENTITY_CHICKEN_HURT,
                1,
                0.9F + (rng.nextFloat() - 0.5F)
        );
        player.asWorld().emitGameEvent(player.asEntity(), GameEvent.STEP, player.asEntity().getEyePos());
    }

    protected void spookMob(Pony player, LivingEntity living, float strength) {
        boolean isEarthPony = EquinePredicates.PLAYER_EARTH.test(living);
        boolean isBracing = isEarthPony && player.asEntity().isSneaking();

        if (!isBracing && living.getWorld() instanceof ServerWorld sw) {
            living.damage(sw, player.damageOf(UDamageTypes.BAT_SCREECH, player), isEarthPony ? 0.1F : 0.3F);
        }

        Vec3d knockVec = player.getOriginVector().subtract(living.getPos()).multiply(strength);
        living.takeKnockback((isBracing ? 0.2F : isEarthPony ? 0.3F : 0.5F) * strength, knockVec.getX(), knockVec.getZ());
        if (!isEarthPony) {
            living.addVelocity(0, 0.1 * strength, 0);
        }
        Living.updateVelocity(living);
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}

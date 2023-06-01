package com.minelittlepony.unicopia.item.enchantment;

import java.util.function.DoubleSupplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ConsumptionEnchantment extends SimpleEnchantment {
    protected ConsumptionEnchantment(Options options) {
        super(options);
    }

    public static boolean applyConsumption(World w, BlockState state, BlockPos pos, @Nullable BlockEntity blockEntity, Entity entity, ItemStack tool) {

        if (!(w instanceof ServerWorld world)) {
            return false;
        }

        if (tool.isEmpty() && entity instanceof LivingEntity l) {
            tool = l.getMainHandStack();
        }
        if (EnchantmentHelper.getLevel(UEnchantments.CONSUMPTION, tool) <= 0) {
            return false;
        }

        DoubleSupplier vecComponentFactory = () -> world.random.nextTriangular(0, 0.3);

        Block.getDroppedStacks(state, world, pos, blockEntity, entity, tool).forEach(s -> {
            world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.BLOCKS, 0.05F, (float)world.random.nextTriangular(0.6F, 0.2F));
            ExperienceOrbEntity.spawn(world, Vec3d.ofCenter(pos).add(VecHelper.supply(vecComponentFactory)), s.getCount());
            UCriteria.USE_CONSUMPTION.trigger(entity);
        });
        state.onStacksDropped(world, pos, tool, true);

        return true;
    }
}

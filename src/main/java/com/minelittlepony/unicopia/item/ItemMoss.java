package com.minelittlepony.unicopia.item;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.edibles.ItemEdible;
import com.minelittlepony.unicopia.edibles.Toxicity;
import com.minelittlepony.util.collection.ReversableStateMapList;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.BlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

public class ItemMoss extends ItemEdible {

    public static final ReversableStateMapList affected = new ReversableStateMapList();

    static {
        affected.replaceBlock(Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE);
        affected.replaceProperty(Blocks.COBBLESTONE_WALL, BlockWall.VARIANT, BlockWall.EnumType.MOSSY, BlockWall.EnumType.NORMAL);
        affected.replaceProperty(Blocks.STONEBRICK, BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY, BlockStoneBrick.EnumType.DEFAULT);
        affected.replaceProperty(Blocks.MONSTER_EGG, BlockSilverfish.VARIANT, BlockSilverfish.EnumType.MOSSY_STONEBRICK, BlockSilverfish.EnumType.STONEBRICK);
    }

    @Nullable
    protected IBehaviorDispenseItem vanillaDispenserBehaviour;
    private final IBehaviorDispenseItem dispenserBehavior = new BehaviorDefaultDispenseItem() {
        @Override
        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {

            Direction facing = source.getBlockState().getValue(BlockDispenser.FACING);
            BlockPos pos = source.getBlockPos().offset(facing);
            World w = source.getWorld();

            if (tryConvert(w, w.getBlockState(pos), pos, null)) {
                stack.attemptDamageItem(1, w.rand, null);

                return stack;
            }

            PlayerEntity player = null;

            for (LivingEntity e : w.getEntitiesWithinAABB(LivingEntity.class, Block.FULL_BLOCK_AABB.offset(pos), e ->
                    e instanceof IShearable && ((IShearable)e).isShearable(stack, w, pos)
            )) {
                if (player == null) {
                    player = UClient.instance().createPlayer(e, new GameProfile(null, "Notch"));
                }

                if (stack.interactWithEntity(player, e, EnumHand.MAIN_HAND)) {
                    return stack;
                }
            }

            if (vanillaDispenserBehaviour != null) {
                return vanillaDispenserBehaviour.dispense(source, stack);
            }

            return stack;
        }
    };

    public ItemMoss(String domain, String name) {
        super(domain, name, 3, 0, false);

        IBehaviorDispenseItem previous = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.containsKey(Items.SHEARS) ? BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(Items.SHEARS) : null;

        if (previous != null && previous != dispenserBehavior) {
            vanillaDispenserBehaviour = previous;
        }

        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SHEARS, dispenserBehavior);
    }

    public boolean tryConvert(World world, BlockState state, BlockPos pos, @Nullable PlayerEntity player) {
        BlockState converted = affected.getConverted(state);

        if (!state.equals(converted)) {
            world.setBlockState(pos, converted, 3);

            world.playSound(null, pos, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1, 1);

            int amount = 1;

            if (player != null && SpeciesList.instance().getPlayer(player).getSpecies().canUseEarth()) {
                amount = world.rand.nextInt(4);
            }

            Block.spawnAsEntity(world, pos, new ItemStack(this, amount));

            return true;
        }

        return false;
    }

    @Override
    public Toxicity getToxicityLevel(ItemStack stack) {
        return Toxicity.MILD;
    }
}

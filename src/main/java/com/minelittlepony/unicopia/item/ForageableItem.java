package com.minelittlepony.unicopia.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class ForageableItem extends Item {
    private static final List<ForageableItem> REGISTRY = new ArrayList<>();
    static {
        UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) -> {
            if (player.shouldCancelInteraction()) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isIn(ItemTags.HOES)) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            ActionResult result = ActionResult.PASS;

            if (state.isIn(BlockTags.LEAVES)) {
                player.swingHand(hand);
                world.playSound(player, pos, state.getSoundGroup().getHitSound(), SoundCategory.BLOCKS);
                InteractionManager.instance().addBlockBreakingParticles(pos, hitResult.getSide());

                int miningLevel = (stack.getItem() instanceof HoeItem hoe ? hoe.getMaterial().getMiningLevel() : 59);

                for (ForageableItem item : REGISTRY) {
                    if ((result = item.onTryForage(world, pos, state, stack, player, miningLevel)).isAccepted()) {
                        stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                        return result;
                    }
                }
            }

            return result.isAccepted() ? ActionResult.SUCCESS : ActionResult.PASS;
        });
    }

    private final Supplier<Block> targetBlock;

    public ForageableItem(Settings settings, Supplier<Block> targetBlock) {
        super(settings);
        this.targetBlock = targetBlock;
        REGISTRY.add(this);
    }

    public ActionResult onTryForage(World world, BlockPos pos, BlockState state, ItemStack stack, PlayerEntity player, int miningLevel) {
        if (state.isOf(targetBlock.get())) {
            int spawnChance = (int)((1F - MathHelper.clamp(miningLevel / (float)ToolMaterials.NETHERITE.getMiningLevel(), 0, 1)) * 32);
            spawnChance -= EnchantmentUtil.getLuck(1, player);

            if (spawnChance <= 0 || world.random.nextInt(spawnChance) == 0) {
                Block.dropStack(world, pos, new ItemStack(this, 1 + EnchantmentHelper.getLooting(player)));
                world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
                if (BlockDestructionManager.of(world).damageBlock(pos, world.getRandom().nextBetween(3, 7)) >= BlockDestructionManager.MAX_DAMAGE) {
                    world.breakBlock(pos, true);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }
}

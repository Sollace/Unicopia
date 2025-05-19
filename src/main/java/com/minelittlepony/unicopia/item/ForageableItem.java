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
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.RegistryKeys;
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
                InteractionManager.getInstance().addBlockBreakingParticles(pos, hitResult.getSide());

                float foragingChance = getForagingChance(stack);

                for (ForageableItem item : REGISTRY) {
                    if ((result = item.onTryForage(world, pos, state, stack, player, foragingChance)).isAccepted()) {
                        stack.damage(1, player, LivingEntity.getSlotForHand(hand));
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

    public ActionResult onTryForage(World world, BlockPos pos, BlockState state, ItemStack stack, PlayerEntity player, float spawnChance) {
        if (!state.isOf(targetBlock.get())) {
            return ActionResult.PASS;
        }

        spawnChance -= EnchantmentUtil.getLuck(1, player);

        if (spawnChance <= 0 || world.random.nextInt((int)(spawnChance * 32)) == 0) {
            Block.dropStack(world, pos, new ItemStack(this, 1 + EnchantmentHelper.getEquipmentLevel(player.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.LOOTING).get(), player)));
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
            if (BlockDestructionManager.of(world).damageBlock(pos, world.getRandom().nextBetween(3, 7)) >= BlockDestructionManager.MAX_DAMAGE) {
                world.breakBlock(pos, true);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }

    static float getForagingChance(ItemStack stack) {
        if (!(stack.getItem() instanceof HoeItem hoe)) {
            return 0.25F;
        }

        float spawnChance = hoe.getMaterial() instanceof ToolMaterials m ? switch(m) {
                case WOOD -> 0.25F;
                case STONE -> 0.3F;
                case IRON -> 0.4F;
                case GOLD -> 0.6F;
                case DIAMOND -> 0.7F;
                case NETHERITE -> 0.8F;
                default -> 0.25F;
        } : MathHelper.clamp(hoe.getMaterial().getMiningSpeedMultiplier(), 0, 1);

        return 1F - spawnChance;
    }
}

package com.minelittlepony.unicopia.redux.client;

import static com.minelittlepony.unicopia.core.EquinePredicates.MAGI;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.core.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.redux.UBlocks;
import com.minelittlepony.unicopia.redux.block.IColourful;
import com.minelittlepony.unicopia.redux.item.UItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;

public class UnicopiaReduxClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
      //BuildInTexturesBakery.getBuiltInTextures().add(new Identifier(Unicopia.MODID, "items/empty_slot_gem"));

        ColorProviderRegistry.ITEM.register((stack, tint) -> {
            return getLeavesColor(((BlockItem)stack.getItem()).getBlock().getDefaultState(), null, null, tint);
        }, UItems.apple_leaves);
        ColorProviderRegistry.BLOCK.register(UnicopiaReduxClient::getLeavesColor, UBlocks.apple_leaves);
        ColorProviderRegistry.ITEM.register((stack, tint) -> {
            if (MAGI.test(MinecraftClient.getInstance().player)) {
                return SpellRegistry.instance().getSpellTintFromStack(stack);
            }
            return 0xFFFFFF;
        }, UItems.spell, UItems.curse);
    }

    private static int getLeavesColor(BlockState state, @Nullable ExtendedBlockView world, @Nullable BlockPos pos, int tint) {
        Block block = state.getBlock();

        if (block instanceof IColourful) {
            return ((IColourful)block).getCustomTint(state, tint);
        }

        if (world != null && pos != null) {
            return BiomeColors.getGrassColor(world, pos);
        }

        return GrassColors.getColor(0.5D, 1);
    }
}

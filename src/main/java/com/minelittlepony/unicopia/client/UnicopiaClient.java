package com.minelittlepony.unicopia.client;

import static com.minelittlepony.unicopia.EquinePredicates.PLAYER_UNICORN;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ducks.Colourful;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.mixin.client.DefaultTexturesRegistry;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.world.block.UBlocks;
import com.minelittlepony.unicopia.world.container.SpellbookResultSlot;
import com.minelittlepony.unicopia.world.item.UItems;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class UnicopiaClient implements ClientModInitializer {

    private final KeyBindingsHandler keyboard = new KeyBindingsHandler();

    private Race lastPreferredRace = InteractionManager.instance().getPreferredRace();

    @Override
    public void onInitializeClient() {
        lastPreferredRace = InteractionManager.instance().getPreferredRace();
        InteractionManager.INSTANCE = new ClientInteractionManager();

        URenderers.bootstrap();

        ClientTickCallback.EVENT.register(this::tick);
        DefaultTexturesRegistry.getDefaultTextures().add(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, SpellbookResultSlot.EMPTY_GEM_SLOT));

        ColorProviderRegistry.ITEM.register((stack, tint) -> {
            return getLeavesColor(((BlockItem)stack.getItem()).getBlock().getDefaultState(), null, null, tint);
        }, UItems.APPLE_LEAVES);
        ColorProviderRegistry.BLOCK.register(UnicopiaClient::getLeavesColor, UBlocks.APPLE_LEAVES);
        ColorProviderRegistry.ITEM.register((stack, tint) -> {
            if (PLAYER_UNICORN.test(MinecraftClient.getInstance().player)) {
                return SpellRegistry.instance().getSpellTintFromStack(stack);
            }
            return 0xFFFFFF;
        }, UItems.GEM, UItems.CORRUPTED_GEM);
    }

    private void tick(MinecraftClient client) {
        PlayerEntity player = client.player;

        if (player != null && !player.removed) {
            Race newRace = InteractionManager.instance().getPreferredRace();

            if (newRace != lastPreferredRace) {
                lastPreferredRace = newRace;

                Channel.REQUEST_CAPABILITIES.send(new MsgRequestCapabilities(lastPreferredRace));
            }
        }

        keyboard.tick(client);
    }

    private static int getLeavesColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tint) {
        Block block = state.getBlock();

        if (block instanceof Colourful) {
            return ((Colourful)block).getCustomTint(state, tint);
        }

        if (world != null && pos != null) {
            return BiomeColors.getGrassColor(world, pos);
        }

        return GrassColors.getColor(0.5D, 1);
    }

}

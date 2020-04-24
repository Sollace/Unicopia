package com.minelittlepony.unicopia.client;

import static com.minelittlepony.unicopia.EquinePredicates.MAGI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.common.event.ClientReadyCallback;
import com.minelittlepony.unicopia.Config;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.container.SpellbookResultSlot;
import com.minelittlepony.unicopia.ducks.Colourful;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.mixin.client.DefaultTexturesRegistry;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.util.dummy.DummyClientPlayerEntity;
import com.mojang.authlib.GameProfile;
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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class UnicopiaClient extends InteractionManager implements ClientModInitializer {

    private final KeyBindingsHandler keyboard = new KeyBindingsHandler();

    /**
     * The race preferred by the client - as determined by mine little pony.
     * Human if minelp was not installed.
     *
     * This is not neccessarily the _actual_ race used for the player,
     * as the server may not allow certain race types, or the player may override
     * this option in-game themselves.
     */
    private static Race clientPlayerRace = getclientPlayerRace();

    @Override
    public void onInitializeClient() {
        clientPlayerRace = getclientPlayerRace();
        InteractionManager.INSTANCE = this;

        URenderers.bootstrap();

        ClientTickCallback.EVENT.register(this::tick);
        ClientReadyCallback.EVENT.register(client -> Abilities.getInstance().getValues().forEach(keyboard::addKeybind));

        DefaultTexturesRegistry.getDefaultTextures().add(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX, SpellbookResultSlot.EMPTY_GEM_SLOT));

        ColorProviderRegistry.ITEM.register((stack, tint) -> {
            return getLeavesColor(((BlockItem)stack.getItem()).getBlock().getDefaultState(), null, null, tint);
        }, UItems.APPLE_LEAVES);
        ColorProviderRegistry.BLOCK.register(UnicopiaClient::getLeavesColor, UBlocks.APPLE_LEAVES);
        ColorProviderRegistry.ITEM.register((stack, tint) -> {
            if (MAGI.test(MinecraftClient.getInstance().player)) {
                return SpellRegistry.instance().getSpellTintFromStack(stack);
            }
            return 0xFFFFFF;
        }, UItems.GEM, UItems.CORRUPTED_GEM);
    }

    private void tick(MinecraftClient client) {
        PlayerEntity player = client.player;

        if (player != null && !player.removed) {
            Race newRace = getclientPlayerRace();

            if (newRace != clientPlayerRace) {
                clientPlayerRace = newRace;

                Channel.REQUEST_CAPABILITIES.send(new MsgRequestCapabilities(player, clientPlayerRace));
            }
        }

        keyboard.onKeyInput();
    }

    @Override
    @Nonnull
    public PlayerEntity createPlayer(Entity observer, GameProfile profile) {
        if (observer.world instanceof ClientWorld) {
            return new DummyClientPlayerEntity((ClientWorld)observer.world, profile);
        }
        return super.createPlayer(observer, profile);
    }

    @Override
    public boolean isClientPlayer(@Nullable PlayerEntity player) {
        if (MinecraftClient.getInstance().player == player) {
            return true;
        }

        if (MinecraftClient.getInstance().player == null || player == null) {
            return false;
        }

        return Pony.equal(MinecraftClient.getInstance().player, player);
    }

    @Override
    public int getViewMode() {
        return MinecraftClient.getInstance().options.perspective;
    }

    private static Race getclientPlayerRace() {
        if (!Config.getInstance().ignoresMineLittlePony()
                && MinecraftClient.getInstance().player != null) {
            Race race = MineLPConnector.getPlayerPonyRace();

            if (!race.isDefault()) {
                return race;
            }
        }

        return Config.getInstance().getPrefferedRace();
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

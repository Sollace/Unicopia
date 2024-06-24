package com.minelittlepony.unicopia.client;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.FlightType;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.client.gui.DismissSpellScreen;
import com.minelittlepony.unicopia.client.gui.spellbook.ClientChapters;
import com.minelittlepony.unicopia.client.particle.ClientBoundParticleSpawner;
import com.minelittlepony.unicopia.client.sound.*;
import com.minelittlepony.unicopia.entity.player.PlayerPhysics;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.entity.player.dummy.DummyClientPlayerEntity;
import com.minelittlepony.unicopia.particle.ParticleSpawner;
import com.mojang.authlib.GameProfile;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.AggressiveBeeSoundInstance;
import net.minecraft.client.sound.MovingMinecartSoundInstance;
import net.minecraft.client.sound.PassiveBeeSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ClientInteractionManager extends InteractionManager {

    private final MinecraftClient client = MinecraftClient.getInstance();

    private final Int2ObjectMap<WeakReference<TickableSoundInstance>> playingSounds = new Int2ObjectOpenHashMap<>();

    @Override
    public Map<Identifier, ?> readChapters(PacketByteBuf buffer) {
        return buffer.readMap(PacketByteBuf::readIdentifier, ClientChapters::loadChapter);
    }

    @Override
    public void playLoopingSound(Entity source, int type, long seed) {
        client.execute(() -> {
            if (type == SOUND_EARS_RINGING && source instanceof LivingEntity living) {
                play(type, () -> new LoopingSoundInstance<>(living,
                        createTicker(100).and(e -> !e.isRemoved()),
                        USounds.ENTITY_PLAYER_EARS_RINGING, 0.01F, 2, Random.create(seed)).setFadeIn()
                );
            } else if (type == SOUND_BEE && source instanceof BeeEntity bee) {
                play(type, () ->
                        bee.hasAngerTime()
                            ? new AggressiveBeeSoundInstance(bee)
                            : new PassiveBeeSoundInstance(bee)
                );
            } else if (type == SOUND_MINECART && source instanceof AbstractMinecartEntity minecart) {
                play(type, () -> new MovingMinecartSoundInstance(minecart));
            } else if (type == SOUND_CHANGELING_BUZZ && source instanceof PlayerEntity player) {
                play(type, () -> new MotionBasedSoundInstance<>(USounds.ENTITY_PLAYER_CHANGELING_BUZZ, player, e -> {
                    PlayerPhysics physics = Pony.of(e).getPhysics();
                    return physics.isFlying() && physics.getFlightType() == FlightType.INSECTOID;
                }, 0.25F, 0.5F, 0.66F, Random.create(seed)));
            } else if (type == SOUND_GLIDING && source instanceof PlayerEntity player && isClientPlayer(player)) {
                play(type, () -> new MotionBasedSoundInstance<>(USounds.Vanilla.ITEM_ELYTRA_FLYING, player, e -> {
                    Pony pony = Pony.of(e);
                    return pony.getPhysics().isFlying() && pony.getPhysics().getFlightType().isAvian();
                }, 0, 1, 1, Random.create(seed)));
            } else if (type == SOUND_GLIDING && source instanceof PlayerEntity player) {
                play(type, () -> new MotionBasedSoundInstance<>(USounds.ENTITY_PLAYER_PEGASUS_FLYING, player, e -> {
                    Pony pony = Pony.of(e);
                    return pony.getPhysics().isFlying() && pony.getPhysics().getFlightType().isAvian();
                }, 0, 1, 1, Random.create(seed)));
            } else if (type == SOUND_MAGIC_BEAM) {
                play(type, () -> new LoopedEntityTrackingSoundInstance(USounds.SPELL_CAST_SHOOT, 0.3F, 1F, source, seed));
            } else if (type == SOUND_HEART_BEAT) {
                play(type, () -> new NonLoopingFadeOutSoundInstance(USounds.ENTITY_PLAYER_HEARTBEAT_LOOP, SoundCategory.PLAYERS, 0.3F, Random.create(seed), 80L));
            } else if (type == SOUND_KIRIN_RAGE) {
                play(type, () -> new FadeOutSoundInstance(USounds.ENTITY_PLAYER_KIRIN_RAGE_LOOP, SoundCategory.AMBIENT, 0.3F, Random.create(seed)) {
                    @Override
                    protected boolean shouldKeepPlaying() {
                        return EquinePredicates.RAGING.test(source);
                    }
                });
            }
        });
    }

    private void play(int type, Supplier<TickableSoundInstance> soundSupplier) {
        WeakReference<TickableSoundInstance> activeSound = playingSounds.get(type);
        TickableSoundInstance existing;
        if (activeSound == null || (existing = activeSound.get()) == null || existing.isDone()) {
            existing = soundSupplier.get();
            playingSounds.put(type, new WeakReference<>(existing));
            playNow(existing);
        }
    }

    private void playNow(TickableSoundInstance sound) {
        client.getSoundManager().playNextTick(sound);
    }

    static Predicate<LivingEntity> createTicker(int ticks) {
        int[] ticker = new int[] {ticks};
        return entity -> ticker[0]-- > 0;
    }

    @Override
    public void openScreen(int type) {
        client.execute(() -> {
            if (type == SCREEN_DISPELL_ABILITY) {
                client.setScreen(new DismissSpellScreen());
            }
        });
    }

    @Override
    @NotNull
    public PlayerEntity createPlayer(World world, GameProfile profile) {
        if (world instanceof ClientWorld) {
            return new DummyClientPlayerEntity((ClientWorld)world, profile);
        }
        return super.createPlayer(world, profile);
    }

    @Override
    public boolean isClientPlayer(@Nullable PlayerEntity player) {
        return (client.player != null && player != null)
             && (client.player == player
                 || Pony.equal(client.player, player));
    }

    @Override
    public int getViewMode() {
        return client.options.getPerspective().ordinal();
    }

    @Override
    public float getTickRate() {
        return client.world == null ? 20 : client.world.getTickManager().getTickRate();
    }

    @Override
    public ParticleSpawner createBoundParticle(UUID id) {
        return new ClientBoundParticleSpawner(id);
    }

    @Override
    public void sendPlayerLookAngles(PlayerEntity player) {
        if (player instanceof ClientPlayerEntity c) {
            c.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(player.getYaw(), player.getPitch(), player.isOnGround()));
        }
    }

    @Override
    public void addBlockBreakingParticles(BlockPos pos, Direction direction) {
        client.particleManager.addBlockBreakingParticles(pos, direction);
    }

    @Override
    public Optional<Pony> getClientPony() {
        return Optional.ofNullable(client.player).map(Pony::of);
    }
}

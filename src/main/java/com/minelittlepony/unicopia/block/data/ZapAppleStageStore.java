package com.minelittlepony.unicopia.block.data;

import java.util.Locale;
import java.util.stream.StreamSupport;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.nbt.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ZapAppleStageStore extends PersistentState implements Tickable {
    private static final Identifier ID = Unicopia.id("zap_apple_stage");

    public static ZapAppleStageStore get(World world) {
        return WorldOverlay.getPersistableStorage(world, ID, ZapAppleStageStore::new, ZapAppleStageStore::new);
    }

    private final World world;

    private Stage lastStage = Stage.HIBERNATING;
    private int countdown;
    private boolean stageChanged;
    private boolean playedMoonEffect;
    private int nextLightningEvent = 1200;

    ZapAppleStageStore(World world, NbtCompound compound) {
        this(world);
        lastStage = Stage.VALUES[Math.max(0, compound.getInt("stage")) % Stage.VALUES.length];
        stageChanged = compound.getBoolean("stageChanged");
        countdown = compound.getInt("countdown");
        playedMoonEffect = compound.getBoolean("playedMoonEffect");
        nextLightningEvent = compound.getInt("nextLightningEvent");
    }

    ZapAppleStageStore(World world) {
        this.world = world;
    }

    @Override
    public void tick() {
        if (!world.isDay()) {
            if (nextLightningEvent > 0) {
                nextLightningEvent--;
                markDirty();
            }

            if (!stageChanged && (lastStage != Stage.HIBERNATING || (world.getMoonPhase() == 0))) {
                stageChanged = true;
                if (countDay()) {
                    lastStage = lastStage.getNext();
                    countdown = 1;
                    playedMoonEffect = false;
                    markDirty();
                    onStageChanged();
                }
            }
        } else if (stageChanged) {
            stageChanged = false;
            markDirty();
        }
    }

    private boolean countDay() {
        markDirty();
        return countdown-- <= 0;
    }

    protected void onStageChanged() {
        world.setRainGradient(0.5F);
    }

    public void playMoonEffect(BlockPos pos) {
        if (!playedMoonEffect) {
            playedMoonEffect = true;
            markDirty();
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_WOLF_HOWL, SoundCategory.BLOCKS, 1.5F, 0.9F, true);
        }
    }

    public void triggerLightningStrike(BlockPos pos) {
        world.emitGameEvent(GameEvent.LIGHTNING_STRIKE, pos, GameEvent.Emitter.of(world.getBlockState(pos)));
        ParticleUtils.spawnParticle(world, UParticles.LIGHTNING_BOLT, Vec3d.ofCenter(pos), Vec3d.ZERO);

        if (nextLightningEvent <= 0) {
            StreamSupport.stream(BlockPos.iterateRandomly(world.random, 20, pos, 10).spliterator(), false)
                .filter(p -> world.isAir(p) && !world.isAir(p.down()) && world.isSkyVisible(p))
                .findFirst().ifPresent(p -> {
                    LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
                    bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(pos));
                    bolt.setCosmetic(true);
                    world.spawnEntity(bolt);
                    nextLightningEvent = world.getRandom().nextBetween(1200, 8000);
                    markDirty();
                });
        }
    }

    /**
     * Returns true during nights that the zap apples must change their states.
     * @return
     */
    public boolean hasStageChanged() {
        return stageChanged;
    }

    /**
     * Returns the current zap apple ripening stage.
     */
    public Stage getStage() {
        return lastStage;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        compound.putInt("stage", lastStage.ordinal());
        compound.putBoolean("stageChanged", stageChanged);
        compound.putInt("countdown", countdown);
        compound.putBoolean("playedMoonEffect", playedMoonEffect);
        compound.putInt("nextLightningEvent", nextLightningEvent);
        return compound;
    }

    public enum Stage implements StringIdentifiable {
        HIBERNATING,
        GREENING,
        FLOWERING,
        FRUITING,
        RIPE;

        static final long DAY_LENGTH = 24000;
        static final Stage[] VALUES = values();

        public Stage getNext() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }

        public boolean mustChangeInto(Stage to) {
            return this != to && (getNext() == to || this == HIBERNATING || to == HIBERNATING);
        }

        public boolean mustChangeIntoInstantly(Stage to) {
            return this != to && (this == HIBERNATING || to == HIBERNATING);
        }

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}

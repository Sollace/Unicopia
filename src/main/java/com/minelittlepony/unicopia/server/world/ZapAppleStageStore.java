package com.minelittlepony.unicopia.server.world;

import java.util.Locale;
import java.util.stream.StreamSupport;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgZapAppleStage;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.nbt.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;

public class ZapAppleStageStore extends PersistentState implements Tickable {
    private static final Identifier ID = Unicopia.id("zap_apple_stage");
    static final long DAY_LENGTH = World.field_30969;
    static final long MOON_PHASES = DimensionType.MOON_SIZES.length;

    public static ZapAppleStageStore get(World world) {
        return WorldOverlay.getPersistableStorage(world, ID, ZapAppleStageStore::new, ZapAppleStageStore::new);
    }

    private final World world;

    private Stage lastStage = Stage.HIBERNATING;
    private long stageDelta;
    private long lastTime;

    private boolean stageChanged;
    private boolean playedMoonEffect;
    private int nextLightningEvent = 1200;

    ZapAppleStageStore(World world, NbtCompound compound) {
        this(world);
        lastStage = Stage.VALUES[Math.max(0, compound.getInt("stage")) % Stage.VALUES.length];
        stageDelta = compound.getLong("stageDelta");
        stageChanged = compound.getBoolean("stageChanged");
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
            }

            if (!stageChanged && (lastStage != Stage.HIBERNATING || (world.getMoonPhase() == 0))) {
                stageChanged = true;
                lastStage = lastStage.getNext();
                stageDelta = 0;
                playedMoonEffect = false;
                sendUpdate();
            }
        } else if (stageChanged) {
            stageChanged = false;
        }

        long timeOfDay = world.getTimeOfDay();
        if (stageDelta != 0 && (timeOfDay < lastTime || timeOfDay > lastTime + 10)) {
            long timeDifference = timeOfDay - lastTime;
            Unicopia.LOGGER.info("Times a changing {}!", timeDifference);
            while (timeDifference < 0) {
                timeDifference += DAY_LENGTH;
            }
            stageDelta += timeDifference;
            sendUpdate();
        }
        lastTime = timeOfDay;


        stageDelta++;
        markDirty();
    }

    protected void sendUpdate() {
        Channel.SERVER_ZAP_STAGE.sendToAllPlayers(new MsgZapAppleStage(getStage(), stageDelta), world);
    }

    public void playMoonEffect(BlockPos pos) {
        if (!playedMoonEffect) {
            playedMoonEffect = true;
            markDirty();

            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), USounds.Vanilla.ENTITY_WOLF_HOWL, SoundCategory.BLOCKS, 1.5F, 0.3F, world.random.nextInt(1200));
        }
    }

    public void triggerLightningStrike(BlockPos pos) {
        world.emitGameEvent(GameEvent.LIGHTNING_STRIKE, pos, GameEvent.Emitter.of(world.getBlockState(pos)));
        ParticleUtils.spawnParticle(world, LightningBoltParticleEffect.DEFAULT, Vec3d.ofCenter(pos), Vec3d.ZERO);

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

    public long getStageDelta() {
        return stageDelta;
    }

    public float getStageProgress() {
        return getStage().getStageProgress(getStageDelta());
    }

    public float getCycleProgress() {
        return getStage().getCycleProgress(getStageDelta());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        compound.putInt("stage", lastStage.ordinal());
        compound.putLong("stageDelta", stageDelta);
        compound.putBoolean("stageChanged", stageChanged);
        compound.putBoolean("playedMoonEffect", playedMoonEffect);
        compound.putInt("nextLightningEvent", nextLightningEvent);
        return compound;
    }

    public enum Stage implements StringIdentifiable {
        HIBERNATING(MOON_PHASES * DAY_LENGTH),
        GREENING(DAY_LENGTH),
        FLOWERING(DAY_LENGTH),
        FRUITING(DAY_LENGTH),
        RIPE(DAY_LENGTH);

        static final Stage[] VALUES = values();

        private final long duration;

        Stage(long duration) {
            this.duration = duration;
        }

        public Stage getNext() {
            return byId((ordinal() + 1) % VALUES.length);
        }

        public Stage getPrevious() {
            return byId(((ordinal() - 1) + VALUES.length) % VALUES.length);
        }

        public float getStageProgress(long time) {
            return (time % duration) / (float)duration;
        }

        public float getCycleProgress(long time) {
            float ordinal = ordinal();
            return MathHelper.lerp(getStageProgress(time), ordinal, ordinal + 1) / 5F;
        }

        public static Stage byId(int id) {
            return VALUES[MathHelper.clamp(id, 0, VALUES.length)];
        }

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}

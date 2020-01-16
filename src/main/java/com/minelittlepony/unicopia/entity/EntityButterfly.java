package com.minelittlepony.unicopia.entity;

import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnEntry;

public class EntityButterfly extends AmbientEntity {

    public static final EntityType<EntityButterfly> TYPE = EntityType.Builder.create(EntityButterfly::new, EntityCategory.AMBIENT)
            .setDimensions(0.1F, 0.1F)
            .build("butterfly");

    public static final SpawnEntry SPAWN_ENTRY = new SpawnEntry(TYPE, 15, 9, 15);

    private static final TrackedData<Boolean> RESTING = DataTracker.registerData(EntityButterfly.class, TrackedDataHandlerRegistry.BOOLEAN);

    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(EntityButterfly.class, TrackedDataHandlerRegistry.INTEGER);

    private BlockPos hoveringPosition;

    public EntityButterfly(EntityType<EntityButterfly> type, World world) {
        super(type, world);
        setVariaty(Variant.random(world.random));
        setResting(true);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(2);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(VARIANT, Variant.BUTTERFLY.ordinal());
        getDataTracker().startTracking(RESTING, false);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean collides() {
        return false;
     }

    @Override
    public void tick() {
        super.tick();

        Vec3d vel = getVelocity();
        setVelocity(vel.x, y * 0.6, vel.z);
    }

    public boolean isResting() {
        return getDataTracker().get(RESTING);
    }

    public void setResting(boolean resting) {
        getDataTracker().set(RESTING, resting);
    }

    public Variant getVariety() {
        Variant[] values = Variant.values();
        return values[getDataTracker().get(VARIANT) % values.length];
    }

    public void setVariaty(Variant variant) {
        getDataTracker().set(VARIANT, variant.ordinal());
    }

    protected boolean isAggressor(Entity e) {
        if (e instanceof EntityButterfly) {
            return false;
        }

        if (e instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)e;

            if (player.isCreative() || player.isSpectator()) {
                return false;
            }

            if (player.isSprinting() || player.isHandSwinging || player.forwardSpeed > 0 || player.sidewaysSpeed > 0) {
                return true;
            }
            // TODO:
        }/* else if (!IMob.VISIBLE_MOB_SELECTOR.test(e)) {
            return false;
        }*/

        return e.getVelocity().x != 0 || e.getVelocity().z != 0;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        BlockPos pos = getBlockPos();
        BlockPos below = pos.down();

        if (isResting()) {
            if (world.getBlockState(below).isOpaque()) {
                if (world.getEntities(this, getBoundingBox().expand(7)).stream().anyMatch(this::isAggressor)) {
                    setResting(false);
                }
            } else {
                setResting(false);
            }

        } else {

            // invalidate the hovering position
            if (hoveringPosition != null && (!world.isAir(hoveringPosition) || hoveringPosition.getY() < 1)) {
                hoveringPosition = null;
            }

            // select a new hovering position
            if (hoveringPosition == null || random.nextInt(30) == 0 || hoveringPosition.getSquaredDistance(pos) < 4) {
                hoveringPosition = new BlockPos(x + random.nextInt(7) - random.nextInt(7), y + random.nextInt(6) - 2, z + random.nextInt(7) - random.nextInt(7));
            }

            // hover casually towards the chosen position

            double changedX = hoveringPosition.getX() + 0.5D - x;
            double changedY = hoveringPosition.getY() + 0.1D - y;
            double changedZ = hoveringPosition.getZ() + 0.5D - z;

            Vec3d vel = getVelocity();

            setVelocity(
                vel.x + (Math.signum(changedX) * 0.5D - vel.x) * 0.10000000149011612D,
                vel.y + (Math.signum(changedY) * 0.699999988079071D - vel.y) * 0.10000000149011612D,
                vel.z + (Math.signum(changedZ) * 0.5D - vel.z) * 0.10000000149011612D
            );

            float f = (float)(MathHelper.atan2(vel.z, vel.x) * (180 / Math.PI)) - 90;

            forwardSpeed = 0.5F;
            headYaw += MathHelper.wrapDegrees(f - headYaw);

            if (random.nextInt(100) == 0 && world.getBlockState(below).isOpaque()) {
                setResting(true);
            }
        }
    }

    @Override
    public void handleFallDamage(float distance, float damageMultiplier) {
    }

    @Override
    protected void fall(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean canSpawn(IWorld world, SpawnType type) {
        if (type == SpawnType.NATURAL) {
            return y < world.getSeaLevel() && world.getLightLevel(getBlockPos()) > 7;
        }
        return true;
    }

    @Override
    public float getEyeHeight(EntityPose pos) {
        return getHeight() / 2;
    }

    public enum Variant {
        BUTTERFLY,
        YELLOW,
        LIME,
        RED,
        GREEN,
        BLUE,
        PURPLE,
        MAGENTA,
        PINK,
        HEDYLIDAE,
        LYCAENIDAE,
        NYMPHALIDAE,
        MONARCH,
        WHITE_MONARCH,
        BRIMSTONE;

        private final Identifier skin = new Identifier(Unicopia.MODID, "textures/entity/butterfly/" + name().toLowerCase() + ".png");

        public Identifier getSkin() {
            return skin;
        }

        static Variant random(Random rand) {
            Variant[] values = values();
            return values[rand.nextInt(values.length)];
        }
    }
}

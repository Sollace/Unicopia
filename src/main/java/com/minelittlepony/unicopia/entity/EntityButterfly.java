package com.minelittlepony.unicopia.entity;

import java.util.Random;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;

public class EntityButterfly extends EntityAmbientCreature {

    public static final SpawnListEntry SPAWN_ENTRY = new SpawnListEntry(EntityButterfly.class, 15, 9, 15);

    private static final DataParameter<Boolean> RESTING = EntityDataManager.createKey(EntityButterfly.class, DataSerializers.BOOLEAN);

    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityButterfly.class, DataSerializers.VARINT);

    private BlockPos hoveringPosition;

    public EntityButterfly(World world) {
        super(world);

        preventEntitySpawning = false;
        width = 0.1F;
        height = 0.1F;

        setVariaty(Variant.random(world.rand));
        setResting(true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(VARIANT, Variant.BUTTERFLY.ordinal());
        dataManager.register(RESTING, false);
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity entity) {
    }

    @Override
    protected void collideWithNearbyEntities() {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        motionY *= 0.6;
    }

    public boolean isResting() {
        return dataManager.get(RESTING);
    }

    public void setResting(boolean resting) {
        dataManager.set(RESTING, resting);
    }

    public Variant getVariety() {
        Variant[] values = Variant.values();
        return values[dataManager.get(VARIANT) % values.length];
    }

    public void setVariaty(Variant variant) {
        dataManager.set(VARIANT, variant.ordinal());
    }

    protected boolean isAggressor(Entity e) {
        if (e instanceof EntityButterfly) {
            return false;
        }

        if (e instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)e;

            if (player.isCreative() || player.isSpectator()) {
                return false;
            }

            if (player.isSprinting() || player.isSwingInProgress || player.moveForward > 0 || player.moveStrafing > 0) {
                return true;
            }
        } else if (!IMob.VISIBLE_MOB_SELECTOR.test(e)) {
            return false;
        }

        return Math.abs(e.motionX) > 0 || Math.abs(e.motionZ) > 0;
    }

    protected void updateAITasks() {
        super.updateAITasks();

        BlockPos pos = getPosition();
        BlockPos below = pos.down();

        if (isResting()) {
            if (world.getBlockState(below).isNormalCube()) {
                if (world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().grow(7)).stream().anyMatch(this::isAggressor)) {
                    setResting(false);
                }
            } else {
                setResting(false);
            }

        } else {

            // invalidate the hovering position
            if (hoveringPosition != null && (!world.isAirBlock(hoveringPosition) || hoveringPosition.getY() < 1)) {
                hoveringPosition = null;
            }

            // select a new hovering position
            if (hoveringPosition == null || rand.nextInt(30) == 0 || hoveringPosition.distanceSq(posX, posY, posZ) < 4) {
                hoveringPosition = new BlockPos(posX + rand.nextInt(7) - rand.nextInt(7), posY + rand.nextInt(6) - 2, posZ + rand.nextInt(7) - rand.nextInt(7));
            }

            // hover casually towards the chosen position

            double changedX = hoveringPosition.getX() + 0.5D - posX;
            double changedY = hoveringPosition.getY() + 0.1D - posY;
            double changedZ = hoveringPosition.getZ() + 0.5D - posZ;

            motionX += (Math.signum(changedX) * 0.5D - motionX) * 0.10000000149011612D;
            motionY += (Math.signum(changedY) * 0.699999988079071D - motionY) * 0.10000000149011612D;
            motionZ += (Math.signum(changedZ) * 0.5D - motionZ) * 0.10000000149011612D;

            float f = (float)(MathHelper.atan2(motionZ, motionX) * (180 / Math.PI)) - 90;

            moveForward = 0.5F;
            rotationYaw += MathHelper.wrapDegrees(f - rotationYaw);

            if (rand.nextInt(100) == 0 && world.getBlockState(below).isNormalCube()) {
                setResting(true);
            }
        }
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    public boolean getCanSpawnHere() {
        if (posY < world.getSeaLevel()) {
            return false;
        }

        return world.getLightFromNeighbors(getPosition()) > 7
                && super.getCanSpawnHere();
    }

    @Override
    public float getEyeHeight() {
        return height / 2;
    }

    public static enum Variant {
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

        private final ResourceLocation skin = new ResourceLocation(Unicopia.MODID, "textures/entity/butterfly/" + name().toLowerCase() + ".png");

        public ResourceLocation getSkin() {
            return skin;
        }

        static Variant random(Random rand) {
            Variant[] values = values();
            return values[rand.nextInt(values.length)];
        }
    }
}

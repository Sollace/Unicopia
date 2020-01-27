package com.minelittlepony.unicopia.redux.entity;

import com.minelittlepony.unicopia.core.EquinePredicates;
import com.minelittlepony.unicopia.core.entity.IMagicals;
import com.minelittlepony.unicopia.redux.item.UItems;

import net.minecraft.container.Container;
import net.minecraft.container.NameableContainerProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class SpellbookEntity extends MobEntity implements NameableContainerProvider, IMagicals {

    private static final TrackedData<Boolean> OPENED = DataTracker.registerData(SpellbookEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> ALTERED = DataTracker.registerData(SpellbookEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Byte> OPENED_USER = DataTracker.registerData(SpellbookEntity.class, TrackedDataHandlerRegistry.BYTE);

    public SpellbookEntity(EntityType<SpellbookEntity> type, World world) {
        super(type, world);
        setPersistent();

        if (world.random.nextInt(3) == 0) {
            setAltered();
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(OPENED, true);
        dataTracker.startTracking(OPENED_USER, (byte)1);
        dataTracker.startTracking(ALTERED, false);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean doesRenderOnFire() {
        return false;
    }

    public boolean getIsAltered() {
        return dataTracker.get(ALTERED);
    }

    public void setAltered() {
        dataTracker.set(ALTERED, true);
    }

    public boolean getIsOpen() {
        return dataTracker.get(OPENED);
    }

    public Boolean getUserSetState() {
        byte state = dataTracker.get(OPENED_USER);
        return state == 1 ? null : state == 2;
    }

    public void setIsOpen(boolean val) {
        dataTracker.set(OPENED, val);
    }

    public void setUserSetState(Boolean val) {
        dataTracker.set(OPENED_USER, val == null ? (byte)1 : val == true ? (byte)2 : (byte)0);
    }

    @Override
    public void tick() {
        boolean open = getIsOpen();
        jumping = open && isInWater();
        super.tick();
        if (open && world.isClient) {

            for (int offX = -2; offX <= 1; ++offX) {
                for (int offZ = -2; offZ <= 1; ++offZ) {
                    if (offX > -1 && offX < 1 && offZ == -1) {
                        offZ = 1;
                    }

                    if (random.nextInt(320) == 0) {
                        for (int offY = 0; offY <= 1; ++offY) {
                            world.addParticle(ParticleTypes.ENCHANT,
                                    x, y, z,
                                    offX/2F + random.nextFloat(),
                                    offY/2F - random.nextFloat() + 0.5f,
                                    offZ/2F + random.nextFloat()
                            );
                        }
                    }
                }
            }
        }

        if (world.random.nextInt(30) == 0) {
            float celest = world.getStarsBrightness(1) * 4;

            boolean isDay = celest > 3 || celest < 1;

            Boolean userState = getUserSetState();

            boolean canToggle = (isDay != open) && (userState == null || userState == isDay);

            if (canToggle) {
                setUserSetState(null);
                setIsOpen(isDay);
            }

            if (userState != null && (isDay == open) && (userState == open)) {
                setUserSetState(null);
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!world.isClient) {
            remove();

            BlockSoundGroup sound = BlockSoundGroup.WOOD;

            world.playSound(x, y, z, sound.getBreakSound(), SoundCategory.BLOCKS, sound.getVolume(), sound.getPitch(), true);

            if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
                dropItem(UItems.spellbook, 1);
            }
        }
        return false;
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {
        if (player.isSneaking()) {
            boolean open = !getIsOpen();

            setIsOpen(open);
            setUserSetState(open);

            return ActionResult.SUCCESS;
        }

        if (EquinePredicates.MAGI.test(player)) {

            player.playSound(SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, 2, 1);

            player.openContainer(this);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
        return null;
    }

    @Override
    public void readCustomDataFromTag(CompoundTag compound) {
        super.readCustomDataFromTag(compound);

        setIsOpen(compound.getBoolean("open"));
        setUserSetState(compound.containsKey("force_open") ? compound.getBoolean("force_open") : null);
    }

    @Override
    public void writeCustomDataToTag(CompoundTag compound) {
        super.writeCustomDataToTag(compound);
        compound.putBoolean("open", getIsOpen());

        Boolean state = getUserSetState();

        if (state != null) {
            compound.putBoolean("force_open", state);
        }
    }

    // TODO:
    /*@Override
    public ItemStack getPickStack(HitResult target) {
        return new ItemStack(UItems.spellbook);
    }*/
}

package com.minelittlepony.unicopia.projectile;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.mob.UEntities;

import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class PhysicsBodyProjectileEntity extends PersistentProjectileEntity implements FlyingItemEntity {

    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(PhysicsBodyProjectileEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Boolean> BOUNCY = DataTracker.registerData(PhysicsBodyProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private int inWaterTime;

    public PhysicsBodyProjectileEntity(EntityType<PhysicsBodyProjectileEntity> type, World world) {
        super(type, world);
    }

    public PhysicsBodyProjectileEntity(World world) {
        this(UEntities.MUFFIN, world);
    }

    public PhysicsBodyProjectileEntity(World world, @Nullable LivingEntity thrower) {
        super(UEntities.MUFFIN, thrower, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(ITEM, ItemStack.EMPTY);
        getDataTracker().startTracking(BOUNCY, false);
    }

    public void setStack(ItemStack stack) {
        getDataTracker().set(ITEM, stack);
    }

    @Override
    public ItemStack getStack() {
        return getDataTracker().get(ITEM);
    }

    @Override
    protected ItemStack asItemStack() {
        return getStack();
    }

    public void setBouncy() {
        getDataTracker().set(BOUNCY, true);
    }

    public boolean isBouncy() {
        return getDataTracker().get(BOUNCY);
    }

    @Override
    public void tick() {
        super.tick();
        if (inGround) {
            Vec3d vel = getVelocity();
            vel = vel.multiply(0, 1, 0);

            move(MovementType.SELF, vel);

            setVelocity(vel.multiply(0.3));
            addVelocity(0, -0.025, 0);
        }

        if (isBouncy() && isInsideWaterOrBubbleColumn()) {
            setVelocity(getVelocity().multiply(0.3).add(0, 0.05125, 0));
            inWaterTime++;
        } else {
            inWaterTime = 0;
        }
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {

        if (getWorld().isClient || isNoClip() || shake > 0) {
            return;
        }

        if (inWaterTime <= 0) {
            super.onPlayerCollision(player);
        } else if (tryPickup(player)) {
            player.sendPickup(this, 1);
            discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        if (isBouncy()) {
            setVelocity(getVelocity().multiply(-0.1, -0.3, -0.1));
            setYaw(getYaw() + 180);
            prevYaw += 180;
            return;
        }
        super.onEntityHit(hit);
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        BlockPos buttonPos = hit.getBlockPos().offset(hit.getSide());
        BlockState state = getWorld().getBlockState(buttonPos);

        if (state.isIn(BlockTags.WOODEN_BUTTONS) && state.getBlock() instanceof ButtonBlock button) {
            button.powerOn(state, getWorld(), buttonPos);
        } else if (state.getBlock() instanceof LeverBlock lever) {
            lever.togglePower(state, getWorld(), buttonPos);
        }

        BlockPos belowPos = buttonPos.down();
        BlockState below = getWorld().getBlockState(belowPos);
        ItemStack stack = getStack();
        if (below.getBlock() instanceof HopperBlock hopper) {
            BlockEntity e = getWorld().getBlockEntity(belowPos);
            if (e instanceof Inventory inventory) {
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack slotStack = inventory.getStack(i);
                    if (slotStack.isEmpty()) {
                        inventory.setStack(i, stack);
                        discard();
                        break;
                    }

                    if (ItemStack.canCombine(slotStack, stack) && slotStack.getCount() < slotStack.getMaxCount()) {
                        slotStack.increment(1);
                        discard();
                        break;
                    }
                }
            }
        }

        if (getVelocity().length() > 0.2F) {
            boolean ownerCanModify = !getWorld().isClient && Caster.of(getOwner()).filter(pony -> pony.canModifyAt(hit.getBlockPos())).isPresent();

            if (ownerCanModify && getWorld().getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                if ((!isBouncy() || getWorld().random.nextInt(200) == 0) && state.isIn(UTags.FRAGILE)) {
                    getWorld().breakBlock(hit.getBlockPos(), true);
                }
            }

            if (isBouncy()) {
                Direction.Axis side = hit.getSide().getAxis();

                double randomisation = ((getWorld().random.nextFloat() - 0.5F) / 5);

                double inflectionAmount = randomisation + -0.4;
                double deflectionAmount = randomisation + 0.3;

                if (side == Direction.Axis.X) {
                    setVelocity(getVelocity().multiply(inflectionAmount, deflectionAmount, deflectionAmount));
                }

                if (side == Direction.Axis.Y) {
                    setVelocity(getVelocity().multiply(deflectionAmount, inflectionAmount, deflectionAmount));
                }

                if (side == Direction.Axis.Z) {
                    setVelocity(getVelocity().multiply(deflectionAmount, deflectionAmount, inflectionAmount));
                }

                addVelocity(
                        ((getWorld().random.nextFloat() - 0.5F) / 5),
                        ((getWorld().random.nextFloat() - 0.5F) / 5),
                        ((getWorld().random.nextFloat() - 0.5F) / 5)
                );
            } else {
                super.onBlockHit(hit);
            }
        } else {
            super.onBlockHit(hit);
        }

        setSound(state.getSoundGroup().getStepSound());
        getWorld().playSoundFromEntity(null, this, state.getSoundGroup().getStepSound(), SoundCategory.BLOCKS, 1, 1);
        emitGameEvent(GameEvent.STEP);
    }

    @Override
    protected SoundEvent getHitSound() {
        return isBouncy() ? USounds.ITEM_MUFFIN_BOUNCE.value() : USounds.ITEM_ROCK_LAND;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        ItemStack stack = getStack();
        if (!stack.isEmpty()) {
            nbt.put("Item", stack.writeNbt(new NbtCompound()));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setStack(ItemStack.fromNbt(nbt.getCompound("Item")));
    }
}

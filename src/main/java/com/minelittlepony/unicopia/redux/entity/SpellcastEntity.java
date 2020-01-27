package com.minelittlepony.unicopia.redux.entity;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.minelittlepony.unicopia.core.EquinePredicates;
import com.minelittlepony.unicopia.core.Race;
import com.minelittlepony.unicopia.core.entity.InAnimate;
import com.minelittlepony.unicopia.core.entity.IMagicals;
import com.minelittlepony.unicopia.core.magic.Affinity;
import com.minelittlepony.unicopia.core.magic.ICaster;
import com.minelittlepony.unicopia.core.magic.IMagicEffect;
import com.minelittlepony.unicopia.core.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.core.network.EffectSync;
import com.minelittlepony.unicopia.redux.item.UItems;
import com.minelittlepony.unicopia.redux.magic.ICastable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;

public class SpellcastEntity extends MobEntityWithAi implements IMagicals, ICaster<LivingEntity>, InAnimate {

    private LivingEntity owner = null;

    public float hoverStart;

    private static final TrackedData<Integer> LEVEL = DataTracker.registerData(SpellcastEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final TrackedData<Optional<UUID>> OWNER = DataTracker.registerData(SpellcastEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    private static final TrackedData<CompoundTag> EFFECT = DataTracker.registerData(SpellcastEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);

    private static final TrackedData<Integer> AFFINITY = DataTracker.registerData(SpellcastEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);

    public SpellcastEntity(EntityType<SpellcastEntity> type, World w) {
        super(type, w);
        hoverStart = (float)(Math.random() * Math.PI * 2.0D);
        setPersistent();
    }

    public GoalSelector getGoals() {
        return goalSelector;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean shouldRenderAtDistance(double distance) {
        if (getCurrentLevel() > 0) {
            distance /= getCurrentLevel();
        }
        if (distance > 0) {
            distance--;
        }
        return super.shouldRenderAtDistance(distance);
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.values()[dataTracker.get(AFFINITY)];
    }

    public void setAffinity(Affinity affinity) {
        dataTracker.set(AFFINITY, affinity.ordinal());
    }

    @Override
    public void setEffect(@Nullable IMagicEffect effect) {
        effectDelegate.set(effect);

        if (effect != null) {
            effect.onPlaced(this);
        }
    }

    @Override
    public boolean canInteract(Race race) {
        return race.canCast();
    }

    @Nullable
    @Override
    public <T extends IMagicEffect> T getEffect(@Nullable Class<T> type, boolean update) {
        return effectDelegate.get(type, update);
    }

    @Override
    public boolean hasEffect() {
        return effectDelegate.has();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(LEVEL, 0);
        dataTracker.startTracking(EFFECT, new CompoundTag());
        dataTracker.startTracking(OWNER, Optional.empty());
        dataTracker.startTracking(AFFINITY, Affinity.NEUTRAL.ordinal());
    }

    // TODO:
    /*@Override
    public ItemStack getPickedStack(HitResult target) {
        return SpellRegistry.instance().enchantStack(new ItemStack(getItem()), getEffect().getName());
    }*/

    protected Item getItem() {
        return getAffinity() == Affinity.BAD ? UItems.curse : UItems.spell;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean doesRenderOnFire() {
        return false;
    }

    @Override
    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        setOwner(owner.getUuid());
    }

    protected void setOwner(UUID ownerId) {
        dataTracker.set(OWNER, Optional.ofNullable(ownerId));
    }

    protected String getOwnerName() {
        LivingEntity owner = getOwner();

        if (owner != null) {
            return owner.getEntityName();
        }

        return "";
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        if (owner == null) {
            owner = dataTracker.get(OWNER).map(world::getPlayerByUuid).orElse(null);
        }

        return owner;
    }

    protected void displayTick() {
        if (hasEffect()) {
            getEffect().render(this);
        }
    }

    @Override
    public void tickMovement() {
        super.mobTick();
        if (world.isClient) {
            displayTick();
        }

        if (!hasEffect()) {
            remove();
        } else {
            if (getEffect().isDead()) {
                remove();
                onDeath();
            } else {
                getEffect().update(this);
            }

            if (getEffect().allowAI()) {
                dimensions.height = 1.5F;
                super.tickMovement();
            }
        }

        if (overLevelCap()) {
            if (world.random.nextInt(10) == 0) {
                playSpawnEffects();
            }

            if (!world.isClient && hasEffect()) {
                float exhaustionChance = getEffect().getExhaustion(this);

                if (exhaustionChance == 0 || world.random.nextInt((int)(exhaustionChance / 500)) == 0) {
                    addLevels(-1);
                } else if (world.random.nextInt((int)(exhaustionChance * 500)) == 0) {
                    setEffect(null);
                } else if (world.random.nextInt((int)(exhaustionChance * 3500)) == 0) {
                    world.createExplosion(this, x, y, z, getCurrentLevel()/2, DestructionType.BREAK);
                    remove();
                }
            }
        }

        if (getCurrentLevel() < 0) {
            remove();
        }
    }

    public boolean overLevelCap() {
        return getCurrentLevel() > getMaxLevel();
    }

    @Override
    protected void fall(double y, boolean onGround, BlockState state, BlockPos pos) {
        this.onGround = true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!world.isClient) {
            remove();
            onDeath();
        }
        return false;
    }

    protected void onDeath() {
        BlockSoundGroup sound = BlockSoundGroup.STONE;

        world.playSound(x, y, z, sound.getBreakSound(), SoundCategory.NEUTRAL, sound.getVolume(), sound.getPitch(), true);

        if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
            int level = getCurrentLevel();

            ItemStack stack = new ItemStack(getItem(), level + 1);
            if (hasEffect()) {
                SpellRegistry.instance().enchantStack(stack, getEffect().getName());
            }

            dropStack(stack, 0);
        }
    }

    @Override
    public void remove() {
        if (hasEffect()) {
            getEffect().setDead();
        }
        super.remove();
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d vec, Hand hand) {
        if (EquinePredicates.MAGI.test(player)) {
            ItemStack currentItem = player.getStackInHand(Hand.MAIN_HAND);

            if (currentItem != null
                    && currentItem.getItem() instanceof ICastable
                    && ((ICastable)currentItem.getItem()).canFeed(this, currentItem)
                    && tryLevelUp(currentItem)) {

                if (!player.abilities.creativeMode) {
                    currentItem.decrement(1);

                    if (currentItem.isEmpty()) {
                        player.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
                    }
                }

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.FAIL;
    }

    public boolean tryLevelUp(ItemStack stack) {
        if (hasEffect() && SpellRegistry.stackHasEnchantment(stack)) {
            if (!getEffect().getName().contentEquals(SpellRegistry.getKeyFromStack(stack))) {
                return false;
            }

            addLevels(1);

            playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 0.1f, 1);

            return true;
        }

        return false;
    }

    @Override
    public int getMaxLevel() {
        return hasEffect() ? getEffect().getMaxLevelCutOff(this) : 0;
    }

    @Override
    public int getCurrentLevel() {
        return dataTracker.get(LEVEL);
    }

    @Override
    public void setCurrentLevel(int level) {
        dataTracker.set(LEVEL, Math.max(level, 0));
    }

    @Override
    public Entity getEntity() {
        return this;
    }

    @Override
    public void readCustomDataFromTag(CompoundTag compound) {
        super.readCustomDataFromTag(compound);
        if (compound.containsKey("affinity")) {
            setAffinity(Affinity.of(compound.getString("affinity")));
        }

        setOwner(compound.getUuid("owner"));
        setCurrentLevel(compound.getInt("level"));

        if (compound.containsKey("effect")) {
            setEffect(SpellRegistry.instance().createEffectFromNBT(compound.getCompound("effect")));
        }
    }

    @Override
    public void writeCustomDataToTag(CompoundTag compound) {
        super.writeCustomDataToTag(compound);

        compound.putString("affinity", getAffinity().name());
        compound.putString("owner", getOwnerName());
        compound.putInt("level", getCurrentLevel());

        if (hasEffect()) {
            compound.put("effect", SpellRegistry.instance().serializeEffectToNBT(getEffect()));
        }
    }
}
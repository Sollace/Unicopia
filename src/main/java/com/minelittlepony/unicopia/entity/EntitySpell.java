package com.minelittlepony.unicopia.entity;

import org.apache.commons.lang3.StringUtils;

import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.UItems;
import com.minelittlepony.unicopia.item.ICastable;
import com.minelittlepony.unicopia.network.EffectSync;
import com.minelittlepony.unicopia.spell.ICaster;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntitySpell extends EntityLiving implements IMagicals, ICaster<EntityLivingBase> {

	private EntityLivingBase owner = null;

	public float hoverStart;

	private static final DataParameter<Integer> LEVEL = EntityDataManager
	        .createKey(EntitySpell.class, DataSerializers.VARINT);

	private static final DataParameter<String> OWNER = EntityDataManager
	        .createKey(EntitySpell.class, DataSerializers.STRING);

	private static final DataParameter<NBTTagCompound> EFFECT = EntityDataManager
	        .createKey(EntitySpell.class, DataSerializers.COMPOUND_TAG);

	private final EffectSync<EntityLivingBase> effectDelegate = new EffectSync<>(this, EFFECT);

	public EntitySpell(World w) {
		super(w);
		setSize(0.6f, 0.25f);
		hoverStart = (float)(Math.random() * Math.PI * 2.0D);
		setRenderDistanceWeight(getRenderDistanceWeight() + 1);
		preventEntitySpawning = false;
		enablePersistence();
	}

	@Override
	public boolean isInRangeToRenderDist(double distance) {
	    if (getCurrentLevel() > 0) {
	        distance /= getCurrentLevel();
	    }
		return super.isInRangeToRenderDist(distance);
    }

	@Override
	public void setEffect(IMagicEffect effect) {
	    effectDelegate.set(effect);
	}

	@Override
	public IMagicEffect getEffect() {
	    return effectDelegate.get();
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(LEVEL, 0);
		dataManager.register(EFFECT, new NBTTagCompound());
		dataManager.register(OWNER, "");
	}

	public ItemStack onPlayerMiddleClick(EntityPlayer player) {
	    ItemStack stack = new ItemStack(UItems.spell, 1);
	    SpellRegistry.instance().enchantStack(stack, getEffect().getName());
		return stack;
	}

	@Override
    protected boolean canTriggerWalking() {
	    return false;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
	public void setOwner(EntityLivingBase owner) {
		this.owner = owner;
		setOwner(owner.getName());
	}

	protected void setOwner(String ownerName) {
		if (!StringUtils.isEmpty(ownerName)) {
			dataManager.set(OWNER, ownerName);
		}
	}

	protected String getOwnerName() {
		String ownerName = dataManager.get(OWNER);

		if (!StringUtils.isEmpty(ownerName)) {
			if (owner instanceof EntityPlayer) {
				return owner.getName();
			}

			return "";
        }

        return ownerName;
	}

	@Override
	public EntityLivingBase getOwner() {
        if (owner == null) {
        	String ownerName = dataManager.get(OWNER);
        	if (ownerName != null && ownerName.length() > 0) {
        		owner = world.getPlayerEntityByName(ownerName);
        	}
        }

        return owner;
    }

	protected void displayTick() {
		if (hasEffect()) {
		    getEffect().render(this, getCurrentLevel());
		}
	}

	@Override
	public void onUpdate() {
		if (world.isRemote) {
			displayTick();
		}

		if (getEffect() == null) {
			setDead();
		} else {
			if (getEffect().getDead()) {
				setDead();
				onDeath();
			} else {
			    getEffect().update(this, getCurrentLevel());
			}

			if (getEffect().allowAI()) {
				super.onUpdate();
			}
		}

		if (getCurrentLevel() > 0 && !world.isRemote && world.rand.nextInt(200) == 0) {
		    addLevels(-1);
		    if (getCurrentLevel() <= 0) {
		        setDead();
		    }
		}

		if (overLevelCap()) {
		    if (world.rand.nextInt(10) == 0) {
		        spawnExplosionParticle();
		    }
		}
	}

	@Override
	public void fall(float distance, float damageMultiplier) {

	}

	@Override
	protected void updateFallState(double y, boolean onGround, IBlockState state, BlockPos pos) {
	    this.onGround = true;
    	//super.updateFallState(y, onGround = this.onGround = true, state, pos);
    }

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (!world.isRemote) {
			setDead();
			onDeath();
		}
		return false;
	}

	protected void onDeath() {
		SoundType sound = SoundType.STONE;

		world.playSound(posX, posY, posZ, sound.getBreakSound(), SoundCategory.NEUTRAL, sound.getVolume(), sound.getPitch(), true);

		if (world.getGameRules().getBoolean("doTileDrops")) {
			int level = getCurrentLevel();

			ItemStack stack = new ItemStack(UItems.spell, level + 1);
			if (hasEffect()) {
			    SpellRegistry.instance().enchantStack(stack, getEffect().getName());
			}

			entityDropItem(stack, 0);
		}
	}

	@Override
	public void setDead() {
		if (hasEffect()) {
			getEffect().setDead();
		}
		super.setDead();
	}

	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
		if (Predicates.MAGI.test(player)) {
			ItemStack currentItem = player.getHeldItem(EnumHand.MAIN_HAND);

			if (currentItem != null
			        && currentItem.getItem() instanceof ICastable
			        && ((ICastable)currentItem.getItem()).canFeed(this, currentItem)
			        && tryLevelUp(currentItem)) {

				if (!player.capabilities.isCreativeMode) {
					currentItem.shrink(1);

					if (currentItem.isEmpty()) {
						player.renderBrokenItemStack(currentItem);
					}
				}

				return EnumActionResult.SUCCESS;
			}
		}

		return EnumActionResult.FAIL;
	}

    public boolean tryLevelUp(ItemStack stack) {
        if (SpellRegistry.stackHasEnchantment(stack)) {
            if (!getEffect().getName().equals(SpellRegistry.getKeyFromStack(stack))) {
                return false;
            }

            addLevels(1);

            if (!world.isRemote) {
                if (overLevelCap() || (rand.nextFloat() * getCurrentLevel()) > 10) {
                    world.createExplosion(this, posX, posY, posZ, getCurrentLevel()/2, true);
                    setDead();
                    return false;
                }
            }

            playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 0.1f, 1);

            return true;
        }

        return false;
    }

	@Override
	public int getMaxLevel() {
	    return hasEffect() ? getEffect().getMaxLevel() : 0;
	}

	@Override
    public int getCurrentLevel() {
        return dataManager.get(LEVEL);
    }

	@Override
    public void setCurrentLevel(int level) {
        level = Math.max(level, 0);
        if (hasEffect()) {
            getEffect().setCurrentLevel(level);
            level = getEffect().getCurrentLevel();
        }

        dataManager.set(LEVEL, level);
    }

	public boolean overLevelCap() {
		int max = getMaxLevel();
		return max > 0 && getCurrentLevel() >= (max * 1.1);
	}

	@Override
	public Entity getEntity() {
	    return this;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setOwner(compound.getString("ownerName"));
        setCurrentLevel(compound.getInteger("level"));

		if (compound.hasKey("effect")) {
		    setEffect(SpellRegistry.instance().createEffectFromNBT(compound.getCompoundTag("effect")));
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);

        compound.setString("ownerName", getOwnerName());
        compound.setInteger("level", getCurrentLevel());

        if (hasEffect()) {
			compound.setTag("effect", SpellRegistry.instance().serializeEffectToNBT(getEffect()));
        }
	}
}
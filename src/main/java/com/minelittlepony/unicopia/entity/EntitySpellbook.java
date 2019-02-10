package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.UItems;
import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.block.SoundType;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntitySpellbook extends EntityLiving implements IMagicals {

    private static final DataParameter<Boolean> OPENED = EntityDataManager.createKey(EntitySpellbook.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> OPENED_USER = EntityDataManager.createKey(EntitySpellbook.class, DataSerializers.BYTE);

    public EntitySpellbook(World worldIn) {
        super(worldIn);
        setSize(0.6f, 0.6f);
        enablePersistence();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(OPENED, true);
        dataManager.register(OPENED_USER, (byte)1);
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

    public boolean getIsOpen() {
        return dataManager.get(OPENED);
    }

    public Boolean getUserSetState() {
        byte state = dataManager.get(OPENED_USER);
        return state == 1 ? null : state == 2;
    }

    public void setIsOpen(boolean val) {
        dataManager.set(OPENED, val);
    }

    public void setUserSetState(Boolean val) {
        dataManager.set(OPENED_USER, val == null ? (byte)1 : val == true ? (byte)2 : (byte)0);
    }

    @Override
    public void onUpdate() {
        boolean open = getIsOpen();
        this.isJumping = open && isInWater();
        super.onUpdate();
        if (open && world.isRemote) {

            for (int offX = -2; offX <= 1; ++offX) {
                for (int offZ = -2; offZ <= 1; ++offZ) {
                    if (offX > -1 && offX < 1 && offZ == -1) {
                        offZ = 1;
                    }

                    if (rand.nextInt(320) == 0) {
                        for (int offY = 0; offY <= 1; ++offY) {
                            world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE,
                                    posX, posY, posZ,
                                    offX/2F + rand.nextFloat(),
                                    offY/2F - rand.nextFloat() + 0.5f,
                                    offZ/2F + rand.nextFloat()
                            );
                        }
                    }
                }
            }
        }

        if (world.rand.nextInt(30) == 0) {
            float celest = world.getCelestialAngle(1) * 4;

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
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!world.isRemote) {
            setDead();

            SoundType sound = SoundType.WOOD;

            world.playSound(posX, posY, posZ, sound.getBreakSound(), SoundCategory.BLOCKS, sound.getVolume(), sound.getPitch(), true);

            if (world.getGameRules().getBoolean("doTileDrops")) {
                entityDropItem(new ItemStack(UItems.spellbook), 0);
            }
        }
        return false;
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (player.isSneaking()) {
            boolean open = !getIsOpen();

            setIsOpen(open);
            setUserSetState(open);

            return EnumActionResult.SUCCESS;
        }

        if (Predicates.MAGI.test(player)) {

            player.playSound(SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, 2, 1);

            player.openGui(Unicopia.MODID, 0, world, (int)posX, (int)posY, (int)posZ);

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        setIsOpen(compound.getBoolean("open"));
        setUserSetState(compound.hasKey("force_open") ? compound.getBoolean("force_open") : null);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("open", getIsOpen());

        Boolean state = getUserSetState();

        if (state != null) {
            compound.setBoolean("force_open", state);
        }
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(UItems.spellbook);
    }
}

package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.spell.IUseAction;
import com.minelittlepony.unicopia.spell.SpellCastResult;
import com.minelittlepony.unicopia.spell.IDispenceable;
import com.minelittlepony.unicopia.spell.SpellRegistry;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class ItemSpell extends Item implements ICastable {
	private static final IBehaviorDispenseItem dispenserBehavior = new BehaviorDefaultDispenseItem() {
	    @Override
		protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {

			IDispenceable effect = SpellRegistry.instance().getDispenseActionFrom(stack);

			if (effect == null) {
			    return super.dispenseStack(source, stack);
			}

			SpellCastResult dispenceResult = ((ICastable)stack.getItem()).onDispenseSpell(source, stack, effect);

            if (dispenceResult == SpellCastResult.DEFAULT) {
                return super.dispenseStack(source, stack);
            }

            if (dispenceResult == SpellCastResult.PLACE) {
                BlockPos pos = source.getBlockPos();

                castContainedSpell(source.getWorld(), pos, stack, effect);

                stack.shrink(1);
            }

			return stack;
		}
	};

	public ItemSpell(String domain, String name) {
		super();

		setHasSubtypes(true);
		setMaxDamage(0);
		setTranslationKey(name);
		setRegistryName(domain, name);
		setMaxStackSize(16);

        setCreativeTab(CreativeTabs.BREWING);

        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, dispenserBehavior);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return SpellRegistry.stackHasEnchantment(stack);
	}

    @Override
    public SpellCastResult onDispenseSpell(IBlockSource source, ItemStack stack, IDispenceable effect) {
        EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
        BlockPos pos = source.getBlockPos().offset(facing);

        return effect.onDispenced(pos, facing, source);
    }

    @Override
    public SpellCastResult onCastSpell(EntityPlayer player, World world, BlockPos pos, ItemStack stack, IMagicEffect effect, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (effect instanceof IUseAction) {
            return ((IUseAction)effect).onUse(stack, player, world, pos, side, hitX, hitY, hitZ);
        }

        return SpellCastResult.PLACE;
    }

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

	    if (hand != EnumHand.MAIN_HAND || !Predicates.MAGI.test(player)) {
	        return EnumActionResult.PASS;
	    }

	    ItemStack stack = player.getHeldItem(hand);

		if (!SpellRegistry.stackHasEnchantment(stack)) {
		    return EnumActionResult.FAIL;
		}

	    IMagicEffect effect = SpellRegistry.instance().getSpellFromItemStack(stack);

        if (effect == null) {
            return EnumActionResult.FAIL;
        }

        SpellCastResult result = ((ICastable)stack.getItem()).onCastSpell(player, world, pos, stack, effect, side, hitX, hitY, hitZ);

        if (!world.isRemote) {
            pos = pos.offset(side);

            if (result == SpellCastResult.PLACE) {
                castContainedSpell(world, pos, stack, effect).setOwner(player);
            }
        }

        if (result != SpellCastResult.NONE) {
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        }

    	return EnumActionResult.FAIL;
    }

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		Entity target = VecHelper.getLookedAtEntity(player, 5);

		ItemStack stack = player.getHeldItem(hand);

		if (target == null) {
		    return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
		}

	    if (!SpellRegistry.stackHasEnchantment(stack)) {
	        return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
	    }

        IMagicEffect effect = SpellRegistry.instance().getSpellFromItemStack(stack);

        if (effect instanceof IUseAction) {
            SpellCastResult result = ((IUseAction)effect).onUse(stack, player, world, target);

            if (result != SpellCastResult.NONE) {
                if (result == SpellCastResult.PLACE && !player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }

                return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
            }
        }

        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		super.getSubItems(tab, subItems);

		if (isInCreativeTab(tab)) {
    		for (String name : SpellRegistry.instance().getAllNames()) {
    			subItems.add(SpellRegistry.instance().enchantStack(new ItemStack(this, 1), name));
    		}
		}
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		String result = super.getTranslationKey(stack);

		if (SpellRegistry.stackHasEnchantment(stack)) {
    		result += "." + stack.getTagCompound().getString("spell");
		}

		return result;
	}

	protected static EntitySpell castContainedSpell(World world, BlockPos pos, ItemStack stack, IMagicEffect effect) {
		EntitySpell spell = new EntitySpell(world);

        spell.setEffect(effect);
		spell.setLocationAndAngles(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0);
    	world.spawnEntity(spell);

    	return spell;
	}

    @Override
    public boolean canFeed(EntitySpell entity, ItemStack stack) {

        IMagicEffect effect = entity.getEffect();

        return effect != null && effect.getName().equals(SpellRegistry.getKeyFromStack(stack));
    }
}

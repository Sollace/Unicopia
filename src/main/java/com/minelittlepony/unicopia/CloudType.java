package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.entity.EntityCloud;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IStringSerializable;

public enum CloudType implements IStringSerializable {
    NORMAL("NORMAL", 0, "normal"),
    PACKED("PACKED", 1, "packed"),
    ENCHANTED("ENCHANTED", 2, "enchanted");

    private String name;
    private int meta;
    private String unlocalizedName;

    private static final CloudType[] META_LOOKUP = new CloudType[values().length];
    private static final String[] NAMES = new String[values().length];

    static {
        for (CloudType i : values()) {
            META_LOOKUP[i.getMetadata()] = i;
            NAMES[i.ordinal()] = i.getTranslationKey();
        }
    }

    CloudType(String name, int meta, String unlocalised) {
        this.name = name;
        this.meta = meta;
        this.unlocalizedName = unlocalised;
    }

    public static CloudType byMetadata(int meta) {
        if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
        return META_LOOKUP[meta];
    }


    public static String[] getVariants(String suffex) {
        String[] result = new String[NAMES.length];

        for (int i = 0; i < NAMES.length; i++) {
            result[i] = NAMES[i] + suffex;
        }

        return result;
    }


    public String toString() {
        return name;
    }

    public String getName() {
        return unlocalizedName;
    }

    public String getTranslationKey() {
        return unlocalizedName;
    }

    public int getMetadata() {
        return meta;
    }

    public boolean canInteract(Entity e) {
        if (e == null) {
            return false;
        }

        if (this == ENCHANTED) {
            return true;
        }

        if (e instanceof EntityPlayer) {

            if (this == PACKED) {
                return true;
            }

            return Predicates.INTERACT_WITH_CLOUDS.test((EntityPlayer)e)
                || EntityCloud.getFeatherEnchantStrength((EntityPlayer)e) > 0;
        }

        if (e instanceof EntityItem) {
            return Predicates.ITEM_INTERACT_WITH_CLOUDS.test((EntityItem)e);
        }

        if (e instanceof EntityCloud && e.isRiding()) {
            return canInteract(e.getRidingEntity());
        }

        return false;
    }
}
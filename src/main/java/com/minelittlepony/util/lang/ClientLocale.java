package com.minelittlepony.util.lang;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Localisation class for client-side translations.
 * Only use this in client code. Servers should use ServerLocale.
 */
@SideOnly(Side.CLIENT)
public class ClientLocale {
    /**
     * Translates the given string and then formats it. Equivalent to String.format(translate(key), parameters).
     */
    public static String format(String translateKey, Object... parameters) {
        return I18n.format(translateKey, parameters);
    }

    /**
     * Determines if a language mapping exists in the current locale for the requested key.
     */
    public static boolean hasKey(String key) {
        return I18n.hasKey(key);
    }
}

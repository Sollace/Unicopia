package com.minelittlepony.util.lang;

import com.minelittlepony.unicopia.forgebullshit.FUF;

import net.minecraft.util.text.translation.I18n;

/**
 * Localisation class for server-side translations.
 * Use this in a server context, otherwise use ClientLocale.
 */
@FUF(reason = "Don't deprecate classes we actually need to use")
@SuppressWarnings("deprecation")
public class ServerLocale {
    /**
     * Translates a Stat name with format args
     */
    public static String format(String key, Object... format) {
        return I18n.translateToLocalFormatted(key, format);
    }

    /**
     * Determines whether or not translateToLocal will find a translation for the given key.
     */
    public static boolean hasKey(String key) {
        return I18n.canTranslate(key);
    }
}
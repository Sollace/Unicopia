package com.minelittlepony.unicopia.datafixer;

import java.util.Set;

import com.mojang.serialization.Dynamic;

import net.minecraft.datafixer.fix.ItemStackComponentizationFix;

final class UnicopiaItemStackComponentizations {
    private static final Set<String> ENCHANTED_ITEMS = Set.of(
            "unicopia:gemstone",
            "unicopia:magic_staff"
    );

    static void run(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic) {
        if (data.itemEquals("unicopia:friendship_bracelet")) {
            data.moveToComponent("glowing", "unicopia:glowing");
            data.getAndRemove("issuer_id").result().ifPresent(id -> {
                data.setComponent("unicopia:issuer", dynamic.emptyMap()
                        .set("id", id)
                        .setFieldIfPresent("name", data.getAndRemove("issuer").result()));
            });
        }
        if (data.itemEquals("unicopia:grogars_bell")) {
            fixEnergy(data, dynamic, 1000, 1000);
        }
        if (data.itemEquals("unicopia:magic_staff")) {
            fixEnergy(data, dynamic, 3, 3);
        }
        if (data.itemEquals("unicopia:pegasus_amulet")) {
            fixEnergy(data, dynamic, 900, 450);
        }
        if (data.itemEquals("unicopia:zap_apple")) {
            fixAppearance(data, dynamic, true);
        }
        if (data.itemEquals("unicopia:filled_jar")) {
            fixAppearance(data, dynamic, false);
        }
        if (data.itemMatches(ENCHANTED_ITEMS)) {
            data.moveToComponent("spell", "unicopia:stored_spell");
            data.moveToComponent("spell_traits", "unicopia:spell_traits");
        }
        if (data.itemEquals("unicopia:butterfly")) {
            data.moveToComponent("variant", "unicopia:butterfly_variant");
        }
        if (data.itemEquals("unicopia:giant_balloon")) {
            data.moveToComponent("design", "unicopia:balloon_design");
        }
        if (data.itemEquals("unicopia:spellbook")) {
            data.moveToComponent("spellbookState", "unicopia:spellbook_state");
        }
    }

    private static void fixAppearance(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, boolean replaceFully) {
        data.getAndRemove("appearance").result().ifPresent(appearance -> {
            data.setComponent("unicopia:appearance", dynamic.emptyMap()
                    .set("item", appearance)
                    .set("replace_fully", dynamic.createBoolean(replaceFully)));
        });
    }

    private static void fixEnergy(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, int maximum, int baseline) {
        data.getAndRemove("energy").getElement("energy").result().ifPresent(energy -> {
            data.setComponent("unicopia:charges", dynamic.emptyMap()
                    .set("energy", dynamic.createInt((int)energy))
                    .set("maximum", dynamic.createInt(maximum))
                    .set("baseline", dynamic.createInt(baseline))
                    .set("show_in_tooltip", dynamic.createBoolean(true)));
        });
    }
}

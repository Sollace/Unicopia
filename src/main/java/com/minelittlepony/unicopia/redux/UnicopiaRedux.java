package com.minelittlepony.unicopia.redux;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;

import com.minelittlepony.unicopia.core.ability.PowersRegistry;
import com.minelittlepony.unicopia.core.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.redux.UContainers;
import com.minelittlepony.unicopia.redux.ability.PowerCloudBase;
import com.minelittlepony.unicopia.redux.ability.PowerDisguise;
import com.minelittlepony.unicopia.redux.ability.PowerEngulf;
import com.minelittlepony.unicopia.redux.ability.PowerStomp;
import com.minelittlepony.unicopia.redux.command.DisguiseCommand;
import com.minelittlepony.unicopia.redux.enchanting.Pages;
import com.minelittlepony.unicopia.redux.item.UItems;
import com.minelittlepony.unicopia.redux.magic.spells.AwkwardSpell;
import com.minelittlepony.unicopia.redux.magic.spells.ChangelingTrapSpell;
import com.minelittlepony.unicopia.redux.magic.spells.ChargingSpell;
import com.minelittlepony.unicopia.redux.magic.spells.DarknessSpell;
import com.minelittlepony.unicopia.redux.magic.spells.DisguiseSpell;
import com.minelittlepony.unicopia.redux.magic.spells.FaithfulAssistantSpell;
import com.minelittlepony.unicopia.redux.magic.spells.FlameSpell;
import com.minelittlepony.unicopia.redux.magic.spells.GlowingSpell;
import com.minelittlepony.unicopia.redux.magic.spells.IceSpell;
import com.minelittlepony.unicopia.redux.magic.spells.InfernoSpell;
import com.minelittlepony.unicopia.redux.magic.spells.PortalSpell;
import com.minelittlepony.unicopia.redux.magic.spells.RevealingSpell;
import com.minelittlepony.unicopia.redux.magic.spells.ScorchSpell;
import com.minelittlepony.unicopia.redux.structure.UStructures;

public class UnicopiaRedux implements ModInitializer {
    @Override
    public void onInitialize() {
        Pages.instance().load();

        UBlocks.bootstrap();
        UItems.bootstrap();
        UContainers.bootstrap();
        UStructures.bootstrap();

        CommandRegistry.INSTANCE.register(false, DisguiseCommand::register);

        PowersRegistry.instance().registerPower(new PowerCloudBase());
        PowersRegistry.instance().registerPower(new PowerEngulf());
        PowersRegistry.instance().registerPower(new PowerStomp());
        PowersRegistry.instance().registerPower(new PowerDisguise());

        SpellRegistry.instance().registerSpell(ChargingSpell::new);
        SpellRegistry.instance().registerSpell(IceSpell::new);
        SpellRegistry.instance().registerSpell(PortalSpell::new);
        SpellRegistry.instance().registerSpell(AwkwardSpell::new);
        SpellRegistry.instance().registerSpell(InfernoSpell::new);
        SpellRegistry.instance().registerSpell(FaithfulAssistantSpell::new);
        SpellRegistry.instance().registerSpell(RevealingSpell::new);
        SpellRegistry.instance().registerSpell(DarknessSpell::new);
        SpellRegistry.instance().registerSpell(FlameSpell::new);
        SpellRegistry.instance().registerSpell(GlowingSpell::new);
        SpellRegistry.instance().registerSpell(ChangelingTrapSpell::new);
        SpellRegistry.instance().registerSpell(ScorchSpell::new);
        SpellRegistry.instance().registerSpell(DisguiseSpell::new);
    }
}

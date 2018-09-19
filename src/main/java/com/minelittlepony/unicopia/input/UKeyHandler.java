package com.minelittlepony.unicopia.input;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;
import com.minelittlepony.unicopia.power.PowersRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

class UKeyHandler implements IKeyHandler {
	private static ArrayList<KeyBinding> bindings = new ArrayList<KeyBinding>();
	private static ArrayList<KeyBinding> removed = new ArrayList<KeyBinding>();

	private static ArrayList<KeyBinding> pressed = new ArrayList<KeyBinding>();

	@Override
	public void addKeybind(IKeyBind p) {
		KeyBinding b = new KeyBinding(p.getKeyName(), p.getKeyCode(), p.getKeyCategory());

		ClientRegistry.registerKeyBinding(b);

		bindings.add(b);
	}

	@Override
	public void onKeyInput() {
		if (Minecraft.getMinecraft().currentScreen != null
	        || Minecraft.getMinecraft().player == null) {
		    return;
		}
		IPlayer iplayer = PlayerSpeciesList.instance().getPlayer(Minecraft.getMinecraft().player);

		for (KeyBinding i : bindings) {
			if (Keyboard.isKeyDown(i.getKeyCode())) {

				if (!pressed.contains(i)) {
				    pressed.add(i);
				}

				if (!PowersRegistry.instance().hasRegisteredPower(i.getKeyCodeDefault())) {
					removed.add(i);
					System.out.println("Error: Keybinding(" + i.getKeyDescription() + ") does not have a registered pony power. Keybinding will be removed from event.");
				} else {
				    PowersRegistry.instance()
				        .getCapablePowerFromKey(i.getKeyCodeDefault(), iplayer.getPlayerSpecies())
				        .ifPresent(iplayer.getAbilities()::tryUseAbility);
				}
			} else {
				if (pressed.contains(i)) {
					pressed.remove(i);

					iplayer.getAbilities().tryClearAbility();
				}
			}
		}

		for (KeyBinding i : removed) {
		    removed.remove(i);
			bindings.remove(i);
			pressed.remove(i);
		}
	}
}

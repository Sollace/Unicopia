package com.minelittlepony.unicopia.command;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

class CommandSpecies extends CommandBase {

	public String getName() {
		return "race";
	}

    public int getRequiredPermissionLevel() {
        return 0;
    }

    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(getRequiredPermissionLevel(), "help");
    }

    private String getRacesString() {
		String values = "";
		for (Race i : Race.values()) {
			if (PlayerSpeciesList.instance().speciesPermitted(i)) {
				if (values != "") values += ", ";
				values += i.toString();
			}
		}
		return values;
    }

	public String getUsage(ICommandSender sender) {
		return "commands.race.usage";
	}

	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1) {
			throw new WrongUsageException(getUsage(sender));
		}

		EntityPlayerMP player;
		int playerIndex = 1;

		if (args[0].contentEquals("set")) playerIndex++;

		if (args.length > playerIndex) {
			player = getPlayer(server, sender, args[playerIndex]);
		} else {
			player = getCommandSenderAsPlayer(sender);
		}

		if (args[0].contentEquals("set")) {
			if (args.length >= 2) {
				Race species = Race.fromName(args[1]);

				if (species == null) {
					player.sendMessage(new TextComponentTranslation("commands.race.fail", args[1].toUpperCase()));
				} else {
					if (PlayerSpeciesList.instance().speciesPermitted(species)) {
					    PlayerSpeciesList.instance().getPlayer(player).setPlayerSpecies(species);

						TextComponentTranslation formattedName = new TextComponentTranslation(species.name().toLowerCase());

						if (player != sender) {
							notifyCommandListener(sender, this, 1, "commands.race.success.other", player.getName(), formattedName);
			            } else {
			            	player.sendMessage(new TextComponentTranslation("commands.race.success.self"));
			            	notifyCommandListener(sender, this, 1, "commands.race.success.otherself", player.getName(), formattedName);
			            }
					} else {
						player.sendMessage(new TextComponentTranslation("commands.race.permission"));
					}
				}
			}
		} else if (args[0].contentEquals("get")) {
			Race spec = PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies();

			String name = "commands.race.tell.";
			name += player == sender ? "self" : "other";

			ITextComponent race = new TextComponentTranslation(spec.getTranslationString());

			TextComponentTranslation message = new TextComponentTranslation(name);

			race.getStyle().setColor(TextFormatting.GOLD);

			message.appendSibling(race);

			player.sendMessage(message);
		} else if (args[0].contentEquals("list")) {
			player.sendMessage(new TextComponentTranslation("commands.race.list"));

			ITextComponent message = new TextComponentString(" " + getRacesString());

			message.getStyle().setColor(TextFormatting.GOLD);

			player.sendMessage(message);
		}
	}

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender par1ICommandSender, String[] args, BlockPos pos) {
    	if (args.length == 1) {
    		return getListOfStringsMatchingLastWord(args, new String[] { "get", "set", "list" });
    	} else if (args.length == 2 && args[0].contentEquals("set")) {
    		ArrayList<String> names = new ArrayList<String>();
    		for (Race i : Race.values()) {
    			if (PlayerSpeciesList.instance().speciesPermitted(i)) {
    				names.add(i.toString());
    			}
    		}
			return getListOfStringsMatchingLastWord(args, names.toArray(new String[names.size()]));
    	} else if ((args.length == 3 && args[0].contentEquals("set")) || (args[0].contentEquals("get") && args.length == 2)) {
    		return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
    	}

        return null;
    }

    public boolean isUsernameIndex(String[] args, int index) {
    	if (args[0].contentEquals("get")) {
			return index == 1;
    	} else if (args[0].contentEquals("set")) {
    		return index == 2;
    	}
        return false;
    }
}

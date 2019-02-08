package com.minelittlepony.unicopia.command;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
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

		if (args[0].contentEquals("set") || args[0].contentEquals("describe")) {
		    playerIndex++;
		}

		if (args.length > playerIndex) {
			player = getPlayer(server, sender, args[playerIndex]);
		} else {
			player = getCommandSenderAsPlayer(sender);
		}

		if (args[0].contentEquals("set") && args.length >= 2) {
			Race species = Race.fromName(args[1], Race.HUMAN);

			if (species.isDefault()) {
			    ITextComponent message = new TextComponentTranslation("commands.race.fail", args[1].toUpperCase());
		        message.getStyle().setColor(TextFormatting.RED);

				player.sendMessage(message);
			} else {
				if (PlayerSpeciesList.instance().speciesPermitted(species, player)) {
				    PlayerSpeciesList.instance().getPlayer(player).setPlayerSpecies(species);

				    ITextComponent formattedName = new TextComponentTranslation(species.name().toLowerCase());

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
		} else if (args[0].contentEquals("get")) {
			Race spec = PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies();

			String name = "commands.race.tell.";
			name += player == sender ? "self" : "other";

			ITextComponent race = new TextComponentTranslation(spec.getTranslationKey());
			ITextComponent message = new TextComponentTranslation(name);

			race.getStyle().setColor(TextFormatting.GOLD);

			message.appendSibling(race);

			player.sendMessage(message);
		} else if (args[0].contentEquals("list")) {
			player.sendMessage(new TextComponentTranslation("commands.race.list"));

			ITextComponent message = new TextComponentString(getRacesString());

			boolean first = true;
			for (Race i : Race.values()) {
	            if (!i.isDefault() && PlayerSpeciesList.instance().speciesPermitted(i, player)) {
	                message.appendSibling(new TextComponentString((!first ? "\n" : "") + " - " + i.name().toLowerCase()));
	                first = false;
	            }
	        }

			message.getStyle().setColor(TextFormatting.GOLD);

			player.sendMessage(message);
		} else if (args[0].contentEquals("describe") && args.length >= 2) {
		    Race species = Race.fromName(args[1], null);

            if (species == null) {
                player.sendMessage(new TextComponentTranslation("commands.race.fail", args[1].toUpperCase()));
            } else {
                String name = species.name().toLowerCase();

                ITextComponent line1 = new TextComponentTranslation(String.format("commands.race.describe.%s.1", name));
                line1.getStyle().setColor(TextFormatting.YELLOW);

                player.sendMessage(line1);

                player.sendMessage(new TextComponentTranslation(String.format("commands.race.describe.%s.2", name)));

                ITextComponent line3 = new TextComponentTranslation(String.format("commands.race.describe.%s.3", name));
                line3.getStyle().setColor(TextFormatting.RED);

                player.sendMessage(line3);
            }
		} else {
            throw new WrongUsageException(getUsage(sender));
        }
	}

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {

    	if (args.length == 1) {
    		return getListOfStringsMatchingLastWord(args, "get", "set", "list", "describe");
    	}

    	if (args.length == 2 && (args[0].contentEquals("set") || args[0].contentEquals("describe"))) {
    		ArrayList<String> names = new ArrayList<String>();

    		EntityPlayer player = sender instanceof EntityPlayer ? (EntityPlayer)sender : null;

    		for (Race i : Race.values()) {
    			if (args[0].contentEquals("describe") || !(i.isDefault() && PlayerSpeciesList.instance().speciesPermitted(i, player))) {
    				names.add(i.name().toLowerCase());
    			}
    		}

			return getListOfStringsMatchingLastWord(args, names.stream().toArray(String[]::new));
    	}

    	if ((args.length == 3 && args[0].contentEquals("set")) || (args[0].contentEquals("get") && args.length == 2)) {
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

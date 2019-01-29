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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

class CommandRacelist extends CommandBase {

	public String getName() {
		return "racelist";
	}

    public int getRequiredPermissionLevel() {
        return 4;
    }

	public String getUsage(ICommandSender sender) {
		return "commands.racelist.usage";
	}

	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException(getUsage(sender));
        }

        EntityPlayer player = getCommandSenderAsPlayer(sender);

        Race race = Race.fromName(args[1], Race.EARTH);

        TextComponentTranslation formattedName = new TextComponentTranslation(race.name().toLowerCase());
        formattedName.getStyle().setColor(TextFormatting.GOLD);

        if (race.isDefault()) {
            player.sendMessage(new TextComponentTranslation("commands.racelist.illegal", formattedName));
        } else if (args[0].contentEquals("allow")) {
            String translationKey = "commands.racelist.allowed";

            if (!PlayerSpeciesList.instance().whiteListRace(race)) {
                translationKey += ".failed";
            }

            if (sender == player) {
                ITextComponent comp = new TextComponentTranslation(translationKey, formattedName);
                comp.getStyle().setColor(TextFormatting.GREEN);
                player.sendMessage(comp);
            }
            notifyCommandListener(sender, this, 1, translationKey + ".other", player.getName(), formattedName);

        } else if (args[0].contentEquals("disallow")) {
            String translationKey = "commands.racelist.disallowed";

            if (!PlayerSpeciesList.instance().unwhiteListRace(race)) {
                translationKey += ".failed";
            }

            if (sender == player) {
                ITextComponent comp = new TextComponentTranslation(translationKey, formattedName);
                comp.getStyle().setColor(TextFormatting.GREEN);
                player.sendMessage(comp);
            }
            notifyCommandListener(sender, this, 1, translationKey + ".other", player.getName(), formattedName);
        } else {
            throw new WrongUsageException(getUsage(sender));
        }
	}

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "allow", "disallow");
        }

        if (args.length == 2) {
            ArrayList<String> names = new ArrayList<String>();

            for (Race i : Race.values()) {
                names.add(i.name().toLowerCase());
            }

            return getListOfStringsMatchingLastWord(args, names.stream().toArray(String[]::new));
        }

        return null;
    }
}

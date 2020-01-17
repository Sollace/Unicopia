package com.minelittlepony.unicopia.command;

import java.util.List;

import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.entity.capabilities.IPlayer;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

class CommandGravity extends CommandBase {

    public String getName() {
        return "gravity";
    }

    public int getRequiredPermissionLevel() {
        return 4;
    }

    public String getUsage(ICommandSender sender) {
        return "commands.gravity.usage";
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException(getUsage(sender));
        }

        PlayerEntity player = getCommandSenderAsPlayer(sender);
        IPlayer iplayer = SpeciesList.instance().getPlayer(player);



        if (args[0].contentEquals("get")) {
            String translationKey = "commands.gravity.get";

            float gravity = iplayer.getGravity().getGravitationConstant();

            if (sender == player) {
                player.sendMessage(new TextComponentTranslation(translationKey, gravity));
            }

            notifyCommandListener(sender, this, 1, translationKey + ".other", player.getName(), gravity);

        } else if (args[0].contentEquals("set") || args.length > 2) {
            String translationKey = "commands.gravity.set";

            float gravity = Float.valueOf(args[2]);

            iplayer.getGravity().setGraviationConstant(gravity);
            iplayer.sendCapabilities(true);

            if (sender == player) {
                player.sendMessage(new TextComponentTranslation(translationKey, gravity));
            }

            notifyCommandListener(sender, this, 1, translationKey + ".other", player.getName(), gravity);
        } else {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "get", "set");
        }

        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        return null;
    }
}

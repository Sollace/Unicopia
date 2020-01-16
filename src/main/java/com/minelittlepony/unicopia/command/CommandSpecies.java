package com.minelittlepony.unicopia.command;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.SpeciesList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

class CommandSpecies extends CommandBase {

    @Override
    public String getName() {
        return "race";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(getRequiredPermissionLevel(), "help");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.race.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!processCommand(server, sender, args)) {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    protected boolean processCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        if (args.length < 1) {
            return false;
        }

        int playerIndex = this.getPlayerIndex(args);

        ServerPlayerEntity player = args.length > playerIndex ? getPlayer(server, sender, args[playerIndex]) : getCommandSenderAsPlayer(sender);

        if (args.length >= 2) {
            switch (args[0]) {
                case "set": return updateSpecies(sender, player, args);
                case "describe": return describeSpecies(player, args);
            }
        }

        switch (args[0]) {
            case "get": return printSpecies(sender, player);
            case "list": return list(player);
        }

        return false;
    }

    protected int getPlayerIndex(String[] args) {
        switch (args[0]) {
            case "set":
            case "describe": return 2;
            default: return 1;
        }
    }

    protected boolean updateSpecies(ICommandSender sender, EntityPlayer player, String[] args) {
        Race species = Race.fromName(args[1], Race.HUMAN);

        if (species.isDefault()) {
            if (player.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                ITextComponent message = new TextComponentTranslation("commands.race.fail", args[1].toUpperCase());
                message.getStyle().setColor(TextFormatting.RED);

                player.sendMessage(message);
            }
        } else if (SpeciesList.instance().speciesPermitted(species, player)) {
            SpeciesList.instance().getPlayer(player).setSpecies(species);

            ITextComponent formattedName = new TextComponentTranslation(species.name().toLowerCase());

            if (player != sender) {
                notifyCommandListener(sender, this, 1, "commands.race.success.other", player.getName(), formattedName);
            } else {
                if (player.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                    player.sendMessage(new TextComponentTranslation("commands.race.success.self"));
                }
                notifyCommandListener(sender, this, 1, "commands.race.success.otherself", player.getName(), formattedName);
            }
        } else if (player.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback")) {
            player.sendMessage(new TextComponentTranslation("commands.race.permission"));
        }

        return true;
    }

    protected boolean printSpecies(ICommandSender sender, EntityPlayer player) {
        Race spec = SpeciesList.instance().getPlayer(player).getSpecies();

        String name = "commands.race.tell.";
        name += player == sender ? "self" : "other";

        ITextComponent race = new TextComponentTranslation(spec.getTranslationKey());
        ITextComponent message = new TextComponentTranslation(name);

        race.getStyle().setColor(TextFormatting.GOLD);

        message.appendSibling(race);

        player.sendMessage(message);

        return true;
    }

    protected boolean list(EntityPlayer player) {
        player.sendMessage(new TextComponentTranslation("commands.race.list"));

        ITextComponent message = new TextComponentString("");

        boolean first = true;
        for (Race i : Race.values()) {
            if (!i.isDefault() && SpeciesList.instance().speciesPermitted(i, player)) {
                message.appendSibling(new TextComponentString((!first ? "\n" : "") + " - " + i.name().toLowerCase()));
                first = false;
            }
        }

        message.getStyle().setColor(TextFormatting.GOLD);

        player.sendMessage(message);

        return true;
    }

    protected boolean describeSpecies(EntityPlayer player, String[] args) {
        Race species = Race.fromName(args[1], null);

        if (species == null) {
            if (player.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                player.sendMessage(new TextComponentTranslation("commands.race.fail", args[1].toUpperCase()));
            }
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

        return true;
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "get", "set", "list", "describe");
        }

        if (args.length == 2 && (args[0].contentEquals("set") || args[0].contentEquals("describe"))) {
            ArrayList<String> names = new ArrayList<String>();

            EntityPlayer player = sender instanceof EntityPlayer ? (EntityPlayer)sender : null;

            for (Race i : Race.values()) {
                if (args[0].contentEquals("describe") || (!i.isDefault() && SpeciesList.instance().speciesPermitted(i, player))) {
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

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == getPlayerIndex(args);
    }
}

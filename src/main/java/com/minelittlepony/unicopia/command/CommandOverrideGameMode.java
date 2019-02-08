package com.minelittlepony.unicopia.command;

import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandGameMode;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;

class CommandOverrideGameMode extends CommandGameMode {
    public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException {
        if (params.length <= 0) {
            throw new WrongUsageException("commands.gamemode.usage");
        }

        GameType gametype = getGameModeFromCommand(sender, params[0]);

        EntityPlayerMP entityplayermp = params.length >= 2 ? getPlayer(server, sender, params[1]) : getCommandSenderAsPlayer(sender);

        updateGameMode(entityplayermp, gametype);

        ITextComponent chatcomponenttranslation = new TextComponentTranslation("gameMode." + gametype.getName(), new Object[0]);

        if (entityplayermp != sender) {
            notifyCommandListener(sender, this, 1, "commands.gamemode.success.other", entityplayermp.getName(), chatcomponenttranslation);
        } else {
            notifyCommandListener(sender, this, 1, "commands.gamemode.success.self", chatcomponenttranslation);
        }
    }

    private void updateGameMode(EntityPlayerMP player, GameType m) {
        boolean flying = player.capabilities.isFlying;

        player.setGameType(m);
        player.capabilities.isFlying = PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies().canFly();

        if (flying != player.capabilities.isFlying) {
            player.sendPlayerAbilities();
        }

        player.fallDistance = 0;
    }
}

package com.minelittlepony.unicopia.command;

import java.util.List;

import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.minelittlepony.unicopia.magic.spells.SpellDisguise;

import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class CommandDisguise extends Command {

    @Override
    public String getName() {
        return "disguise";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.disguise.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException(getUsage(sender));
        }

        ServerPlayerEntity player = args.length > 1 ? getPlayer(server, sender, args[0]) : getCommandSenderAsPlayer(sender);

        IPlayer iplayer = SpeciesList.instance().getPlayer(player);

        Entity entity = constructDisguiseEntity(player.world, args);

        if (entity == null) {
            throw new CommandException("commands.disguise.notfound", args[1]);
        }

        SpellDisguise effect = iplayer.getEffect(SpellDisguise.class, true);

        if (effect == null) {
            iplayer.setEffect(new SpellDisguise().setDisguise(entity));
        } else {
            effect.setDisguise(entity);
        }

        if (player != sender) {
            notifyCommandListener(sender, this, 1, "commands.disguise.success.other", player.getName(), entity.getName());
        } else {
            if (player.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                player.sendMessage(new TextComponentTranslation("commands.disguise.success.self", entity.getName()));
            }
            notifyCommandListener(sender, this, 1, "commands.disguise.success.otherself", player.getName(), entity.getName());
        }
    }

    protected Entity constructDisguiseEntity(World world, String[] args) throws CommandException {
        NBTTagCompound nbt = getEntityNBT(args);
        nbt.setString("id", args[1]);

        return AnvilChunkLoader.readWorldEntityPos(nbt, world, 0, 0, 0, false);
    }

    protected NBTTagCompound getEntityNBT(String[] args) throws CommandException {
        if (args.length > 2) {
            try {
                return JsonToNBT.getTagFromJson(buildString(args, 2));
            } catch (NBTException e) {
                throw new CommandException("commands.summon.tagError", e.getMessage());
            }
        }

        return new NBTTagCompound();
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {

        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, EntityList.getEntityNameList());
        }

        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 1;
    }
}

package ru.defea.oneblockultima.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;

import java.util.List;

public class CommandSetOwner extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "setOwner";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/setOwner <x> <y> <z> <playerName>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length != 4)
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.usage") + getCommandUsage(sender)));
            return;
        }

        int x = parseInt(sender, args[0]);
        int y = parseInt(sender, args[1]);
        int z = parseInt(sender, args[2]);
        EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[3]);

        if (player == null)
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.player_not_found")));
            return;
        }

        TileEntity tileEntity = MinecraftServer.getServer().getEntityWorld().getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityOneBlockGenerator) {
            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tileEntity;
            generator.setOwnerId(player.getUniqueID());
            sender.addChatMessage(new ChatComponentText("§a" + StatCollector.translateToLocal("command.setOwner.success")));
        }
        else
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.no_generator")));
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 4)
        {
            List playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
            String[] playerNames = new String[playerList.size()];
            for (int i = 0; i < playerList.size(); i++)
            {
                playerNames[i] = ((EntityPlayerMP) playerList.get(i)).getCommandSenderName();
            }
            return getListOfStringsMatchingLastWord(args, playerNames);
        }

        return getListOfStringsMatchingLastWord(args, "~");
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
}

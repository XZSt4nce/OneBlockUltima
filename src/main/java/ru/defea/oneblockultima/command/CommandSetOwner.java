package ru.defea.oneblockultima.command;

import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import ru.defea.oneblockultima.block.ModBlocks;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;

import java.util.ArrayList;
import java.util.List;

public class CommandSetOwner extends CommandBase
{
    @Override
    public String getName()
    {
        return "setOwner";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/setOwner <x> <y> <z> <playerName>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length != 4)
        {
            sender.sendMessage(new TextComponentString("§c" + I18n.format("command.usage") + getUsage(sender)));
            return;
        }

        BlockPos pos = CommandBase.parseBlockPos(sender, args, 0, false);
        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(args[3]);

        if (player == null)
        {
            sender.sendMessage(new TextComponentString("§c" + I18n.format("command.player_not_found")));
            return;
        }

        TileEntity tileEntity = server.getEntityWorld().getTileEntity(pos);
        if (tileEntity instanceof TileEntityOneBlockGenerator) {
            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tileEntity;
            generator.setOwnerId(player.getUniqueID());
            sender.sendMessage(new TextComponentString("§a" + I18n.format("command.setOwner.success")));
        }
        else
        {
            sender.sendMessage(new TextComponentString("§c" + I18n.format("command.no_generator")));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }
        else if (args.length == 2)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }
        else if (args.length == 3)
        {
            return getListOfStringsMatchingLastWord(args, "~");
        }
        else if (args.length == 4)
        {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
}

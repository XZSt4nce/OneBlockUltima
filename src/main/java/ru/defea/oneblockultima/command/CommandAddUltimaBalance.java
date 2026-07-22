package ru.defea.oneblockultima.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import ru.defea.oneblockultima.capability.IOneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider;
import ru.defea.oneblockultima.network.PacketSyncPlayerData;

public class CommandAddUltimaBalance extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "addUltimaBalance";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/addUltimaBalance <amount>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length != 1)
        {
            sender.addChatMessage(new ChatComponentTranslation("§c" + StatCollector.translateToLocalFormatted("command.usage"), getCommandUsage(sender)));
            return;
        }

        int amount;
        try
        {
            amount = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException ex)
        {
            sender.addChatMessage(new ChatComponentTranslation("command.addUltimaBalance.integer"));
            return;
        }

        if (!((Entity)sender instanceof EntityPlayerMP))
        {
            sender.addChatMessage(new ChatComponentTranslation("command.addUltimaBalance.player"));
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP)(Entity)sender;
        IOneBlockPlayerData data = OneBlockPlayerDataProvider.get(player);
        if (data == null)
        {
            sender.addChatMessage(new ChatComponentTranslation("command.addUltimaBalance.no_data"));
            return;
        }

        data.addCurrency(amount);
        PacketSyncPlayerData.sendToPlayer(player);
        sender.addChatMessage(new ChatComponentTranslation("command.addUltimaBalance.success", amount, data.getCurrency()));
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
}

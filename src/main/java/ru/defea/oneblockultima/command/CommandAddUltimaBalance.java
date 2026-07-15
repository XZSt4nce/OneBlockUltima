package ru.defea.oneblockultima.command;

import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import ru.defea.oneblockultima.capability.IOneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider;
import ru.defea.oneblockultima.network.PacketSyncPlayerData;

public class CommandAddUltimaBalance extends CommandBase
{
    @Override
    public String getName()
    {
        return "addUltimaBalance";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/addUltimaBalance <amount>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length != 1)
        {
            sender.sendMessage(new TextComponentTranslation("§c" + I18n.format("command.usage"), getUsage(sender)));
            return;
        }

        int amount;
        try
        {
            amount = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException ex)
        {
            sender.sendMessage(new TextComponentTranslation("command.addUltimaBalance.integer"));
            return;
        }

        if (!(sender.getCommandSenderEntity() instanceof EntityPlayerMP))
        {
            sender.sendMessage(new TextComponentTranslation("command.addUltimaBalance.player"));
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP)sender.getCommandSenderEntity();
        IOneBlockPlayerData data = OneBlockPlayerDataProvider.get(player);
        if (data == null)
        {
            sender.sendMessage(new TextComponentTranslation("command.addUltimaBalance.no_data"));
            return;
        }

        data.addCurrency(amount);
        PacketSyncPlayerData.sendToPlayer(player);
        sender.sendMessage(new TextComponentTranslation("command.addUltimaBalance.success", amount, data.getCurrency()));
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
}

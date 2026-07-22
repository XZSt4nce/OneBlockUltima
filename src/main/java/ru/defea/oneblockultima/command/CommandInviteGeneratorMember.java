package ru.defea.oneblockultima.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.defea.oneblockultima.block.ModBlocks;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;

import java.util.ArrayList;
import java.util.List;

public class CommandInviteGeneratorMember extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "inviteGeneratorMember";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/inviteGeneratorMember <playerName>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (!((Entity)sender instanceof EntityPlayerMP))
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.inviteGeneratorMember.only_player")));
            return;
        }

        if (args.length != 1)
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.inviteGeneratorMember.usage")));
            return;
        }

        EntityPlayerMP owner = (EntityPlayerMP) (Entity)sender;
        World world = owner.worldObj;
        int genX = (int) owner.posX;
        int genY = (int) owner.posY - 1;
        int genZ = (int) owner.posZ;

        if (world.getBlock(genX, genY, genZ) != ModBlocks.ONE_BLOCK_GENERATOR)
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.inviteGeneratorMember.not_near_generator")));
            return;
        }

        TileEntity tileEntity = world.getTileEntity(genX, genY, genZ);
        if (!(tileEntity instanceof TileEntityOneBlockGenerator))
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.no_generator")));
            return;
        }

        TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tileEntity;
        if (!generator.isOwner(owner))
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.inviteGeneratorMember.owner_only")));
            return;
        }

        EntityPlayerMP target = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[0]);
        if (target == null)
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.player_not_found")));
            return;
        }

        if (target.getUniqueID().equals(owner.getUniqueID()))
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.inviteGeneratorMember.self_invite")));
            return;
        }

        generator.addPendingInvite(target.getUniqueID(), owner.getUniqueID(), 1200);
        ((ICommandSender) target).addChatMessage(new ChatComponentText("§a" + StatCollector.translateToLocal("command.inviteGeneratorMember.invitation_received")));
        sender.addChatMessage(new ChatComponentText("§a" + StatCollector.translateToLocalFormatted("command.inviteGeneratorMember.invitation_sent", target.getCommandSenderName())));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length != 1)
        {
            return new ArrayList();
        }

        List playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        String[] playerNames = new String[playerList.size()];
        for (int i = 0; i < playerList.size(); i++)
        {
            playerNames[i] = ((EntityPlayerMP) playerList.get(i)).getCommandSenderName();
        }
        return getListOfStringsMatchingLastWord(args, playerNames);
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }
}

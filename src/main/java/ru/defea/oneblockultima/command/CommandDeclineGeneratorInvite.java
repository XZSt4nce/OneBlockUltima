package ru.defea.oneblockultima.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.defea.oneblockultima.block.ModBlocks;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;

import java.util.ArrayList;
import java.util.List;

public class CommandDeclineGeneratorInvite extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "declineGeneratorInvite";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/declineGeneratorInvite";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (!((Entity)sender instanceof EntityPlayerMP))
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.declineGeneratorInvite.only_player")));
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) (Entity)sender;
        World world = player.worldObj;
        int genX = (int) player.posX;
        int genY = (int) player.posY - 1;
        int genZ = (int) player.posZ;

        if (world.getBlock(genX, genY, genZ) != ModBlocks.ONE_BLOCK_GENERATOR)
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.declineGeneratorInvite.not_near_generator")));
            return;
        }

        TileEntity tileEntity = world.getTileEntity(genX, genY, genZ);
        if (!(tileEntity instanceof TileEntityOneBlockGenerator))
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.no_generator")));
            return;
        }

        TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) tileEntity;
        if (!generator.declineInvite(player.getUniqueID()))
        {
            sender.addChatMessage(new ChatComponentText("§c" + StatCollector.translateToLocal("command.declineGeneratorInvite.no_invite")));
            return;
        }

        sender.addChatMessage(new ChatComponentText("§a" + StatCollector.translateToLocal("command.declineGeneratorInvite.declined")));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        return new ArrayList();
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}

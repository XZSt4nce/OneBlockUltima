package ru.defea.oneblockultima.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.defea.oneblockultima.gui.GuiHandler;
import ru.defea.oneblockultima.tile.TileEntityOneBlockGenerator;

public class PacketRequestGuiOpen implements IMessage
{
    private int x;
    private int y;
    private int z;

    public PacketRequestGuiOpen()
    {
    }

    public PacketRequestGuiOpen(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<PacketRequestGuiOpen, IMessage>
    {
        @Override
        public IMessage onMessage(PacketRequestGuiOpen message, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null)
            {
                return null;
            }

            World world = player.worldObj;
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (!(te instanceof TileEntityOneBlockGenerator))
            {
                return null;
            }

            TileEntityOneBlockGenerator generator = (TileEntityOneBlockGenerator) te;

            if (generator.isFree() && generator.canBeClaimedBy(player.getUniqueID()))
            {
                GuiHandler.openClaimScreen(player, message.x, message.y, message.z);
            }
            else if (ru.defea.oneblockultima.event.ModEvents.ensureGeneratorAccess(world, message.x, message.y, message.z, player, generator))
            {
                GuiHandler.open(player, message.x, message.y, message.z);
            }

            return null;
        }
    }
}

package ru.defea.oneblockultima.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ru.defea.oneblockultima.gui.ContainerOneBlock;

public class PacketOneBlockAction implements IMessage
{
    public enum Action
    {
        SELECT_SET,
        UPGRADE_SET
    }

    private BlockPos generatorPos;
    private Action action;
    private String setId;

    public PacketOneBlockAction()
    {
    }

    public PacketOneBlockAction(BlockPos generatorPos, Action action, String setId)
    {
        this.generatorPos = generatorPos;
        this.action = action;
        this.setId = setId;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        generatorPos = BlockPos.fromLong(buf.readLong());
        action = Action.values()[buf.readByte()];
        setId = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeLong(generatorPos.toLong());
        buf.writeByte(action.ordinal());
        ByteBufUtils.writeUTF8String(buf, setId);
    }

    public static class Handler implements IMessageHandler<PacketOneBlockAction, IMessage>
    {
        @Override
        public IMessage onMessage(PacketOneBlockAction message, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    if (!(player.openContainer instanceof ContainerOneBlock))
                    {
                        return;
                    }

                    ContainerOneBlock container = (ContainerOneBlock) player.openContainer;
                    if (!container.getGeneratorPos().equals(message.generatorPos))
                    {
                        return;
                    }

                    if (message.action == Action.SELECT_SET)
                    {
                        container.applySelectSet(message.setId);
                        // Принудительно синхронизируем контейнер с клиентом
                        player.openContainer.detectAndSendChanges();
                    }
                    else if (message.action == Action.UPGRADE_SET)
                    {
                        container.applyUpgradeSet(message.setId);
                        // Принудительно синхронизируем контейнер с клиентом
                        player.openContainer.detectAndSendChanges();
                    }
                }
            });
            return null;
        }
    }
}

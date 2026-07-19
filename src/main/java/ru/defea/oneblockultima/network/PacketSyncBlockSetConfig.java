package ru.defea.oneblockultima.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.config.BlockSetConfig;

public class PacketSyncBlockSetConfig implements IMessage
{
    private String json;

    public PacketSyncBlockSetConfig()
    {
    }

    public PacketSyncBlockSetConfig(String json)
    {
        this.json = json;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        json = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, json);
    }

    public static void sendToPlayer(String json, EntityPlayer player)
    {
        if (!(player instanceof EntityPlayerMP))
        {
            return;
        }
        PacketSyncBlockSetConfig packet = new PacketSyncBlockSetConfig(json);
        ModMessages.sendToPlayer(packet, (EntityPlayerMP) player);
    }

    public static class Handler implements IMessageHandler<PacketSyncBlockSetConfig, IMessage>
    {
        @Override
        public IMessage onMessage(PacketSyncBlockSetConfig message, MessageContext ctx)
        {
            try
            {
                if (message.json != null && !message.json.isEmpty())
                {
                    BlockSetConfig.loadFromServerJson(message.json);
                    OneBlockUltima.getLogger().info("[Sync] Applied server BlockSetConfig, sets count: " + BlockSetConfig.get().getSets().size());
                }
            }
            catch (Exception e)
            {
                OneBlockUltima.getLogger().error("Error handling PacketSyncBlockSetConfig", e);
            }
            return null;
        }
    }
}

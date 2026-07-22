package ru.defea.oneblockultima.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.nio.charset.StandardCharsets;

public class PacketSyncBlockSetConfig implements IMessage
{
    String json;

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
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        json = new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
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
}

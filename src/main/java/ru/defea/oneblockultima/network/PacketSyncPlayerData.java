package ru.defea.oneblockultima.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.defea.oneblockultima.capability.IOneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider;

import java.util.HashMap;
import java.util.Map;

public class PacketSyncPlayerData implements IMessage
{
    int currency;
    int brokenBlocksCount;
    Map<String, Integer> setLevels;
    Map<String, Integer> brokenBlocksBySet;

    public PacketSyncPlayerData()
    {
        setLevels = new HashMap<String, Integer>();
        brokenBlocksBySet = new HashMap<String, Integer>();
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        currency = buf.readInt();
        brokenBlocksCount = buf.readInt();

        int setLevelsCount = buf.readInt();
        setLevels = new HashMap<String, Integer>();
        for (int i = 0; i < setLevelsCount; i++)
        {
            setLevels.put(ByteBufUtils.readUTF8String(buf), buf.readInt());
        }

        int brokenCount = buf.readInt();
        brokenBlocksBySet = new HashMap<String, Integer>();
        for (int i = 0; i < brokenCount; i++)
        {
            brokenBlocksBySet.put(ByteBufUtils.readUTF8String(buf), buf.readInt());
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(currency);
        buf.writeInt(brokenBlocksCount);

        buf.writeInt(setLevels.size());
        for (Map.Entry<String, Integer> entry : setLevels.entrySet())
        {
            ByteBufUtils.writeUTF8String(buf, entry.getKey());
            buf.writeInt(entry.getValue());
        }

        buf.writeInt(brokenBlocksBySet.size());
        for (Map.Entry<String, Integer> entry : brokenBlocksBySet.entrySet())
        {
            ByteBufUtils.writeUTF8String(buf, entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    public static void sendToPlayer(EntityPlayer player)
    {
        if (!(player instanceof EntityPlayerMP))
        {
            return;
        }

        IOneBlockPlayerData playerData = OneBlockPlayerDataProvider.get(player);
        if (!(playerData instanceof OneBlockPlayerData))
        {
            return;
        }

        OneBlockPlayerData data = (OneBlockPlayerData) playerData;
        PacketSyncPlayerData packet = new PacketSyncPlayerData();
        packet.currency = data.getCurrency();
        packet.brokenBlocksCount = data.getBrokenBlocksCount();
        packet.setLevels = new HashMap<String, Integer>(data.getSetLevels());
        packet.brokenBlocksBySet = new HashMap<String, Integer>(data.getBrokenBlocksBySet());
        ModMessages.sendToPlayer(packet, (EntityPlayerMP) player);
    }

}

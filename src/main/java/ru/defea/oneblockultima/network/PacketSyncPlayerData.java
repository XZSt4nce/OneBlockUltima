package ru.defea.oneblockultima.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.capability.IOneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider;
import ru.defea.oneblockultima.event.ModEvents;

import java.util.HashMap;
import java.util.Map;

public class PacketSyncPlayerData implements IMessage
{
    private int currency;
    private int brokenBlocksCount;
    private Map<String, Integer> setLevels;
    private Map<String, Integer> brokenBlocksBySet;

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

    public static class Handler implements IMessageHandler<PacketSyncPlayerData, IMessage>
    {
        @Override
        public IMessage onMessage(PacketSyncPlayerData message, MessageContext ctx)
        {
            try
            {
                EntityPlayer localPlayer = net.minecraft.client.Minecraft.getMinecraft().thePlayer;
                if (localPlayer == null)
                {
                    return null;
                }

                IOneBlockPlayerData data = OneBlockPlayerDataProvider.get(localPlayer);
                if (data instanceof OneBlockPlayerData)
                {
                    OneBlockPlayerData playerData = (OneBlockPlayerData) data;
                    playerData.setCurrency(message.currency);
                    playerData.setBrokenBlocksTotal(message.brokenBlocksCount);
                    playerData.getSetLevels().clear();
                    playerData.getSetLevels().putAll(message.setLevels);
                    playerData.getBrokenBlocksBySet().clear();
                    playerData.getBrokenBlocksBySet().putAll(message.brokenBlocksBySet);
                    OneBlockPlayerDataProvider.saveToEntity(localPlayer, playerData);
                    ModEvents.syncDisplayedCurrency(localPlayer, playerData.getCurrency());
                }
            }
            catch (Exception e)
            {
                OneBlockUltima.getLogger().error("Error handling PacketSyncPlayerData", e);
            }
            return null;
        }
    }
}

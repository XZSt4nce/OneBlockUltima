package ru.defea.oneblockultima.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import ru.defea.oneblockultima.OneBlockUltima;
import ru.defea.oneblockultima.capability.IOneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerData;
import ru.defea.oneblockultima.capability.OneBlockPlayerDataProvider;
import ru.defea.oneblockultima.config.BlockSetConfig;
import ru.defea.oneblockultima.event.ModEvents;

@SideOnly(Side.CLIENT)
public class ClientPacketHandlers
{
    public static class SyncPlayerData implements IMessageHandler<PacketSyncPlayerData, IMessage>
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

    public static class SyncBlockSetConfig implements IMessageHandler<PacketSyncBlockSetConfig, IMessage>
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

package ru.defea.oneblockultima.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.defea.oneblockultima.OneBlockUltima;

public final class ModMessages
{
    public static final String CHANNEL = OneBlockUltima.MODID;

    public static SimpleNetworkWrapper network;

    private ModMessages()
    {
    }

    public static void register()
    {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
        int id = 0;
        network.registerMessage(PacketOneBlockAction.Handler.class, PacketOneBlockAction.class, id++, Side.SERVER);

        if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
        {
            network.registerMessage(ClientPacketHandlers.SyncPlayerData.class, PacketSyncPlayerData.class, id++, Side.CLIENT);
            network.registerMessage(ClientPacketHandlers.SyncBlockSetConfig.class, PacketSyncBlockSetConfig.class, id++, Side.CLIENT);
        }
        else
        {
            network.registerMessage(new IMessageHandler<PacketSyncPlayerData, IMessage>() {
                @Override public IMessage onMessage(PacketSyncPlayerData m, MessageContext c) { return null; }
            }, PacketSyncPlayerData.class, id++, Side.CLIENT);
            network.registerMessage(new IMessageHandler<PacketSyncBlockSetConfig, IMessage>() {
                @Override public IMessage onMessage(PacketSyncBlockSetConfig m, MessageContext c) { return null; }
            }, PacketSyncBlockSetConfig.class, id++, Side.CLIENT);
        }
    }

    public static void sendToServer(PacketOneBlockAction message)
    {
        network.sendToServer(message);
    }

    public static void sendToPlayer(PacketSyncPlayerData message, EntityPlayerMP player)
    {
        network.sendTo(message, player);
    }

    public static void sendToPlayer(PacketSyncBlockSetConfig message, EntityPlayerMP player)
    {
        network.sendTo(message, player);
    }
}

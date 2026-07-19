package ru.defea.oneblockultima.network;

import cpw.mods.fml.common.network.NetworkRegistry;
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
        network.registerMessage(PacketSyncPlayerData.Handler.class, PacketSyncPlayerData.class, id++, Side.CLIENT);
        network.registerMessage(PacketSyncBlockSetConfig.Handler.class, PacketSyncBlockSetConfig.class, id++, Side.CLIENT);
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

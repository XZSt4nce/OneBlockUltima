package ru.defea.oneblockultima;

import cpw.mods.fml.client.registry.RenderingRegistry;
import ru.defea.oneblockultima.client.render.OneBlockRenderHandler;
import ru.defea.oneblockultima.event.ModEventsClient;

public class ClientProxy extends CommonProxy
{
    public static int oneBlockRenderId = -1;

    @Override
    public void init()
    {
        super.init();
        oneBlockRenderId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new OneBlockRenderHandler(oneBlockRenderId));
        ModEventsClient.register();
    }
}

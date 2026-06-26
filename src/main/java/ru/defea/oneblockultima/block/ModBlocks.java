package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ru.defea.oneblockultima.OneBlockUltima;

@Mod.EventBusSubscriber(modid = OneBlockUltima.MODID)
public final class ModBlocks
{
    public static final BlockOneBlockGenerator ONE_BLOCK_GENERATOR = new BlockOneBlockGenerator();
    public static final BlockCustomPortalFrame CUSTOM_PORTAL_FRAME = new BlockCustomPortalFrame();
    public static final BlockCustomBedrock CUSTOM_BEDROCK = new BlockCustomBedrock();

    private ModBlocks()
    {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(ONE_BLOCK_GENERATOR);
        event.getRegistry().register(CUSTOM_PORTAL_FRAME);
        event.getRegistry().register(CUSTOM_BEDROCK);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        ItemBlock generator = new ItemBlock(ONE_BLOCK_GENERATOR);
        generator.setRegistryName(ONE_BLOCK_GENERATOR.getRegistryName());

        event.getRegistry().register(generator);
    }
}

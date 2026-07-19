package ru.defea.oneblockultima.tile;

import cpw.mods.fml.common.registry.GameRegistry;
import ru.defea.oneblockultima.OneBlockUltima;

public final class ModTileEntities
{
    private ModTileEntities()
    {
    }

    public static void register()
    {
        GameRegistry.registerTileEntity(
                TileEntityOneBlockGenerator.class,
                OneBlockUltima.MODID + "_one_block_generator"
        );
    }
}

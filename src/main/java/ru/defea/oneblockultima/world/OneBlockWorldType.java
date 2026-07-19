package ru.defea.oneblockultima.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkProviderFlat;

public class OneBlockWorldType extends WorldType
{
    public static final OneBlockWorldType ONE_BLOCK = new OneBlockWorldType();

    public OneBlockWorldType()
    {
        super("one_block");
    }

    public static void init()
    {
        ONE_BLOCK.getWorldTypeName();
    }

    public String getFlatGeneratorOptions()
    {
        return "3;minecraft:air;127";
    }

    @Override
    public boolean isCustomizable()
    {
        return false;
    }

    @Override
    public net.minecraft.world.chunk.IChunkProvider getChunkGenerator(World world, String generatorOptions)
    {
        if (generatorOptions == null || generatorOptions.isEmpty())
        {
            generatorOptions = getFlatGeneratorOptions();
        }
        return new ChunkProviderFlat(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
    }
}

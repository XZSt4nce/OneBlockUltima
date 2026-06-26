package ru.defea.oneblockultima.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.IChunkGenerator;

public class OneBlockWorldType extends WorldType
{
    public static final OneBlockWorldType ONE_BLOCK = new OneBlockWorldType();

    public OneBlockWorldType()
    {
        super("one_block");
    }

    public static void init()
    {
        // Force class loading / register the world type.
        ONE_BLOCK.getName();
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
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions)
    {
        // Use default flat world template if no options provided
        if (generatorOptions == null || generatorOptions.isEmpty())
        {
            generatorOptions = getFlatGeneratorOptions();
        }
        return new ChunkGeneratorFlat(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
    }
}


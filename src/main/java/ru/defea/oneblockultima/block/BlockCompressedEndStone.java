package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedEndStone extends Block {
    public BlockCompressedEndStone()
    {
        super(Material.ROCK, MapColor.SAND);
        init("compressed_end_stone_1x");
    }

    protected BlockCompressedEndStone(String name)
    {
        super(Material.ROCK, MapColor.SAND);
        init(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.END_STONE.getExplosionResistance(exploder) * 9.0F;
    }

    private void init(String name) {
        this.setSoundType(SoundType.STONE);
        this.setHardness(3F * 9F);
        this.setResistance(15F * 9F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setUnlocalizedName(name);
        this.setRegistryName(OneBlockUltima.MODID, name);
    }
}

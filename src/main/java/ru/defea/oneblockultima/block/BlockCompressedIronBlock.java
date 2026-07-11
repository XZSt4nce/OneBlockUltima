package ru.defea.oneblockultima.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import ru.defea.oneblockultima.OneBlockUltima;

public class BlockCompressedIronBlock extends Block {
    public BlockCompressedIronBlock()
    {
        super(Material.IRON, MapColor.IRON);
        init("compressed_iron_block_1x");
    }

    protected BlockCompressedIronBlock(String name)
    {
        super(Material.IRON, MapColor.IRON);
        init(name);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return Blocks.IRON_BLOCK.getExplosionResistance(exploder) * 9.0F;
    }

    private void init(String name) {
        this.setSoundType(SoundType.METAL);
        this.setHardness(5F * 9F);
        this.setResistance(10F * 9F);
        setCreativeTab(OneBlockUltima.modTab);
        this.setUnlocalizedName(name);
        this.setRegistryName(OneBlockUltima.MODID, name);
    }
}
